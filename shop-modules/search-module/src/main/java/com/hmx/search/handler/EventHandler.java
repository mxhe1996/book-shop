package com.hmx.search.handler;

import com.hmx.kafka.domain.DomainEventEnvelope;
import org.springframework.web.servlet.HandlerAdapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EventHandler {


    private List<EventHandlerAdapter> eventHandlerAdapters = new ArrayList<>();

    public void addHandler(EventHandlerAdapter adapter){
        eventHandlerAdapters.add(adapter);
    }

    public void handler(DomainEventEnvelope domainEventEnvelope){
        EventHandlerAdapter eventHandlerAdapter = getEventHandlerAdapter(domainEventEnvelope);
        String eventType = domainEventEnvelope.getEventType().toLowerCase();
        if (eventType.contains("create")){
            eventHandlerAdapter.createEventProcess(domainEventEnvelope.getEvent());
        } else if (eventType.contains("update")) {
            eventHandlerAdapter.updateEventProcess(domainEventEnvelope.getEvent());
        } else if (eventType.contains("delete")) {
            eventHandlerAdapter.deleteEventProcess(domainEventEnvelope.getEvent());
        }

    }


    private EventHandlerAdapter getEventHandlerAdapter(DomainEventEnvelope domainEventEnvelope) {
        if (this.eventHandlerAdapters != null) {
            Iterator var2 = this.eventHandlerAdapters.iterator();
            while (var2.hasNext()) {
                EventHandlerAdapter adapter = (EventHandlerAdapter) var2.next();
                if (adapter.support(domainEventEnvelope.getAggregateType())) {
                    return adapter;
                }
            }
        }

        throw new RuntimeException(String.format("没有找到aggregateType:%s 对应的handler", domainEventEnvelope.getAggregateType()));
    }


}
