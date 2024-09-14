package com.hmx.search.listener;


import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.hmx.es.service.ElasticService;
import com.hmx.kafka.domain.DomainEvent;
import com.hmx.kafka.domain.DomainEventEnvelope;
import com.hmx.search.domain.OrderDocument;
import com.hmx.search.domain.SearchEventProcessed;
import com.hmx.search.handler.EventHandler;
import com.hmx.search.mapper.SearchEventProcessedMapper;
import com.hmx.shop.domain.EsDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static com.hmx.search.domain.OrderDocument.map2OrderDocument;

@Component
@Slf4j
@RequiredArgsConstructor
public class SearchListener {

    private final SearchEventProcessedMapper processedMapper;

    private final ElasticService elasticService;

    private final EventHandler eventHandler;

/*    @KafkaListener(topics = {"query"}, containerFactory = "kafkaSearchListenerContainerFactory")
    public void CUDEventReceive(List<ConsumerRecord<String,String>> records, Consumer<String, String> consumer){
        Map<TopicPartition, OffsetAndMetadata> offsetAndMetadataMap = new HashMap<>();
        List<Exception> exceptionList = new ArrayList<>();
        for (ConsumerRecord<String, String> record : records) {
            try{
                DomainEventEnvelope domainEventEnvelope = JSON.parseObject(record.value(), DomainEventEnvelope.class);
                String aggregateType = domainEventEnvelope.getAggregateType();
                String aggregateId = domainEventEnvelope.getAggregateId().toString();
                Long eventId = Long.parseLong(domainEventEnvelope.getEventId().toString());
                DomainEvent event = domainEventEnvelope.getEvent();
                String eventType = domainEventEnvelope.getEventType();
                // todo refactor

                List<SearchEventProcessed> processedRecord = processedMapper.searchMaxEventId(aggregateType, aggregateId);
                if (processedRecord.isEmpty()){
                    // 新采集任务
                    SearchEventProcessed eventProcessed = SearchEventProcessed.builder()
                            .aggregateType(aggregateType)
                            .aggregateId(aggregateId)
                            .eventType(eventType)
                            .maxEventId(eventId)
                            .build();
                    // es save()
                    EsDocument esDocument = JSONObject.parseObject(event.getEventObjects().get(0).toString(), EsDocument.class);
                    Map<String,Object> body = JSONObject.parseObject(esDocument.getDocument().toString(), Map.class);
                    List<OrderDocument.Item> itemList = JSONObject.parseObject(body.get("itemsInfo").toString(), List.class);

                    OrderDocument orderDocument = map2OrderDocument(body);
                    orderDocument.setItemsList(itemList);
                    BulkResponse bulkResponse = elasticService.insertDocuments(esDocument.getIndex(), Collections.singletonList(orderDocument));
                    log.info("插入结果：{}",bulkResponse);
                    // processed save()
                    processedMapper.insert(eventProcessed);
                }else if (processedRecord.get(0).getMaxEventId()<eventId){
                    if (eventType.toLowerCase().contains("update")){
                        // update task
                        assert !event.getEventObjects().isEmpty();
                        // todo query body remake
                        EsDocument<Map<String,Object>> changedField = JSONObject.parseObject(event.getEventObjects().get(0).toString(), EsDocument.class);
                        try{

                            SearchResponse<OrderDocument> searchResponse = elasticService.searchDocument(changedField.getIndex(), changedField.getId(), OrderDocument.class);
                            List<Hit<OrderDocument>> hits = searchResponse.hits().hits();
//                            Map<String,Object> source = (Map<String,Object>)hits.get(0).source();
                            OrderDocument source = hits.get(0).source();
                            Map<String,Object> orderDocMap = new HashMap<>();
                            // 可能涉及时间变换，将obj转换为map再还原回去
                            // load value
                            for (Field declaredField : source.getClass().getDeclaredFields()) {
                                declaredField.setAccessible(true);
                                orderDocMap.put(declaredField.getName(),declaredField.get(source));
                            }
                            // update value
                            orderDocMap.putAll(changedField.getDocument());

                            OrderDocument orderDocument = map2OrderDocument(orderDocMap);
                            elasticService.updateDocument(changedField.getIndex(), changedField.getId(),orderDocument);

                            // 更新操作历史库
                            SearchEventProcessed searchEventProcessed = processedRecord.get(0);
                            searchEventProcessed.setMaxEventId(eventId);
                            processedMapper.updateById(searchEventProcessed);
                            log.info("ElasticSearch-Listener>>>更新成功: index:{},文档Id:{}, 事件类型:{}",changedField.getIndex(), changedField.getId(),eventType);
                        }catch (Exception e){
                            log.error("ElasticSearch-Listener>>>更新失败: index:{},文档Id:{}, 事件类型:{}",changedField.getIndex(), changedField.getId(),eventType);
                            throw new RuntimeException(e);
                        }
                    }
                }else {
                    //无效
                    log.error("ElasticSearch-Listener>>>无效事件: 事件类型:{}",eventType);
                }
                offsetAndMetadataMap.computeIfAbsent(new TopicPartition(record.topic(), record.partition()),(tp)->new OffsetAndMetadata(record.offset() + 1));
                offsetAndMetadataMap.computeIfPresent(new TopicPartition(record.topic(), record.partition()), (tp, oldMeta)-> oldMeta.offset() > record.offset() ?oldMeta:new OffsetAndMetadata(record.offset() + 1));
            }catch (Exception e){
                log.error("error:{}",e.getMessage());
                exceptionList.add(e);
//                throw new RuntimeException(e);
            }finally {
                if (!exceptionList.isEmpty()){
                    throw new RuntimeException(String.valueOf(exceptionList));
                }
                if (!offsetAndMetadataMap.isEmpty()){
                    consumer.commitSync(offsetAndMetadataMap);
                }
            }

        } // for
    }*/

