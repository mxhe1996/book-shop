package com.hmx.kafka.domain;

import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 领域事件发布类
 * @param <T>
 */
@Data
public class DomainEventEnvelope<T extends DomainEvent> {

    // 事件元数据
    private MetaHeader envelopMetaInfo;

    // 事件实体类的 路径
    private String aggregateType;

    // 事件发布实体类的Id(s)
    private Object aggregateId;

    // 事件Id
    // 如果本次存储事件，那么该eventId是递增的，因此在对应的processed_table中只需要判断是否 aggregateType-aggregateId > max（eventId）
    // 本次存储不存储事件时，用messageId
    private Object eventId;

    // 事件类型
    private String eventType;

    private T event;


    public void parseAndSetHeader(String headerJson){
        DomainEventEnvelope.MetaHeader metaHeader = JSONObject.parseObject(headerJson, DomainEventEnvelope.MetaHeader.class);
        this.envelopMetaInfo = metaHeader;
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class MetaHeader{

        // 发送的消息Id
        private String messageId;

        // 请求返回的地址/通道
        private String addressId;

    }


}
