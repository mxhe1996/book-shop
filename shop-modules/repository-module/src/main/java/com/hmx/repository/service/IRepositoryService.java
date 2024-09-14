package com.hmx.repository.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmx.repository.domain.vo.BookVo;

import java.util.List;

public interface IRepositoryService<T> {


    public List<String> requiredRole();

    public Page<T> queryBookWithCondition(BookVo bookVo);

    public T queryBook(Long id);



}
