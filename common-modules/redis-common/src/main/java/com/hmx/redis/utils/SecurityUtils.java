package com.hmx.redis.utils;

import com.alibaba.fastjson2.JSONObject;
import com.hmx.shop.domain.CommonConstants;
import com.hmx.shop.utils.ServletUtils;
import com.hmx.shop.vo.LoginUser;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.List;

public class SecurityUtils {


    public static LoginUser getCurrentUser(){
        ServletRequestAttributes requestAttributes = ServletUtils.getRequestAttributes();
        String userInfo = requestAttributes.getRequest().getHeader(CommonConstants.USER_INFO);

        if (StringUtils.isEmpty(userInfo)){
            // header中没有用户信息，可能是游客
            return defaultUser();
        }
        return JSONObject.parseObject(userInfo, LoginUser.class);
        // todo 用户缓存获取
    }

    public static List<String> getUserRoles(){
        return getCurrentUser().getRoles();
    }

    public static boolean isCurrentUserAdmin(){
        return getCurrentUser().getId().equals(1L);
    }

    private static LoginUser defaultUser(){
        return new LoginUser(-1L,"visitor","visitor","", Arrays.asList("visitor"));
    }



}
