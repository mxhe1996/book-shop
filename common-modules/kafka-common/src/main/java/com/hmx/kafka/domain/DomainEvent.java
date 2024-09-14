package com.hmx.kafka.domain;

import java.util.List;

// 领域事件表标识接口
public interface DomainEvent {

    default public Object getKeyPoint(){
        return null;
    }

    public List<Object> getEventObjects();

    // 返回回调的方法
    public String getReplayMessage();
}
