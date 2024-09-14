package com.hmx.order.listener;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.hmx.kafka.domain.DomainEvent;
import com.hmx.kafka.domain.DomainEventEnvelope;
import com.hmx.kafka.domain.Outbox;
import com.hmx.kafka.mapper.OutboxMapper;
import com.hmx.kafka.service.KafkaService;
import com.hmx.kafka.utils.EventUtils;
import com.hmx.order.domain.Order;
import com.hmx.order.domain.OrderProcessed;
import com.hmx.order.enums.OrderStatus;
import com.hmx.order.event.OrderUpdateEvent;
import com.hmx.order.mapper.OrderMapper;
import com.hmx.order.mapper.OrderProcessedMapper;
import com.hmx.shop.domain.EsDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class RepositoryEventListener {

    private final OrderMapper orderMapper;

    private final OrderProcessedMapper orderProcessedMapper;

    private final OutboxMapper outboxMapper;

    @KafkaListener(topics = {"order"}, containerFactory = "kafkaOrderListenerContainerFactory")
    public void processRepositoryCallBackEvent(List<ConsumerRecord<String,String>> records, Consumer<String,String> consumer){
        Map<TopicPartition, OffsetAndMetadata> offsetAndMetadataMap = new HashMap<>();
        List<Exception> exceptionList = new ArrayList<>();

        for (ConsumerRecord<String, String> record : records) {
            // 添加offset
            offsetAndMetadataMap.computeIfAbsent(new TopicPartition(record.topic(), record.partition()), (tp)->new OffsetAndMetadata(record.offset()+1));
            offsetAndMetadataMap.computeIfPresent(new TopicPartition(record.topic(), record.partition()),(tp,oldOffset)->oldOffset.offset()>record.offset()?oldOffset:new OffsetAndMetadata(record.offset()+1));
            try{
                DomainEventEnvelope domainEventEnvelope = JSON.parseObject(record.value(), DomainEventEnvelope.class);
                String eventId = domainEventEnvelope.getEventId().toString();
                String aggregateType = domainEventEnvelope.getAggregateType();
                Object aggregateId = domainEventEnvelope.getAggregateId();

                List<OrderProcessed> orderProcesseds = orderProcessedMapper.selectList(
                        new QueryWrapper<>(OrderProcessed
                                .builder()
                                .eventId(eventId)
                                .aggregateType(aggregateType)
                                .aggregateId(aggregateId.toString())
                                .build()
                        )
                );
                if (orderProcesseds.isEmpty()){
                    log.info("有效事件记录，当前事件[eventId:{}],事件内容{}",domainEventEnvelope.getEventId(),domainEventEnvelope);
                    // 没有被执行过
                    DomainEvent event = domainEventEnvelope.getEvent();
                    if (event.getEventObjects().isEmpty()){
                        // 没有异常错误 修正
                        Long orderId = Long.parseLong(aggregateId.toString()) ;
                        Order order = orderMapper.selectById(orderId);
                        int nextPhaseCode = order.getOrderStatus().getStatusCode() << 1;
                        OrderStatus orderStatus = OrderStatus.find(nextPhaseCode);
                        if (Objects.nonNull(orderStatus)){
                            // 更正订单状态
                            orderMapper.updateStatus(orderId,nextPhaseCode);
                            // 再发送更新事件
                            Map<String, Object> changedField = new HashMap<>();
                            changedField.put("updateTime",new Date());
                            changedField.put("orderStatus",orderStatus.getStatusInfo());
                            OrderUpdateEvent orderUpdateEvent = new OrderUpdateEvent();
                            orderUpdateEvent.setChangedField(new EsDocument<>("order",orderId.toString(),changedField));
                            DomainEventEnvelope updateEventEnvelop = EventUtils.generateDomainEventEnvelope(Order.class.getTypeName(), orderId, orderUpdateEvent);
                            Outbox queryModuleBox = Outbox.builder()
                                    .uuid(UUID.randomUUID().toString())
                                    .eventBody(JSONObject.toJSONString(updateEventEnvelop))
                                    .topicName("query")
                                    .build();
                            log.info("检测到回调通道信息,计划存储outbox：{}",queryModuleBox);
                            outboxMapper.insertBox(queryModuleBox);

                            Outbox deliveryOutBox = Outbox.builder()
                                    .uuid(UUID.randomUUID().toString())
                                    .eventBody(JSONObject.toJSONString(updateEventEnvelop))
                                    .topicName("delivery")
                                    .build();
                            log.info("存储delivery物流交付事件:{}",deliveryOutBox);
                            outboxMapper.insertBox(deliveryOutBox);

                        }
                    }

                    OrderProcessed processed = OrderProcessed.builder()
                            .aggregateId(aggregateId.toString())
                            .aggregateType(aggregateType)
                            .eventType(domainEventEnvelope.getEventType())
                            .eventId(eventId)
                            .build();
                    orderProcessedMapper.insert(processed);
                }else {
                    log.error("检测到历史事件再消费情况， 当前事件[eventId:{}]无效，事件内容{}",domainEventEnvelope.getEventId(),domainEventEnvelope);
                }
            }catch (Exception e){
                log.error("[bookOrderProcess]监听器 遭遇异常事件：{}",e.toString());
                log.error("====>>>exceptionList 添加一条");
                exceptionList.add(e);
            }
        } // for
        if (!exceptionList.isEmpty()){
            // 让spring-kafka捕获，进入重试
            throw new RuntimeException(exceptionList.toString());
        }
        // 提交位移
        if(!offsetAndMetadataMap.isEmpty()) {
            consumer.commitSync(offsetAndMetadataMap);
        }
    }

}
