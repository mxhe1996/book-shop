package com.hmx.search.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@TableName("search_processed")
public class SearchEventProcessed {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    // 事件id
    @TableField("max_event_id")
    private Long maxEventId;

    // 事件类型
    @TableField("event_type")
    private String eventType;

    // 领域类型
    @TableField("aggregate_type")
    private String aggregateType;

    // 领域对象id
    @TableField("aggregate_id")
    private String aggregateId;
}
