package com.hmx.delivery.service;

import com.alibaba.fastjson2.JSONObject;
import com.hmx.delivery.domain.DeliveryRecord;
import com.hmx.delivery.domain.PhaseDetail;
import com.hmx.delivery.event.DeliveryUpdateEvent;
import com.hmx.delivery.mapper.DeliveryRecordMapper;
import com.hmx.kafka.domain.DomainEventEnvelope;
import com.hmx.kafka.domain.Outbox;
import com.hmx.kafka.mapper.OutboxMapper;
import com.hmx.kafka.utils.EventUtils;
import com.hmx.shop.domain.EsDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class DeliveryService {

    private final DeliveryRecordMapper deliveryRecordMapper;

    private final OutboxMapper outboxMapper;

    public DeliveryService(DeliveryRecordMapper deliveryRecordMapper,OutboxMapper outboxMapper) {
        this.deliveryRecordMapper = deliveryRecordMapper;
        this.outboxMapper = outboxMapper;
    }

    public CompletableFuture<Integer> insertDeliveryPhase(Long deliveryId, PhaseDetail phaseDetail){

        return CompletableFuture.supplyAsync(() -> {
            DeliveryRecord deliveryRecord = deliveryRecordMapper.selectById(deliveryId);
            String phases = deliveryRecord.getPhases();
            List<PhaseDetail> phaseDetailList = JSONObject.parseObject(phases, List.class);
            if (Objects.isNull(phaseDetailList)){
                phaseDetailList = new ArrayList<>();
            }
            phaseDetailList.add(phaseDetail);
            deliveryRecord.setPhases(JSONObject.toJSONString(phaseDetailList));
            int insert1 = deliveryRecordMapper.updateDeliveryPhaseDetail(deliveryRecord.getDeliveryId(),deliveryRecord.getPhases());
            if (insert1 < 0) {
                throw new RuntimeException("插入失败");
            }
            return deliveryRecord;
        }).handle((deliveryRecord, throwable) -> {
            if (Objects.isNull(throwable)) {
                // 记录可能在视图中产生变化的字段
                Map<Object, Object> changeFieldMap = new HashMap<>();
                changeFieldMap.put("deliveryPhases",deliveryRecord.getPhases());
                changeFieldMap.put("deliveryId",deliveryRecord.getDeliveryId());
                changeFieldMap.put("deliveryNo",deliveryRecord.getDeliveryNo());
                DeliveryUpdateEvent deliveryUpdateEvent = new DeliveryUpdateEvent(new EsDocument<>("order",deliveryRecord.getOrderId().toString(),changeFieldMap));

                DomainEventEnvelope domainEventEnvelope = EventUtils.generateDomainEventEnvelope(deliveryRecord.getClass().getTypeName(), deliveryRecord.getDeliveryId(), deliveryUpdateEvent);
                Outbox outbox = Outbox.builder()
                        .eventBody(JSONObject.toJSONString(domainEventEnvelope))
                        .uuid(UUID.randomUUID().toString())
                        .topicName("query")
                        .build();
                if (outboxMapper.insertBox(outbox) <= 0) {
                    throw new RuntimeException("事件发送(存储)失败");
                }
                return 1;
            }
            log.error(throwable.toString());
            throw new RuntimeException(throwable);
        });

    }

}
