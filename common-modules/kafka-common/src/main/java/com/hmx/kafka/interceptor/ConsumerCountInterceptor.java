package com.hmx.kafka.interceptor;

import org.apache.kafka.clients.consumer.ConsumerInterceptor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 接收消息的时候，获取生产者放置的时间戳
 * 接收信息统计，日志记录
 */
public class ConsumerCountInterceptor implements ConsumerInterceptor<String,String> {

    private static final Logger log = LoggerFactory.getLogger(ConsumerCountInterceptor.class);

    @Override
    public ConsumerRecords<String, String> onConsume(ConsumerRecords<String, String> consumerRecords) {
//        ConsumerRecords<Object, Object> records = new ConsumerRecords<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        Map<TopicPartition, List<ConsumerRecord<String,String>>> map = new HashMap<>();
        Set<TopicPartition> topicPartitions = consumerRecords.partitions();
        topicPartitions.forEach(topicPartition -> {
            List<ConsumerRecord<String, String>> records = consumerRecords.records(topicPartition);
            List<ConsumerRecord<String,String>> records_ori = records.stream().map(record -> {
                return new ConsumerRecord<String,String>(record.topic()
                        , record.partition()
                        , record.offset()
                        , record.key()
                        , record.value().substring(record.value().indexOf("-") + 1));
            }).collect(Collectors.toList());

            Long start_time = records.get(0).value().indexOf("-")>0?Long.parseLong(records.get(0).value().substring(0,records.get(0).value().indexOf("-"))):System.currentTimeMillis();
            Long end_time = records.get(records.size()-1).value().indexOf("-")>0?Long.parseLong(records.get(records.size()-1).value().substring(0,records.get(records.size()-1).value().indexOf("-"))):System.currentTimeMillis();

            log.info("接收"+topicPartition.topic()+"主题，自"+dateFormat.format(new Date(start_time))+" ~ "+dateFormat.format(new Date(end_time))+", 共计"+records.size()+"条消息");
            map.put(topicPartition,records_ori);
        });

        return new ConsumerRecords<>(map);
    }

    @Override
    public void onCommit(Map<TopicPartition, OffsetAndMetadata> map) {

    }

    @Override
    public void close() {

    }

    @Override
    public void configure(Map<String, ?> map) {

    }
}

