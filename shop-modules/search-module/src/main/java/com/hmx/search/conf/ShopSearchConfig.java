package com.hmx.search.conf;

import com.hmx.kafka.config.properties.KafkaProperties;
import com.hmx.search.handler.EventHandler;
import com.hmx.search.handler.EventHandlerAdapter;
import com.hmx.shop.factory.DynamicPropertiesFactory;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class ShopSearchConfig {
    private final KafkaProperties KafkaProperties;

    private final ApplicationContext context;

    @Bean
    KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String,String>> kafkaSearchListenerContainerFactory(){
        ConcurrentKafkaListenerContainerFactory<String, String> containerFactory = new ConcurrentKafkaListenerContainerFactory<>();
        Map<String,Object> properties = KafkaProperties.generateMap("consumer");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,false);
        containerFactory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        containerFactory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(properties));
        containerFactory.setConcurrency(1);
        containerFactory.setBatchListener(true);
        long interval = 10000;   //重试间隔    10s重试一次
        long maxAttempts = 3;  //最大重试次数 最多重试10次
//        当 Consumer 消费消息异常的时候，进行拦截处理,重试小于最大次数时，重新投递该消息给 Consumer
//       代替 SeekToCurrentBatchErrorHandler()
        CommonErrorHandler commonErrorHandler = new DefaultErrorHandler(new FixedBackOff(interval,maxAttempts));
        containerFactory.setCommonErrorHandler(commonErrorHandler);
        return containerFactory;
    }

    @Bean
    public EventHandler eventHandler(){
        EventHandler eventHandler = new EventHandler();
        Map<String, EventHandlerAdapter> beansOfType = context.getBeansOfType(EventHandlerAdapter.class);
        beansOfType.forEach((name,bean)->eventHandler.addHandler(bean));
        return eventHandler;
    }



}
