package com.hmx.kafka.service;

import com.alibaba.fastjson2.JSONObject;
import com.hmx.kafka.domain.DomainEvent;
import com.hmx.kafka.domain.DomainEventEnvelope;
import com.hmx.kafka.domain.EventBody;
import com.hmx.kafka.domain.Outbox;
import com.hmx.kafka.mapper.OutboxMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.Future;

import static com.hmx.kafka.utils.EventUtils.generateDomainEventEnvelope;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaService {

    private final KafkaProducer kafkaProducer;

    private final KafkaAdmin kafkaAdmin;

    private final OutboxMapper outboxMapper;

    /**
     * 查询主题
     * @param topicName
     * @return
     */
    public TopicDescription selectTopic(String topicName){
        Map<String, TopicDescription> stringTopicDescriptionMap = kafkaAdmin.describeTopics(topicName);
        if (!stringTopicDescriptionMap.isEmpty()) {
            return stringTopicDescriptionMap.get(topicName);
        }
        return null;
    }

    /**
     * 创建主题
     * @param topicName
     * @return
     */
    public boolean createTopic(String topicName){
        NewTopic topic = TopicBuilder.name(topicName).build();
        try{
            kafkaAdmin.createOrModifyTopics(topic);
            return true;
        }catch (Exception e){
            log.error("创建主题:[{}] 异常",topicName);
            return false;
        }
    }

    /**
     * 发送信息
     * @param topicName
     * @param eventBody
     * @return
     */
    @Deprecated
    public String sendMessage(String topicName, EventBody eventBody){

        TopicDescription topicDescription = selectTopic(topicName);
        if (Objects.isNull(topicDescription)){
            createTopic(topicName);
        }

        String messageId = eventBody.getHeader().getMessageId();
        String jsonObject = JSONObject.toJSONString(eventBody);
        ProducerRecord producerRecord = new ProducerRecord<String,String>(topicName,jsonObject);
        kafkaProducer.send(producerRecord);

        // jdbc acid存储
        return messageId;
    }

    public void sendMessage(String topicName, Object object){
        sendMessage(topicName,JSONObject.toJSONString(object));
    }

    public void sendMessage(String topicName, String jsonString){
        kafkaProducer.send(new ProducerRecord(topicName,jsonString));
    }



    /**
     * 发布领域事件
     * @param topicName
     * @param objectType
     * @param domainEvent
     */
    public void publishEvent(String topicName, Class objectType, DomainEvent domainEvent){
        DomainEventEnvelope domainEventEnvelope = generateDomainEventEnvelope(objectType.toString(),null, domainEvent);

        /*Outbox outbox = new Outbox();
        outbox.setUuid(UUID.randomUUID().toString());
        outboxMapper.insertBox(outbox);
        log.info("新增发送记录:{},{}",outbox.getId(),outbox.getUuid());

        String jsonString = JSONObject.toJSONString(envelope)+"&"+outbox.getUuid(); // 在尾部追加uuid 用于在outbox定位删除
        log.info("记录到当前发布事件: {}",jsonString);*/

        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topicName, JSONObject.toJSONString(domainEventEnvelope));
        kafkaProducer.send(producerRecord);
    }




}
