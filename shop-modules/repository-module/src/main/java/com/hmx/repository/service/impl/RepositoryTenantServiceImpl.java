package com.hmx.repository.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmx.redis.utils.SecurityUtils;
import com.hmx.repository.domain.Book;
import com.hmx.repository.domain.vo.BookVo;
import com.hmx.repository.mapper.BookMapper;
import com.hmx.repository.service.IRepositoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RepositoryTenantServiceImpl implements IRepositoryService<Book> {

    private final BookMapper bookMapper;


    @Override
    public List<String> requiredRole() {
        return Arrays.asList("tenant","admin");
    }

    /**
     * 返回
     * @param bookVo
     * @return
     */
    @Override
    public Page<Book> queryBookWithCondition(BookVo bookVo){
        if (!SecurityUtils.isCurrentUserAdmin()){
            // 如果是一般租户 只能看自己的物品
            bookVo.setTenantId(SecurityUtils.getCurrentUser().getId().intValue());
        }
        Page<Book> bookPage = bookMapper.selectPage(new Page<>(bookVo.getPageNumber(), bookVo.getPageSize())
                , new QueryWrapper<Book>(Book.convert2Ori(bookVo)));
        return bookPage;
    }


    public int reduceBookStockNumber(BookVo bookVo, int orderNumber){
        return bookMapper.reduceStockNumber(bookVo.getBookId(), orderNumber);
    }

    @Override
    public Book queryBook(Long id){
        return bookMapper.selectById(id);
    }




}
