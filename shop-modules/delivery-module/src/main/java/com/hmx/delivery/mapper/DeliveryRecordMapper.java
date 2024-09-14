package com.hmx.delivery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hmx.delivery.domain.DeliveryRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface DeliveryRecordMapper extends BaseMapper<DeliveryRecord> {


    @Update("update delivery_record set phases=#{phases} where delivery_id=#{deliverId}")
    int updateDeliveryPhaseDetail(@Param("deliverId")Long deliveryId, @Param("phases") String phases);

}
