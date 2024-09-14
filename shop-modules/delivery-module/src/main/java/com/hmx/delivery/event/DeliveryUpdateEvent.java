package com.hmx.delivery.event;

import com.hmx.delivery.domain.DeliveryRecord;
import com.hmx.shop.domain.EsDocument;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Setter
@Getter
public class DeliveryUpdateEvent implements DeliveryEvent{

    private EsDocument esDocument;

    public DeliveryUpdateEvent(){}

    public DeliveryUpdateEvent(EsDocument esDocument){
        this.esDocument = esDocument;
    }

    @Override
    public List<Object> getEventObjects() {
        return Collections.singletonList(esDocument);
    }

    @Override
    public String getReplayMessage() {
        return null;
    }


}
