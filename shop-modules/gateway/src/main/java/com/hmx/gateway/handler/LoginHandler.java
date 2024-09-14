package com.hmx.gateway.handler;

import com.hmx.shop.domain.CommonConstants;
import com.hmx.shop.utils.JwtUtils;
import com.hmx.shop.vo.LoginUser;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

@Component
public class LoginHandler  {

    private final RedisTemplate<String,String> redisTemplate;

    public LoginHandler(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Mono<ServerResponse> handleTenant(ServerRequest request) {
        // todo  验证用户/密码
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.defer(() ->
                        Mono.fromSupplier(() -> JwtUtils.generateToken(new LoginUser(2L,"tenant","tenant","2222@222.com", Collections.singletonList("tenant"))))
                                .map(jwt->{
                                    if (jwt.startsWith("error")){
                                        return "用户登录异常，"+jwt;
                                    }
                                    String userKey = UUID.randomUUID().toString();
                                    redisTemplate.opsForValue().set(CommonConstants.LOGIN_TOKEN_KEY+userKey,jwt);
                                    redisTemplate.expire(CommonConstants.LOGIN_TOKEN_KEY+userKey, Duration.ofMinutes(20));
                                    return userKey;
                                })
                ), String.class);
    }

    public Mono<ServerResponse> handleCommon(ServerRequest request) {
        // todo  验证用户/密码
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.defer(() ->
                        Mono.fromSupplier(() -> JwtUtils.generateToken(new LoginUser(3L,"user","user","3333@333.com", Collections.singletonList("common"))))
                                .map(jwt->{
                                    if (jwt.startsWith("error")){
                                        return "用户登录异常，"+jwt;
                                    }
                                    String userKey = UUID.randomUUID().toString();
                                    redisTemplate.opsForValue().set(CommonConstants.LOGIN_TOKEN_KEY+userKey,jwt);
                                    redisTemplate.expire(CommonConstants.LOGIN_TOKEN_KEY+userKey, Duration.ofMinutes(20));
                                    return userKey;
                                })
                ), String.class);
    }



}
