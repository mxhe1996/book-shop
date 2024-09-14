package com.hmx.order.conf;

import com.hmx.kafka.mapper.OutboxMapper;
import com.hmx.kafka.service.KafkaService;
import com.hmx.kafka.service.OutboxService;
import com.hmx.shop.factory.DynamicPropertiesFactory;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutor;
import org.springframework.util.backoff.FixedBackOff;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@RequiredArgsConstructor
public class OrderConfig {

    private final DynamicPropertiesFactory kafkaProperties;


    @Bean
    KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String,String>> kafkaOrderListenerContainerFactory(){
        ConcurrentKafkaListenerContainerFactory<String, String> containerFactory = new ConcurrentKafkaListenerContainerFactory<>();
        Map<String,Object> properties = kafkaProperties.generateMap("consumer");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,false);
        // 只有设置以下开始手动提交，enable.auto.commit=false才会生效 ，否则交由spring-kafka自主提交
        containerFactory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        containerFactory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(properties));
        containerFactory.setConcurrency(1);
        containerFactory.setBatchListener(true);
        long interval = 10000;   //重试间隔    10s重试一次
        long maxAttempts = 10;  //最大重试次数 最多重试10次
//        当 Consumer 消费消息异常的时候，进行拦截处理,重试小于最大次数时，重新投递该消息给 Consumer
//       代替 SeekToCurrentBatchErrorHandler()
        CommonErrorHandler commonErrorHandler = new DefaultErrorHandler(new FixedBackOff(interval,maxAttempts));
        containerFactory.setCommonErrorHandler(commonErrorHandler);
        return containerFactory;
    }



    @Bean
    public OutboxService outboxService(OutboxMapper outboxMapper, ThreadPoolTaskScheduler taskScheduler, KafkaService kafkaService){
        return new OutboxService(outboxMapper, taskScheduler, kafkaService);
    }


    /**
     * 没用spring security那一套，暂时搁置
     * @return
     */
    @Bean
    public Executor asyncSecurityContextExecutor(){
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(5);
        taskExecutor.setMaxPoolSize(15);
        taskExecutor.setQueueCapacity(100);
        taskExecutor.setThreadNamePrefix("SecurityContext Thread-");
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        taskExecutor.initialize();
        return new DelegatingSecurityContextExecutor(taskExecutor);
    }

}
