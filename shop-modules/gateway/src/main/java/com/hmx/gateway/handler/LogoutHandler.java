package com.hmx.gateway.handler;

import com.hmx.shop.domain.CommonConstants;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class LogoutHandler implements HandlerFunction<ServerResponse> {

    private final RedisTemplate<String,String> redisTemplate;

    public LogoutHandler(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<ServerResponse> handle(ServerRequest request) {
        String userKey = request.headers().firstHeader(CommonConstants.AUTH_HEADER);
        userKey = userKey.replace(CommonConstants.TOKEN_PREFIX,"");
        if (StringUtils.isEmpty(userKey)){
            return ServerResponse.badRequest().body("未携带认证信息", String.class);
        }
        String cacheKey = CommonConstants.LOGIN_TOKEN_KEY+userKey;
        return ServerResponse.ok().body(Mono.defer(()->
                Mono.fromSupplier(()-> redisTemplate.delete(cacheKey))
                        .handle((execResult,sink)->{
                            if (!execResult){
                                sink.error(new RuntimeException("用户退出异常"));
                            }
                        })
                        .then(Mono.just("退出成功"))
        ),String.class);
    }
}
