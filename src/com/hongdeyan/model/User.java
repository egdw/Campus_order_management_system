package com.hongdeyan.model;

import com.hongdeyan.annotation.DbRef;
import com.hongdeyan.annotation.Document;
import com.hongdeyan.annotation.Id;
import com.hongdeyan.annotation.Param;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Document(doucument_name = "user")
public class User {
    @Id
    private String id;
    //用户名
    @Param
    private String username;
    //密码
    @Param
    private String password;
    //职务
    @DbRef(param_name = "duty")
    private Duty duty;
}
