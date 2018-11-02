package com.hongdeyan.message_model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Request {
    //标记当前的事情
    private int code;
    //标记当前的唯一身份
    private String uuid;
    //需要传递的任意数据
    private String message;

    public Request() {
    }

    public Request(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public Request(int code, String uuid, String message) {
        this.code = code;
        this.uuid = uuid;
        this.message = message;
    }
}
