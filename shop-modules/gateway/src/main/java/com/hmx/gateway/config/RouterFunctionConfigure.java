package com.hmx.gateway.config;

import com.hmx.gateway.handler.LoginHandler;
import com.hmx.gateway.handler.LogoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.*;

@Configuration
public class RouterFunctionConfigure {

    private final LoginHandler loginHandler;

    private final LogoutHandler logoutHandler;

    public RouterFunctionConfigure(LoginHandler loginHandler, LogoutHandler logoutHandler) {
        this.loginHandler = loginHandler;
        this.logoutHandler = logoutHandler;
    }

    @Bean
    public RouterFunction<ServerResponse> loginRouter(){
        return RouterFunctions
                .route(RequestPredicates.GET("/auth/login/tenant"),loginHandler::handleTenant)
                .andRoute(RequestPredicates.GET("/auth/login/common"),loginHandler::handleCommon)
                .andRoute(RequestPredicates.GET("/auth/logout"),logoutHandler);
    }

}
