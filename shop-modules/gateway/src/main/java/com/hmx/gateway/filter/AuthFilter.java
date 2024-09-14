package com.hmx.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.hmx.gateway.config.IgnoreListConfigure;
import com.hmx.gateway.utils.ResponsibilityChain;
import com.hmx.shop.domain.CommonConstants;
import com.hmx.shop.utils.JwtUtils;
import com.hmx.shop.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 用户过滤
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AuthFilter implements GlobalFilter, Ordered {


    private final RedisTemplate<String,String> redisTemplate;

    private final IgnoreListConfigure ignoreListConfigure;

    @Value("${open-auth-check:false}")
    private Boolean openAuthCheck;


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        try{
            ResponsibilityChain<HttpHeaders> headersResponsibilityChain = generateAuthChain();
            if (!ignoreListConfigure.isIgnorePath(path)&&openAuthCheck){
                headersResponsibilityChain.exec(request.getHeaders());
                // 传播到下游微服务时，header重新传入payload
                String jwt = redisTemplate.opsForValue().get(CommonConstants.LOGIN_TOKEN_KEY + request.getHeaders().getFirst(CommonConstants.AUTH_HEADER).replace(CommonConstants.TOKEN_PREFIX, ""));
                ServerHttpRequest httpRequest = exchange.getRequest().mutate().header(CommonConstants.USER_INFO, JwtUtils.parseTokenPayload(jwt)).build();
                exchange = exchange.mutate().request(httpRequest).build();
            }
            return chain.filter(exchange);
        }catch (Exception e){
            log.info("用户身份验证失败:{}",e.getMessage());
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.OK);
            return exchange.getResponse().writeWith(Mono.fromSupplier(()->{
                DataBufferFactory bufferFactory = response.bufferFactory();
                return bufferFactory.wrap(JSON.toJSONBytes(ResponseUtils.errorResponse(e.getMessage())));
            }));
        }

    }

    @Override
    public int getOrder() {
        return 0;
    }


    public ResponsibilityChain<HttpHeaders> generateAuthChain(){
        ResponsibilityChain<HttpHeaders> authChain = new ResponsibilityChain<>("缺乏认证信息",headers -> headers.containsKey(CommonConstants.AUTH_HEADER)&& StringUtils.isNotEmpty(headers.getFirst(CommonConstants.AUTH_HEADER)));

        ResponsibilityChain<HttpHeaders> tokenChain = new ResponsibilityChain<>("令牌为空", headers -> {
            String token = headers.getFirst(CommonConstants.AUTH_HEADER);
            token = token.replace(CommonConstants.TOKEN_PREFIX, Strings.EMPTY);
            return StringUtils.isNotEmpty(token);
        });
        authChain.setNextNode(tokenChain);

        ResponsibilityChain<HttpHeaders> statusChain = new ResponsibilityChain<>("登录状态异常", headers -> {
            String token = headers.getFirst(CommonConstants.AUTH_HEADER);
            token = token.replace(CommonConstants.TOKEN_PREFIX, Strings.EMPTY);
            return redisTemplate.hasKey(CommonConstants.LOGIN_TOKEN_KEY + token);
        });
        tokenChain.setNextNode(statusChain);

        ResponsibilityChain<HttpHeaders> userInfoChain = new ResponsibilityChain<>("令牌验证失败", headers -> {
            String token = headers.getFirst(CommonConstants.AUTH_HEADER);
            token = token.replace(CommonConstants.TOKEN_PREFIX, Strings.EMPTY);
            String jwToken = redisTemplate.opsForValue().get(CommonConstants.LOGIN_TOKEN_KEY + token);
            try{
                return JwtUtils.verifyToken(jwToken);
            }catch (Exception e){
                return false;
            }
        });
        statusChain.setNextNode(userInfoChain);


        return authChain;
    }

}
