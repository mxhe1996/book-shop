package com.hmx.delivery.listener;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hmx.delivery.domain.DeliveryRecord;
import com.hmx.delivery.domain.DeliveryEventProcessed;
import com.hmx.delivery.event.DeliveryCreateEvent;
import com.hmx.delivery.mapper.DeliveryEventProcessedMapper;
import com.hmx.delivery.mapper.DeliveryRecordMapper;
import com.hmx.kafka.domain.DomainEventEnvelope;
import com.hmx.kafka.domain.Outbox;
import com.hmx.kafka.mapper.OutboxMapper;
import com.hmx.kafka.utils.EventUtils;
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
public class DeliveryListener {

    private final DeliveryEventProcessedMapper deliveryEventProcessedMapper;

    private final DeliveryRecordMapper deliveryRecordMapper;

    private final OutboxMapper outboxMapper;

    @KafkaListener(topics = {"delivery"}, containerFactory = "kafkaDeliveryListenerContainerFactory")
    public void OrderDeliveryEventCreate(List<ConsumerRecord<String, String>> records, Consumer<String, String> consumer) {
        Map<TopicPartition, OffsetAndMetadata> offsetAndMetaMap = new HashMap<>();
        List<Exception> exceptionList = new ArrayList<>();

        try {
            for (ConsumerRecord<String, String> record : records) {
                DomainEventEnvelope domainEventEnvelope = JSONObject.parseObject(record.value(), DomainEventEnvelope.class);
                DeliveryEventProcessed processedQueryBody = DeliveryEventProcessed.builder()
                        .eventType(domainEventEnvelope.getEventType())
                        .eventId(domainEventEnvelope.getEventId().toString())
                        .aggregateId(domainEventEnvelope.getAggregateId().toString())
                        .aggregateType(domainEventEnvelope.getAggregateType())
                        .build();
                Long count = deliveryEventProcessedMapper.selectCount(new QueryWrapper<>(processedQueryBody));
                if (count < 1) {

                    // 没有处理过该事件
                    DeliveryRecord deliveryRecord = new DeliveryRecord();
                    deliveryRecord.setCreateTime(new Date());
                    deliveryRecord.setDeliveryNo(UUID.randomUUID().toString());
                    deliveryRecord.setOrderId(Long.parseLong(domainEventEnvelope.getAggregateId().toString()));
                    deliveryRecordMapper.insert(deliveryRecord);

                    DeliveryCreateEvent deliveryCreateEvent = new DeliveryCreateEvent();
                    deliveryCreateEvent.setDeliveryRecordEsDocument(new EsDocument<>("order",deliveryRecord.getOrderId().toString(),deliveryRecord));
                    DomainEventEnvelope envelope = EventUtils.generateDomainEventEnvelope(DeliveryRecord.class.getTypeName(), deliveryRecord.getDeliveryId(), deliveryCreateEvent);
                    Outbox deliveryCreateOutbox = Outbox.builder().topicName("query").eventBody(JSONObject.toJSONString(envelope)).uuid(UUID.randomUUID().toString()).build();
                    outboxMapper.insertBox(deliveryCreateOutbox);

                    // 事件处理完成记录
                    deliveryEventProcessedMapper.insert(processedQueryBody);
                } else {
                    // 重复事件
                    log.error("重复性事件 {}", domainEventEnvelope);
                }
                offsetAndMetaMap.computeIfAbsent(new TopicPartition(record.topic(), record.partition()), (tp) -> new OffsetAndMetadata(record.offset() + 1));
                offsetAndMetaMap.computeIfPresent(new TopicPartition(record.topic(), record.partition()), (tp, oldValue) -> oldValue.offset() > record.offset() ? oldValue : new OffsetAndMetadata(record.offset() + 1));
            }// for
        } catch (Exception e) {
            log.error("Delivery常见监听器 事件处理异常{}", e.getMessage());
            exceptionList.add(e);
        } finally {
            if (!exceptionList.isEmpty()){
                throw new RuntimeException(exceptionList.toString());
            }
            if (!offsetAndMetaMap.isEmpty()){
                consumer.commitSync(offsetAndMetaMap);
            }
        }



    }
}
