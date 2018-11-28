package com.hongdeyan.constant;

import lombok.Getter;

@Getter
public enum RequestStatus {
    SEND_RSA(100, "发送RSA公钥"),
    LOGIN(200, "发送登录"),
    REGISTER(201, "注册"),
    FIND_ALL_USER(301, "获取所有用户"),
    UPDATE_USER(302, "修改用户"),
    REMOVE_USER(301, "删除用户"),
    FIND_ALL_GREENS(401, "获取所有菜品"),
    ADD_GREENS(402, "添加菜品"),
    UPDATE_GREENS(403, "修改菜品"),
    REMOVE_GREENS(404, "删除菜品"),
    FIND_ALL_ORDERS(501, "查找所有订单"),
    ADD_ORDERS(502, "添加订单"),
    REMOVE_ORDERS(503, "取消订单"),
    UPDATE_ORDERS(504, "修改订单"),
    COOKIE_OK(601,"烹饪完成"),
    SEND_GOODS(602,"送货完成");



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
