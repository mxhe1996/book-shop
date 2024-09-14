package com.hmx.order.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@TableName("order_processed")
public class OrderProcessed {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    // 事件id
    @TableField("event_id")
    private String eventId;

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
