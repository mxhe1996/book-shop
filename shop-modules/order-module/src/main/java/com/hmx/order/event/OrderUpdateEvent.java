package com.hmx.order.event;

import com.hmx.shop.domain.EsDocument;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public class OrderUpdateEvent implements OrderEvent{

    // todo remake
    private EsDocument<Map<String,Object>> changedField = new EsDocument<>();

    @Override
    public List<Object> getEventObjects() {
        return Collections.singletonList(changedField);
    }

    @Override
    public String getReplayMessage() {
        return null;
    }
}
