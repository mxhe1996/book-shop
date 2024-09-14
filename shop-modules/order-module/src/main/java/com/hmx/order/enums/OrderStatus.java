package com.hmx.order.enums;

import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;

import java.util.Objects;
import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum OrderStatus implements IEnum<Integer> {

    SUBMITTED(1,"已提交"),
    WAIT_DELIVERY(2,"等待");

    private final Integer statusCode;

    private final String statusInfo;

    public static OrderStatus find(Integer statusCode){
        if (Objects.isNull(statusCode)){
            return null;
        }
        return Stream.of(values())
                .filter(orderStatus -> orderStatus.getStatusCode().equals(statusCode))
                .findFirst()
                .orElseThrow(()->new RuntimeException("没有找到"+statusCode+"对应的状态值") );
    }

    public static OrderStatus find(String statusInfo){
        if (StringUtils.isEmpty(statusInfo)){
            return null;
        }
        return Stream.of(values())
                .filter(orderStatus -> orderStatus.getStatusInfo().equals(statusInfo))
                .findFirst()
                .orElseThrow(()->new RuntimeException("没有找到"+statusInfo+"对应的信息") );
    }


    @Override
    public Integer getValue() {
        return statusCode;
    }
}
