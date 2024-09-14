package com.hmx.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hmx.repository.domain.Book;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface BookMapper extends BaseMapper<Book> {

    @Update("update book a inner join (select book_id, stock from book where book_id = #{bookId} and stock >= #{orderNumber}) b using (book_id)  set a.stock = b.stock-#{orderNumber}")
    public int reduceStockNumber(@Param("bookId") long bookId, @Param("orderNumber") int orderNumber);


}
