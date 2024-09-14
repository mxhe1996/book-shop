package com.hmx.shop.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonResponseBody<T> {

    private Integer code;

    private String msg;

    private T data;

    public CommonResponseBody(T data){
        this.data = data;
        this.code = 200;
        this.msg = "success";
    }

}
