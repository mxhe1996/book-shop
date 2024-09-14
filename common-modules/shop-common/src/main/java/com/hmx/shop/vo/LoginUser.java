package com.hmx.shop.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginUser {

    private Long id;

    private String userName;

    private String password;

    private String email;

    private List<String> roles;

}
