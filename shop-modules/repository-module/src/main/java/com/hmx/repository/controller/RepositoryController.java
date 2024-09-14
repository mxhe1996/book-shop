package com.hmx.repository.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmx.redis.utils.SecurityUtils;
import com.hmx.repository.domain.vo.BookVo;
import com.hmx.repository.service.IRepositoryService;
import com.hmx.repository.service.impl.RepositoryTenantServiceImpl;
import com.hmx.repository.service.impl.RepositoryUserServiceImpl;
import com.hmx.shop.domain.CommonResponseBody;
import com.hmx.shop.utils.ResponseUtils;
import com.hmx.shop.vo.LoginUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/repository")
public class RepositoryController {

    private final RepositoryUserServiceImpl repositoryUserService;

    private final RepositoryTenantServiceImpl repositoryTenantService;

    private final List<IRepositoryService> repositoryServices;

    public RepositoryController(RepositoryUserServiceImpl repositoryUserService, RepositoryTenantServiceImpl repositoryTenantService) {
        this.repositoryUserService = repositoryUserService;
        this.repositoryTenantService = repositoryTenantService;
        repositoryServices = new ArrayList<>();
        repositoryServices.add(this.repositoryTenantService);
        repositoryServices.add(this.repositoryUserService);
    }

    @GetMapping("/query")
    public CommonResponseBody<Page>  queryBookRepository(BookVo bookVo){

        // init
        if (Objects.isNull(bookVo.getPageSize())){
            bookVo.setPageSize(10);
        }
        if (Objects.isNull(bookVo.getPageNumber())){
            bookVo.setPageNumber(0);
        }

        // todo 获取用户权限
        LoginUser currentUser = SecurityUtils.getCurrentUser();
        List<String> userRoles = currentUser.getRoles();
        for (IRepositoryService repositoryService : repositoryServices) {
            Optional optional = repositoryService.requiredRole().stream().filter(userRoles::contains).findAny();
            if (optional.isPresent()){
                return ResponseUtils.toResponse(repositoryService.queryBookWithCondition(bookVo));
            }
        }

        return ResponseUtils.errorResponse("服务器内部异常");
    }

    @GetMapping("/reduce")
    public CommonResponseBody<Integer> reduceBookStock(Long bookId, Integer num){
        BookVo bookVo = BookVo.builder().bookId(bookId).build();
        return ResponseUtils.toRow(repositoryTenantService.reduceBookStockNumber(bookVo,num));
    }





}
