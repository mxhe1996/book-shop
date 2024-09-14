package com.hmx.order.domain.vo;

import com.hmx.order.domain.ItemInfo;
import com.hmx.order.domain.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderVo {

    private Long orderId;

    private String orderNo;

//    private Long userId;
    private String orderStatusInfo;

    private String userName;

    private List<ItemInfo> orderItems;

    private Integer money;

    private Date creatTime;

    private Date updateTime;

    private Integer pageSize;

    private Integer pageNum;



}
