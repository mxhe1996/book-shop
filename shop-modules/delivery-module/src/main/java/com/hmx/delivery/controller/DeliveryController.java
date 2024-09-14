package com.hmx.delivery.controller;

import com.hmx.delivery.domain.vo.DeliveryRecordVo;
import com.hmx.delivery.service.DeliveryService;
import com.hmx.shop.domain.CommonResponseBody;
import com.hmx.shop.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/delivery")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping("/add")
    public CompletableFuture<CommonResponseBody> addDeliveryPhase(@RequestBody DeliveryRecordVo deliveryRecordVo){
        return
                deliveryService.insertDeliveryPhase(deliveryRecordVo.getDeliveryId(), deliveryRecordVo.getPhaseDetailList().get(0))
                        .handle(((row, throwable) -> {
                            if (Objects.isNull(throwable) && row == 1) {
                                return ResponseUtils.successResponse();
                            }
                            return ResponseUtils.errorResponse(throwable.getMessage());
                        }));
    }
}
