package com.hmx.repository.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmx.repository.domain.Book;
import com.hmx.repository.domain.vo.BookVo;
import com.hmx.repository.mapper.BookMapper;
import com.hmx.repository.service.IRepositoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RepositoryUserServiceImpl implements IRepositoryService<BookVo> {

    private final BookMapper bookMapper;

    @Override
    public List<String> requiredRole() {
        return Arrays.asList("visitor","common");
    }

    /**
     * 返回
     * @param bookVo
     * @return
     */
    @Override
    public Page<BookVo> queryBookWithCondition(BookVo bookVo){
        // 用户可以查看全部物品
        bookVo.setTenantId(null);
        Page<Book> bookPage = bookMapper.selectPage(new Page<>(bookVo.getPageNumber(), bookVo.getPageSize())
                , new QueryWrapper<Book>(Book.convert2Ori(bookVo)));
        Page<BookVo> bookVoIPage = (Page<BookVo>)bookPage.convert(Book::convert2Vo);
        return bookVoIPage;
    }


    public int reduceBookStockNumber(BookVo bookVo, int orderNumber){
        return bookMapper.reduceStockNumber(bookVo.getBookId(), orderNumber);
    }

    @Override
    public BookVo queryBook(Long id){
        return bookMapper.selectById(id).convert2Vo();
    }




}
