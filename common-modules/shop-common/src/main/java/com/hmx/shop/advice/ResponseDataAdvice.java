package com.hmx.shop.advice;

import com.hmx.shop.annotation.IgnoreResponseAdvice;
import com.hmx.shop.domain.CommonResponseBody;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
//import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Objects;

/**
 * <h1>实现统一响应</h1>
 * */
//@RestControllerAdvice(basePackages = {"com.hmx"})
public class ResponseDataAdvice /*implements ResponseBodyAdvice<Object>*/ {


    /**
     * 返回类型是否支持统一返回格式
     * @param returnType
     * @param converterType
     * @return
     */
//    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {

        Class<?> returnTypeDeclaringClass = returnType.getDeclaringClass();

        if (returnTypeDeclaringClass.isAnnotationPresent(IgnoreResponseAdvice.class)&&returnTypeDeclaringClass.getAnnotation(IgnoreResponseAdvice.class).ignore()){
            return false;
        }
        if (returnType.getMethod().isAnnotationPresent(IgnoreResponseAdvice.class)&&returnType.getMethod().getAnnotation(IgnoreResponseAdvice.class).ignore()){
            return false;
        }

        return true;
    }

//    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        CommonResponseBody<Object> responseBody = new CommonResponseBody<>();
        responseBody.setCode(0);
        responseBody.setMsg("");

        if (Objects.isNull(body)){
            return responseBody;
        }else if (body instanceof CommonResponseBody){
            responseBody = (CommonResponseBody<Object>) body;
        }else {
            responseBody.setData(body);
        }

        return responseBody;
    }
}
