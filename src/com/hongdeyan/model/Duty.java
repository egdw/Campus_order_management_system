package com.hongdeyan.model;

import com.hongdeyan.annotation.Document;
import com.hongdeyan.annotation.Id;
import com.hongdeyan.annotation.Param;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Document
public class Duty {
    //责任名称.比如 厨师 用户....
    @Id
    private String id;
    @Param(param_name = "dutyName")
    private String dutyName;
}
