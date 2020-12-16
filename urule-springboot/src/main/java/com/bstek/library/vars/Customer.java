package com.bstek.library.vars;

import com.bstek.urule.model.Label;
import lombok.Data;

import java.util.Date;
@Data
public class Customer {
    @Label("名称")
    private String name;
    @Label("年龄")
    private int age;
    @Label("出生日期")
    private Date birthday;
    @Label("等级")
    private int level;
    @Label("手机号")
    private String mobile;
    @Label("性别")
    private boolean gender;
    @Label("是否有车")
    private boolean car;
    @Label("婚否")
    private boolean married;
    @Label("是否有房")
    private boolean house;
}
