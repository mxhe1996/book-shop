package com.hmx.kafka.config;

import com.hmx.kafka.config.properties.KafkaProperties;
import com.hmx.shop.factory.DynamicPropertiesFactory;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class KafkaDynamicGenerateFactory {

    private final KafkaProperties kafkaProperties;


    public KafkaProducer generateProducer(){
        Map producer = kafkaProperties.generateMap("producer");
        return new KafkaProducer<>(producer);
    }

    public KafkaConsumer generateConsumer(){
        Map consumer = kafkaProperties.generateMap("consumer");
        return new KafkaConsumer(consumer);
    }



}
