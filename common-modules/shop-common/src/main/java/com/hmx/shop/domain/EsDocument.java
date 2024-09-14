package com.hmx.shop.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EsDocument<T> {

    private String index;

    private String id;

    private T document;

}
