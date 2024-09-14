package com.hmx.kafka.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@RequiredArgsConstructor
@AutoConfigureAfter(KafkaAutoConfiguration.class)
public class KafkaConfigure {

    private final KafkaDynamicGenerateFactory generateFactory;

    @Bean
    public KafkaProducer kafkaProducer(){
        return generateFactory.generateProducer();
    }

    @Bean
    public AdminClient kafkaClient(KafkaAdmin kafkaAdmin){
        return AdminClient.create(kafkaAdmin.getConfigurationProperties());
    }



    @Bean
    public ThreadPoolTaskScheduler circleSendEventTaskScheduler(ApplicationContext context){
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(5);
        threadPoolTaskScheduler.setThreadFactory(new CustomizableThreadFactory(context.getId()+" NonStopped Thread --- "));
        threadPoolTaskScheduler.initialize();
        return threadPoolTaskScheduler;
    }


}
