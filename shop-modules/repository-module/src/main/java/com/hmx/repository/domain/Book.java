package com.hmx.repository.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hmx.repository.domain.vo.BookVo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@TableName("book")
@AllArgsConstructor
@NoArgsConstructor
public class Book {


    @TableId(value = "book_id", type = IdType.AUTO)
    private Long bookId;

    @TableField("book_name")
    private String bookName;

    @TableField("book_description")
    private String bookDescription;

    @TableField("price")
    private Integer price;

    // 库存数量
    @TableField("stock")
    private Integer stock;

    @TableField("tenant_id")
    private Integer tenantId;

    @TableField("tenant_name")
    private String tenantName;



    public BookVo convert2Vo(){
        return BookVo.builder()
                .bookId(bookId)
                .bookName(bookName)
                .price(price)
                .bookDescription(bookDescription)
                .tenantId(tenantId)
                .tenantName(tenantName)
                .build();
    }

    public static Book convert2Ori(BookVo bookVo){
        return Book.builder()
                .bookId(bookVo.getBookId())
                .bookName(bookVo.getBookName())
                .bookDescription(bookVo.getBookDescription())
                .price(bookVo.getPrice())
                .tenantId(bookVo.getTenantId())
                .tenantName(bookVo.getTenantName())
                .build();
    }

}
