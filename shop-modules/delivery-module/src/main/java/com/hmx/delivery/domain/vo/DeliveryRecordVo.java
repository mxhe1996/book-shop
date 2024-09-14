package com.hmx.delivery.domain.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.hmx.delivery.domain.DeliveryRecord;
import com.hmx.delivery.domain.PhaseDetail;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class DeliveryRecordVo {

    private Long deliveryId;

    // 订单编号
    private String deliveryNo;

    private Long orderId;

    private String detail;

    private Date createTime;

    private List<PhaseDetail> phaseDetailList;
}
