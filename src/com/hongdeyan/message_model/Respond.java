package com.hongdeyan.message_model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Respond {
    //返回的事件状态
    private int code;
    //返回的数据
    private String message;
}
