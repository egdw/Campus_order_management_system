package com.hongdeyan.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Greens {
    //菜品的名称
    private String name;
    //菜品的描述
    private String desc;
    //菜品的图片
    private String pic;
    //销售的价格
    private double money;
    //计算销售的数量
    private int saled;
}
