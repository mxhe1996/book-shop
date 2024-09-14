package com.hmx.kafka.config.properties;

import com.hmx.kafka.interceptor.ConsumerCountInterceptor;
import com.hmx.kafka.interceptor.ProducerLabelInterceptor;
import com.hmx.shop.factory.DynamicPropertiesFactory;
import lombok.Data;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Data
@Component
public class KafkaProperties implements DynamicPropertiesFactory {


    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.producer.acks}")
    private String acks;

    @Value("${spring.kafka.producer.key-serializer}")
    private String keySerializer;

    @Value("${spring.kafka.producer.value-serializer}")
    private String valueSerializer;


    @Value("${spring.kafka.consumer.enable-auto-commit}")
    private boolean enableAutoCommit;

    @Value("${spring.kafka.consumer.key-deserializer}")
    private String keyDeserializer;

    @Value("${spring.kafka.consumer.value-deserializer}")
    private String valueDeserializer;

    @Value("${spring.kafka.consumer.auto-offset-reset}")
    private String autoOffsetReset;


    @Value("${spring.application.name}")
    private String serviceId;


    private Properties generateProducerPro(){
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,bootstrapServers);
        properties.put(ProducerConfig.ACKS_CONFIG,acks);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,keySerializer);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,valueSerializer);
        properties.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, ProducerLabelInterceptor.class.getName());
        return properties;
    }

    private Properties generateConsumerPro(){
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,bootstrapServers);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,keyDeserializer);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,valueDeserializer);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,autoOffsetReset);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,enableAutoCommit);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG,"ServiceId:"+serviceId); // spring-application-name
        properties.put(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, ConsumerCountInterceptor.class.getName());
//        properties.put(ConsumerConfig.CLIENT_ID_CONFIG,"local-orderServe");

        return properties;
    }



    @Override
    public Map generateMap(String flag) {
        switch (flag){
            case "consumer": return generateConsumerPro();
            case "producer": return generateProducerPro();
            default: return this.notFoundFlag();
        }

    }



}
