package com.hongdeyan.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class User {
    //用户名
    private String username;
    //密码
    private String password;
    //职务
    private Duty duty;
}
