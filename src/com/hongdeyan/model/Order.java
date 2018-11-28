package com.hongdeyan.model;

import com.hongdeyan.annotation.Document;
import com.hongdeyan.annotation.Id;
import com.hongdeyan.annotation.Param;
import lombok.Data;


@Data
@Document
public class Order {
    @Id
    private String id;

    //购买者是谁(name)
    @Param
    private String buyer;

    //收货地址
    @Param
    private String address;

    //电话号码
    @Param
    private String phone;

    //记录购买的菜品
    @Param
    private String buys;

    //是否正在烹饪
    @Param
    private boolean isCook;

    //是否已经发货
    @Param
    private boolean isSend;

    @Param
    private double sum;

    public Order() {
    }
}
