package com.hmx.order.domain;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hmx.order.domain.vo.OrderVo;
import com.hmx.order.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Data
@Builder
@TableName("order_info")
public class Order  {

    @TableId(value = "order_id", type = IdType.AUTO)
    private Long orderId;

    @TableField("order_no")
    private String orderNo;

    @TableField("user_id")
    private Long userId;

    @TableField("items_info")
    private String itemsInfo;

    @TableField("money")
    private Integer money;

    @TableField("user_name")
    private String userName;

    @TableField("create_time")
    private Date createTime;

    @TableField("update_time")
    private Date updateTime;

    @TableField("order_status")
    private OrderStatus orderStatus;


    public void setItemInfo2String(List<ItemInfo> infoList){
        this.itemsInfo =  JSON.toJSONString(infoList);
    }

    public List<ItemInfo> getItemInfoFromString(){
        return Objects.isNull(itemsInfo)?null:JSON.parseArray(itemsInfo,ItemInfo.class);
    }





    public OrderVo convert2Vo(){
        return OrderVo.builder().orderId(this.orderId)
                .orderNo(this.orderNo)
                .orderStatusInfo(this.orderStatus.getStatusInfo())
                .orderItems(this.getItemInfoFromString())
                .money(this.money)
                .userName(this.userName)
                .creatTime(this.createTime)
                .updateTime(this.updateTime)
                .build();
    }

    public static Order convertFromVo(OrderVo vo){
        return Order.builder().orderId(vo.getOrderId())
                .orderNo(vo.getOrderNo())
                .orderStatus(OrderStatus.find(vo.getOrderStatusInfo()))
                .money(vo.getMoney())
                .userName(vo.getUserName())
                .itemsInfo(Objects.isNull(vo.getOrderItems())?null:JSON.toJSONString(vo.getOrderItems()))
                .createTime(vo.getCreatTime())
                .updateTime(vo.getUpdateTime())
                .build();
    }





}
