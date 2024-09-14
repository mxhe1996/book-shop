package com.hmx.shop.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录表单
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginForm {

    private String userName;

    private String password;
}