    @KafkaListener(topics = {"query"}, containerFactory = "kafkaSearchListenerContainerFactory")
    public void CUDEventReceive(List<ConsumerRecord<String,String>> records, Consumer<String, String> consumer){
        Map<TopicPartition, OffsetAndMetadata> offsetAndMetadataMap = new HashMap<>();
        List<Exception> exceptionList = new ArrayList<>();
        for (ConsumerRecord<String, String> record : records) {
            try{
                DomainEventEnvelope domainEventEnvelope = JSON.parseObject(record.value(), DomainEventEnvelope.class);
                String aggregateType = domainEventEnvelope.getAggregateType();
                String aggregateId = domainEventEnvelope.getAggregateId().toString();
                Long eventId = Long.parseLong(domainEventEnvelope.getEventId().toString().split("-")[1]);
                String eventType = domainEventEnvelope.getEventType();

                List<SearchEventProcessed> processedRecord = processedMapper.searchMaxEventId(aggregateType, aggregateId);
                if (processedRecord.isEmpty()||processedRecord.get(0).getMaxEventId()<eventId){
                    // 有效事件
                    eventHandler.handler(domainEventEnvelope);
                    // 更新操作历史库
                    if (processedRecord.isEmpty()){
                        SearchEventProcessed eventProcessed = SearchEventProcessed.builder()
                                .aggregateType(aggregateType)
                                .aggregateId(aggregateId)
                                .eventType(eventType)
                                .maxEventId(eventId)
                                .build();
                        processedMapper.insert(eventProcessed);
                    }else {
                        SearchEventProcessed searchEventProcessed = processedRecord.get(0);
                        searchEventProcessed.setMaxEventId(eventId);
                        processedMapper.updateById(searchEventProcessed);
                    }
                    log.info("ElasticSearch-Listener>>>有效事件 成功执行: aggregateType:{},aggregateId:{}, 事件类型:{}",aggregateId, aggregateId,eventType);
                }else {
                    //无效
                    log.error("ElasticSearch-Listener>>>无效事件: 事件类型:{}",eventType);
                }
                offsetAndMetadataMap.computeIfAbsent(new TopicPartition(record.topic(), record.partition()),(tp)->new OffsetAndMetadata(record.offset() + 1));
                offsetAndMetadataMap.computeIfPresent(new TopicPartition(record.topic(), record.partition()), (tp, oldMeta)-> oldMeta.offset() > record.offset() ?oldMeta:new OffsetAndMetadata(record.offset() + 1));
            }catch (Exception e){
                log.error("error:{}",e.getMessage());
                exceptionList.add(e);
//                throw new RuntimeException(e);
            }finally {
                if (!exceptionList.isEmpty()){
                    throw new RuntimeException(String.valueOf(exceptionList));
                }
                if (!offsetAndMetadataMap.isEmpty()){
                    consumer.commitSync(offsetAndMetadataMap);
                }
            }

        } // for
    }



}
