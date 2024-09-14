package com.hmx.search.domain;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.hmx.es.domain.BasicEsObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown=true)
public class OrderDocument implements BasicEsObject {


    private Long orderId;

    private String orderNo;

    private Long userId;

    private List<Item> itemsList;

    private Integer money;

    private String userName;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    private String orderStatus;

    private String deliveryNo;

    private Long deliveryId;

    private List<DeliveryPhase> deliveryPhases;


    @Override
    public String getIndexName() {
        return "order";
    }

    @Override
    public String getObjectId() {
        return orderId.toString();
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class Item implements Serializable {

        private Long itemId;

        private String itemName;

        private Integer tenantId;

        private String tenantName;

        private Integer orderNumber;

        private Integer price;
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class DeliveryPhase implements Serializable{
        private Date phaseTime;

        private String comment;
    }


    public static OrderDocument map2OrderDocument(Map<String, Object> body) throws ParseException {
        OrderDocument orderDocument = new OrderDocument();
        orderDocument.setOrderId(Long.parseLong(body.getOrDefault("orderId",-1).toString()));
        orderDocument.setOrderNo(body.getOrDefault("orderNo",-1).toString());
        orderDocument.setUserId(Long.parseLong(body.getOrDefault("userId",-1).toString()));
        orderDocument.setUserName(body.getOrDefault("userName","").toString());
        orderDocument.setMoney(Integer.parseInt(body.getOrDefault("money","0").toString()));
        orderDocument.setOrderStatus(body.getOrDefault("orderStatus","undefined").toString());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (body.get("createTime") != null) {
            String formatterTime = JSONObject.toJSONString(body.get("createTime")).replace("\"", "");
            Date createTime = simpleDateFormat.parse(formatterTime);
            orderDocument.setCreateTime(LocalDateTime.ofInstant(createTime.toInstant(), ZoneId.systemDefault()));
        }else {
            orderDocument.setCreateTime(LocalDateTime.now());
        }
        if (body.get("updateTime") != null) {
            String formatterTime = JSONObject.toJSONString(body.get("updateTime")).replace("\"", "");
            Date updateTime = simpleDateFormat.parse(formatterTime);
            orderDocument.setUpdateTime(LocalDateTime.ofInstant(updateTime.toInstant(),ZoneId.systemDefault()));
        }
        orderDocument.setDeliveryId(Long.parseLong(body.getOrDefault("deliveryId",-1).toString()));
        orderDocument.setDeliveryNo(body.getOrDefault("deliveryNo","").toString());
        orderDocument.setDeliveryPhases(JSONObject.parseObject(body.getOrDefault("deliveryPhases","").toString(),List.class));
        return orderDocument;
    }



}