package com.hmx.kafka.interceptor;


import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Map;

/**
 * 在发送消息时，内容前面加时间戳
 */
public class ProducerLabelInterceptor implements ProducerInterceptor<String,String> {
    @Override
    public ProducerRecord<String, String> onSend(ProducerRecord<String, String> producerRecord) {
        return new ProducerRecord<>(producerRecord.topic(),
                producerRecord.partition(),
                producerRecord.timestamp(),
                producerRecord.key(),
                System.currentTimeMillis()+"-"+producerRecord.value()
        );
    }

    @Override
    public void onAcknowledgement(RecordMetadata recordMetadata, Exception e) {

    }

    @Override
    public void close() {

    }

    @Override
    public void configure(Map<String, ?> map) {

    }
}

