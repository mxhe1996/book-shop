package com.hmx.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hmx.order.domain.vo.OrderVo;
import com.hmx.order.service.OrderService;
import com.hmx.shop.domain.CommonResponseBody;
import com.hmx.shop.utils.ResponseUtils;
import com.hmx.shop.utils.ServletUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    private final Executor asyncSecurityContextExecutor;

    @GetMapping("/query")
    public CommonResponseBody<IPage<OrderVo>> queryOrderWithCondition(OrderVo vo){
        if (Objects.isNull(vo.getPageNum())){
            vo.setPageNum(0);
        }
        if (Objects.isNull(vo.getPageSize())){
            vo.setPageSize(10);
        }
        return ResponseUtils.toResponse(orderService.queryOrderWithCondition(vo));
    }

    @PostMapping("/create")
    public CompletableFuture<CommonResponseBody> createOrder(@RequestBody OrderVo orderVo){
        ServletRequestAttributes requestAttributes = ServletUtils.getRequestAttributes();
        return CompletableFuture
                .supplyAsync(
                        () -> {
                            // getRequestAttributes为ThreadLocal,异步导致丢失
                            RequestContextHolder.setRequestAttributes(requestAttributes);
                            return orderService.createOrder(orderVo.getOrderItems());
                        },asyncSecurityContextExecutor)
                .handle(((rowInsertedNum, throwable) -> {
                    if (Objects.isNull(throwable) && rowInsertedNum == 1) {
                        return ResponseUtils.successResponse();
                    } else {
                        log.error(throwable.getMessage());
                        return ResponseUtils.errorResponse(Objects.isNull(throwable) ? "创建订单异常" : throwable.getMessage());
                    }
                }));

    }

}
