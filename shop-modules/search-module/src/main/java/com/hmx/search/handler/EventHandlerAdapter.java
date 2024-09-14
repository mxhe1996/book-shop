package com.hmx.search.handler;

import com.hmx.kafka.domain.DomainEvent;

public interface EventHandlerAdapter {

    public boolean support(String aggregateType);

    public void createEventProcess(DomainEvent domainEvent);

    public void updateEventProcess(DomainEvent domainEvent);

    public void deleteEventProcess(DomainEvent domainEvent);

}
