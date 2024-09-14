package com.hmx.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hmx.order.domain.Order;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface OrderMapper extends BaseMapper<Order> {
    @Update(value = "update order_info set order_status = #{orderStatusCode} where order_id = #{orderId}")
    public int updateStatus(@Param("orderId") Long orderId, @Param("orderStatusCode") Integer orderStatusCode);

}
