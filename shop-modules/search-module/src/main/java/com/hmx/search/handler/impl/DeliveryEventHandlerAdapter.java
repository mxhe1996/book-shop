package com.hmx.search.handler.impl;

import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.alibaba.fastjson2.JSONObject;
import com.hmx.es.service.ElasticService;
import com.hmx.kafka.domain.DomainEvent;
import com.hmx.search.annotation.ElasticEventHandler;
import com.hmx.search.domain.OrderDocument;
import com.hmx.search.handler.EventHandlerAdapter;
import com.hmx.shop.domain.EsDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hmx.search.domain.OrderDocument.map2OrderDocument;

@Slf4j
@Component
@ElasticEventHandler
public class DeliveryEventHandlerAdapter implements EventHandlerAdapter {


    private final String basicAggregateType = "delivery";

    private final ElasticService elasticService;


    public DeliveryEventHandlerAdapter(ElasticService elasticService){
        this.elasticService = elasticService;
    }


    @Override
    public boolean support(String aggregateType) {
        return aggregateType.toLowerCase().contains(basicAggregateType);
    }

    @Override
    public void createEventProcess(DomainEvent domainEvent){
        EsDocument<Map<String,Object>> esDelivery = JSONObject.parseObject(domainEvent.getEventObjects().get(0).toString(), EsDocument.class);
        Map<String, Object> deliveryDocument = esDelivery.getDocument();

        try{
            SearchResponse<OrderDocument> searchResponse = elasticService.searchDocument(esDelivery.getIndex(), esDelivery.getId(), OrderDocument.class);
            OrderDocument source = searchResponse.hits().hits().get(0).source();
            assert source != null;
            source.setDeliveryId(Long.parseLong(deliveryDocument.getOrDefault("deliveryId","-1").toString()));
            source.setDeliveryNo(deliveryDocument.getOrDefault("deliveryNo","").toString());
            elasticService.updateDocument(esDelivery.getIndex(), esDelivery.getId(),source);
            log.info("ElasticSearch-DeliveryEventHandlerAdapter>>>更新成功: index:{}, order文档Id:{}, 事件类型:{}",esDelivery.getIndex(), esDelivery.getId(),"create");
        }catch (Exception e){
            log.error("ElasticSearch-DeliveryEventHandlerAdapter>>>更新失败: index:{}, order文档Id:{}, 事件类型:{}",esDelivery.getIndex(), esDelivery.getId(),"create");
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateEventProcess(DomainEvent domainEvent) {
        EsDocument<Map<String,Object>> changedField = JSONObject.parseObject(domainEvent.getEventObjects().get(0).toString(), EsDocument.class);
        try{
            SearchResponse<OrderDocument> searchResponse = elasticService.searchDocument(changedField.getIndex(), "deliveryId", changedField.getId(), OrderDocument.class);
            List<Hit<OrderDocument>> hits = searchResponse.hits().hits();
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

            log.info("ElasticSearch-DeliveryEventHandlerAdapter>>>更新成功: index:{},文档Id:{}, 事件类型:{}",changedField.getIndex(), changedField.getId(),"update");
        }catch (Exception e){
            log.error("ElasticSearch-DeliveryEventHandlerAdapter>>>更新失败: index:{},文档Id:{}, 事件类型:{}",changedField.getIndex(), changedField.getId(),"update");
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteEventProcess(DomainEvent domainEvent) {

    }
}
