package com.hongdeyan.constant;

import lombok.Getter;

@Getter
public enum RespondStatus {
    LOGIN_SUCCESS(100, "登录成功"),
    LOGIN_FAIL(101, "登录失败"),
    REGISTER_SUCCESS(102, "注册成功"),
    REGISTER_FAIL(103, "注册失败"),
    CODE_FAIL(102, "验证码错误"),
    QUERY_SUCESS(200, "查询成功"),
    QUERY_FAIL(201, "查询失败"),
    AUTH_FAIL(301, "验证失败"),
    AUTH_SUCCESS(300, "验证成功"),
    GET_RSA(400, "获取RSA公钥");

    private int code;
    private String message;

    private RespondStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 通过code获取相应的message
     *
     * @param code
     * @return
     */
    public String getMessage(int code) {
        for (RespondStatus c : RespondStatus.values()) {
            if (c.getCode() == code) {
                return c.message;
            }
        }
        return null;
    }
}
