package com.hmx.order.event;

import com.hmx.order.domain.Order;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

@Setter
@Getter
public class OrderCreatedEvent implements OrderEvent{


    private Object order;


    @Override
    public List<Object> getEventObjects() {
        return Collections.singletonList(order);
    }

    @Override
    public String getReplayMessage() {
        return "";
    }
}
