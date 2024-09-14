package com.hmx.order.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmx.kafka.domain.DomainEventEnvelope;
import com.hmx.kafka.domain.Outbox;
import com.hmx.kafka.mapper.OutboxMapper;
import com.hmx.kafka.service.KafkaService;
import com.hmx.kafka.utils.EventUtils;
import com.hmx.order.domain.ItemInfo;
import com.hmx.order.domain.Order;
import com.hmx.order.domain.vo.OrderVo;
import com.hmx.order.enums.OrderStatus;
import com.hmx.order.event.OrderCreatedEvent;
import com.hmx.order.mapper.OrderMapper;
import com.hmx.redis.utils.SecurityUtils;
import com.hmx.shop.domain.EsDocument;
import com.hmx.shop.vo.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {


    private final OrderMapper orderMapper;

    private final KafkaService  kafkaService;

    private final OutboxMapper outboxMapper;



    public IPage<OrderVo> queryOrderWithCondition(OrderVo orderVo) {
        Page<Order> orderPage = orderMapper.selectPage(new Page<>(orderVo.getPageNum(), orderVo.getPageSize()), new QueryWrapper<>(Order.convertFromVo(orderVo)));
        IPage<OrderVo> orderVoIPage = orderPage.convert(Order::convert2Vo);
        return orderVoIPage;
    }

    @Transactional
    public Integer createOrder(List<ItemInfo> itemInfos) {
        LoginUser currentUser = SecurityUtils.getCurrentUser();
        String uuid = UUID.randomUUID().toString();
        Order order = Order.builder()
                .orderNo(uuid)
                .userId(currentUser.getId())
                .userName(currentUser.getUserName())
                .userId(currentUser.getId())
                .orderStatus(OrderStatus.SUBMITTED)
                .money(itemInfos.stream().map(item->item.getOrderNumber()*item.getPrice()).reduce(Integer::sum).get())
                .createTime(new Date())
                .itemsInfo(JSONObject.toJSONString(itemInfos))
                .build();

        int orderInserted = orderMapper.insert(order);
        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent();
        orderCreatedEvent.setOrder(order);


        // 向repository发布事件
        DomainEventEnvelope domainEventEnvelope = EventUtils.generateDomainEventEnvelope(
                Order.class.getTypeName(),
                order.getOrderId(),
                orderCreatedEvent,
                (DomainEventEnvelope.MetaHeader.builder().messageId(UUID.randomUUID().toString()).addressId("order").build()));
        Outbox outbox = Outbox.builder().uuid(UUID.randomUUID().toString())
                .eventBody(JSON.toJSONString(domainEventEnvelope))
                .topicName("repository")
                .build();
        outboxMapper.insertBox(outbox);

        orderCreatedEvent.setOrder(new EsDocument<Order>("order",order.getOrderId().toString(),order));

        // 向cqrs发送订单创建事件
        DomainEventEnvelope queryModuleEnvelop = EventUtils.generateDomainEventEnvelope(Order.class.getTypeName(),order.getOrderId(), orderCreatedEvent);
        Outbox queryModuleBox = Outbox.builder().uuid(UUID.randomUUID().toString())
//                .eventBody(JSON.toJSONString(queryModuleEnvelop))
                .eventBody(JSONObject.toJSONString(queryModuleEnvelop))
                .topicName("query")
                .build();
        outboxMapper.insertBox(queryModuleBox);
        //
        return orderInserted;



    }




}
