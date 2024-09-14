package com.hmx.shop.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hmx.shop.annotation.IgnoreResponseAdvice;
import lombok.Data;

import java.util.Date;

@Data
@IgnoreResponseAdvice
public class BasicBody {

    private String createBy;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date createTime;


}
