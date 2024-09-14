package com.hmx.delivery.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("delivery_record")
public class DeliveryRecord {

    @TableId(value = "delivery_id", type = IdType.AUTO)
    private Long deliveryId;

    // 订单编号
    @TableField(value = "delivery_no")
    private String deliveryNo;

    @TableField(value = "order_id")
    private Long orderId;

    @TableField(value = "phases")
    private String phases;

    @TableField(value = "create_time")
    private Date createTime;
}
