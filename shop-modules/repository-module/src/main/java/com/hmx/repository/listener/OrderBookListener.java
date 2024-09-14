package com.hmx.repository.listener;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hmx.kafka.domain.DomainEvent;
import com.hmx.kafka.domain.DomainEventEnvelope;
import com.hmx.kafka.domain.Outbox;
import com.hmx.kafka.mapper.OutboxMapper;
import com.hmx.kafka.service.KafkaService;
import com.hmx.kafka.utils.EventUtils;
import com.hmx.repository.domain.Book;
import com.hmx.repository.domain.RepositoryProcessed;
import com.hmx.repository.event.BookReduceEvent;
import com.hmx.repository.mapper.BookMapper;
import com.hmx.repository.mapper.BookProcessedMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Slf4j
@Component
@RequiredArgsConstructor
public class OrderBookListener {


    private final BookMapper bookMapper;

    private final BookProcessedMapper processedMapper;

    private final OutboxMapper outboxMapper;


    @KafkaListener(topics = {"repository"}, containerFactory = "kafkaRepositoryListenerContainerFactory")
    public void bookOrderProcess(List<ConsumerRecord<String, String>> records,  Consumer<String,String> consumer){
        Map<TopicPartition, OffsetAndMetadata> offsetMap = new HashMap<>();
        List<Exception> exceptionList = new ArrayList<>();

        for (ConsumerRecord<String, String> record : records) {
            // 添加ConsumerRecord偏移量
            offsetMap.computeIfAbsent(new TopicPartition(record.topic(), record.partition()), (tp)->new OffsetAndMetadata(record.offset()+1));
            offsetMap.computeIfPresent(new TopicPartition(record.topic(), record.partition()),(tp,oldOffset)->oldOffset.offset()>record.offset()?oldOffset:new OffsetAndMetadata(record.offset()+1));
            try{
                singleRecordProcess(record);
            }catch (Exception e){
                log.error("[bookOrderProcess]监听器 遭遇异常事件：{}",e.toString());
                log.error("====>>>exceptionList 添加一条");
                exceptionList.add(e);
            }
        }

        if (!exceptionList.isEmpty()){
            // 让spring-kafka捕获，进入重试
            throw new RuntimeException(exceptionList.toString());
        }
        // 提交位移
        if(!offsetMap.isEmpty()) {
            consumer.commitSync(offsetMap);
        }

    }

    private void singleRecordProcess(ConsumerRecord<String, String> record) {
        DomainEventEnvelope domainEventEnvelope = JSONObject.parseObject(record.value(), DomainEventEnvelope.class);
        String aggregateId = domainEventEnvelope.getAggregateId().toString(); /* orderId */

        // check is_processed event
        RepositoryProcessed processedEvent = RepositoryProcessed.builder()
                .aggregateId(aggregateId)
                .aggregateType(domainEventEnvelope.getAggregateType())
                .eventType(domainEventEnvelope.getEventType())
                .eventId(domainEventEnvelope.getEventId().toString())
                .build();
        Long count = processedMapper.selectCount(new QueryWrapper<>(processedEvent));
        if (count>0){
            // 无效事件  该事件已经被处理过
            log.error("检测到历史事件再消费情况， 当前事件[eventId:{}]无效，事件内容{}",processedEvent.getEventId(),domainEventEnvelope);
        }else {
            // 有效事件
            log.info("有效事件记录，当前事件[eventId:{}],事件内容{}",domainEventEnvelope.getEventId(),domainEventEnvelope);
            DomainEvent event = domainEventEnvelope.getEvent();

            BookReduceEvent bookReduceEvent = reduceBookRepository(event);
            // 装载原始订单id 将事件存入数据库
            bookReduceEvent.setOrderId(domainEventEnvelope.getAggregateId().toString());
            DomainEventEnvelope replayEventEnvelop = EventUtils.generateDomainEventEnvelope(domainEventEnvelope.getAggregateType(),domainEventEnvelope.getAggregateId(), bookReduceEvent);

            Outbox outbox = Outbox.builder()
                    .uuid(UUID.randomUUID().toString())
                    .topicName(domainEventEnvelope.getEnvelopMetaInfo().getAddressId())
                    .eventBody(JSONObject.toJSONString(replayEventEnvelop))
                    .build();
            outboxMapper.insertBox(outbox);

            // 无论有没有异常订单子项 都添加事件成功执行记录
            processedMapper.insert(processedEvent);
            log.info("添加事件执行记录");
        }
    }

    /**
     * 具体业务逻辑
     * @param domainEvent 事件本体，包含完整订单实例 细分多个订购物品实例
     */
    @Transactional
    public BookReduceEvent reduceBookRepository(DomainEvent domainEvent) {
        BookReduceEvent bookReduceEvent = new BookReduceEvent();

        for (Object eventObject : domainEvent.getEventObjects()) {
            // 每个子订单 包含同一个租户下的不同的信息
            Map<String,Object> orderInfo = JSONObject.parseObject(eventObject.toString(),Map.class);
            List<Map<String,Object>> items = JSON.parseObject(orderInfo.get("itemsInfo").toString(), List.class);
            for (Map<String, Object> item : items) {
                Object tenantId = item.get("tenantId");
                Object bookId = item.get("itemId");
                Integer bookOrderNumber = Integer.parseInt(item.get("orderNumber").toString());
                log.info(">>>>>商铺id:{},被购买书籍{}，{}本",tenantId,bookId,bookOrderNumber);

                // 消费消息
                int row = bookMapper.reduceStockNumber( Long.parseLong(bookId.toString()), bookOrderNumber);
                if (row<1){
                    // 没有行被影响
                    Book originBook = bookMapper.selectById((long) bookId);
                    log.error("在购买{}本书籍id:{}发生异常异常,库内书籍信息{}",bookOrderNumber,bookId,originBook.toString());
                    // 采集错误信息
                    BookReduceEvent.ErrorReduceResult reduceResult = new BookReduceEvent.ErrorReduceResult();
//                    reduceResult.setOrderId(orderId);
                    reduceResult.setBookInfo(originBook);
                    reduceResult.setNumber(bookOrderNumber);
                    bookReduceEvent.addResult(reduceResult);
                }
            }


        }
        return bookReduceEvent;

    }


}
