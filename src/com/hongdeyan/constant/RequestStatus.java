package com.hongdeyan.constant;

import lombok.Getter;

@Getter
public enum RequestStatus {
    SEND_RSA(100, "发送RSA公钥"), LOGIN(200, "发送登录"),

    REGISTER(201, "注册");

    private int code;
    private String message;

    private RequestStatus(int code, String message) {
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
        for (RequestStatus c : RequestStatus.values()) {
            if (c.getCode() == code) {
                return c.message;
            }
        }
        return null;
    }
}
