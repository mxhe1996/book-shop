package com.hmx.shop.advice;

import com.hmx.shop.domain.CommonResponseBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionAdvice {

    @ExceptionHandler(value = Exception.class)
    public CommonResponseBody<Object> handlerMallException(HttpServletRequest request, Exception exception){
        log.error("检测到异常结果返回:{}",exception.getMessage());
        CommonResponseBody<Object> responseBody = new CommonResponseBody<>();
        responseBody.setCode(-1);
        responseBody.setMsg(exception.getMessage());
        return responseBody;
    }


}
