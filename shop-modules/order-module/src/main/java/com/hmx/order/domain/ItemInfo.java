package com.hmx.order.domain;

import lombok.Data;

@Data
public class ItemInfo{
    private Long itemId;

    private String itemName;

    private Integer tenantId;

    private String tenantName;

    private Integer orderNumber;

    private Integer price;

}
