package com.hmx.kafka.mapper;

import com.hmx.kafka.domain.Outbox;

import java.util.List;

public interface OutboxMapper {

    public List<Outbox> selectBox(Outbox outbox);

    public void deleteBox(Long[] ids);

    public void deleteBoxOnce(Long id);

    public int insertBox(Outbox outbox);

    public Outbox selectBoxByUUid(String uuid);

}
