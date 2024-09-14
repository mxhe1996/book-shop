package com.hmx.repository.domain.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookVo {

    private Long bookId;

    private String bookName;

    private String bookDescription;

    private Integer price;

    private Integer tenantId;


    private Integer pageSize;

    private Integer pageNumber;

    private String tenantName;

}
 