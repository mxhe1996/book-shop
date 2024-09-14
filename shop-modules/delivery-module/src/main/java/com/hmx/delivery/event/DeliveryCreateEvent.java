package com.hmx.delivery.event;

import com.hmx.delivery.domain.DeliveryRecord;
import com.hmx.shop.domain.EsDocument;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

@Setter
@Getter
public class DeliveryCreateEvent implements DeliveryEvent{

    private EsDocument<DeliveryRecord> deliveryRecordEsDocument;

    @Override
    public List<Object> getEventObjects() {
        return Collections.singletonList(deliveryRecordEsDocument);
    }

    @Override
    public String getReplayMessage() {
        return null;
    }
}
