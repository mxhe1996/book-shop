package com.hmx.kafka.domain;

import com.alibaba.fastjson2.JSONObject;
import com.hmx.shop.domain.BasicBody;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Deprecated
public class EventBody<T>{


    private T body;

    private MetaHeader header;

    public void parseAndSetHeader(String headerJson){
        MetaHeader metaHeader = JSONObject.parseObject(headerJson, MetaHeader.class);
        this.header = metaHeader;
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MetaHeader{

        // 发送的消息Id
        private String messageId;

        // 请求返回的地址/通道
        private String addressId;

    }


}
