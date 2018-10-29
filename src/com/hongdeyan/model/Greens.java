package com.hongdeyan.model;

import com.hongdeyan.annotation.Document;
import com.hongdeyan.annotation.Id;
import com.hongdeyan.annotation.Param;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@Document
public class Greens {
    @Id
    private String id;
    //菜品的名称
    @Param
    private String name;
    //菜品的描述
    @Param
    private String desc;
    //菜品的图片
    @Param(param_name ="pic_url")
    private String pic;
    //销售的价格
    @Param
    private double money;
    //计算销售的数量
    @Param
    private int saled;
}
