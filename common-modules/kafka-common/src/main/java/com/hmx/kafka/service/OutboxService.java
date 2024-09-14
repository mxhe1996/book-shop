package com.hmx.kafka.service;

import com.alibaba.fastjson2.JSONObject;
import com.hmx.kafka.domain.DomainEventEnvelope;
import com.hmx.kafka.domain.Outbox;
import com.hmx.kafka.mapper.OutboxMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import javax.annotation.PostConstruct;
import java.time.*;
import java.util.Date;
import java.util.List;

@Slf4j
public class OutboxService {

    private final OutboxMapper outboxMapper;

    private final ThreadPoolTaskScheduler taskScheduler;

    private final KafkaService kafkaService;

    public OutboxService(OutboxMapper outboxMapper, ThreadPoolTaskScheduler taskScheduler, KafkaService kafkaService) {
        this.outboxMapper = outboxMapper;
        this.taskScheduler = taskScheduler;
        this.kafkaService = kafkaService;
    }

    @PostConstruct
    public void queryAndPublishEvent(){
        taskScheduler.scheduleAtFixedRate(this::circleExecuteContent, Duration.ofSeconds(30));
    }


    private void circleExecuteContent(){
        List<Outbox> outboxes = outboxMapper.selectBox(new Outbox());
        Long[] ids = outboxes.stream().map(outbox -> {
            String topicName = outbox.getTopicName();
            String eventBody = outbox.getEventBody();
            try{
                // 给event填充id
                DomainEventEnvelope envelope = JSONObject.parseObject(eventBody, DomainEventEnvelope.class);
                LocalDateTime localDateTime = LocalDateTime.now(ZoneId.of("UTC+8")); // 默认当前时区是+8
                ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of("UTC")); // 切换为UTC时间
                long timeStamp = Date.from(zonedDateTime.toInstant()).getTime();
                envelope.setEventId(timeStamp+"-"+outbox.getId());
                String eventBodyDetail = JSONObject.toJSONString(envelope);
                kafkaService.sendMessage(topicName, eventBodyDetail);
                log.info("orderOutBox>>>>当前线程：{} 向主题{}, 发送事件{}, 成功执行",Thread.currentThread().getName(),topicName,eventBodyDetail);
                return outbox.getId();
            }catch (Exception e){
                log.error("orderOutBox>>>>当前线程：{} 向主题{}, 发送事件{}, 发生异常",Thread.currentThread().getName(),topicName,eventBody);
                return -1L;
            }
        }).toArray(Long[]::new);

        if (ids.length>0){
            outboxMapper.deleteBox(ids);
        }

    }

    public static void main(String[] args) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC+8"));
        System.out.println("now:"+now);
        ZonedDateTime zonedDateTime = now.atZone(ZoneId.of("UTC"));
        Date date = Date.from(zonedDateTime.toInstant());
        System.out.println(zonedDateTime);
        System.out.println(date);
        System.out.println(date.getTime());


        LocalDateTime now1 = LocalDateTime.now(ZoneId.of("UTC+8"));
        System.out.println("now:"+now1);
        ZonedDateTime zonedDateTime1 = now1.atZone(ZoneId.of("UTC"));
        System.out.println(zonedDateTime1);
        Date date1 = Date.from(zonedDateTime1.toInstant());
        System.out.println(date1);
        System.out.println(date1.getTime());

    }

}
