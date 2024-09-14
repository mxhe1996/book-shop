package com.hmx.kafka.utils;

import com.hmx.kafka.domain.DomainEvent;
import com.hmx.kafka.domain.DomainEventEnvelope;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.UUID;

/**
 *  生成DomainEventEnvelope
 *  其中eventId 由outbox入库后，转发到对应主题之前set
 */
public class EventUtils {


    public static DomainEventEnvelope generateDomainEventEnvelope(String aggregateType, Object aggregateId, DomainEvent domainEvent){
        long time = new Date().from(LocalDateTime.now().toInstant(ZoneOffset.UTC)).getTime();
        String messageId = time + "-" + UUID.randomUUID();

        DomainEventEnvelope<DomainEvent> envelope = new DomainEventEnvelope<>();
        envelope.setEvent(domainEvent);
//        envelope.setEventId();
        envelope.setEventType(domainEvent.getClass().getTypeName());
        envelope.setAggregateType(aggregateType);
        envelope.setAggregateId(aggregateId);
        // header
        envelope.setEnvelopMetaInfo(DomainEventEnvelope.MetaHeader.builder().messageId(messageId).build());
        return envelope;
    }

    public static DomainEventEnvelope generateDomainEventEnvelope(String aggregateType, Object aggregateId, DomainEvent domainEvent, DomainEventEnvelope.MetaHeader header){
        DomainEventEnvelope<DomainEvent> envelope = new DomainEventEnvelope<>();
        envelope.setEvent(domainEvent);
        envelope.setEventType(domainEvent.getClass().getTypeName());
        envelope.setAggregateId(aggregateId);
        envelope.setAggregateType(aggregateType);
        envelope.setEnvelopMetaInfo(header);
        return envelope;
    }


}
