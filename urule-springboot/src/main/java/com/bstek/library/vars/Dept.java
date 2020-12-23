package com.bstek.library.vars;

import com.bstek.urule.model.Label;
import lombok.Data;
import lombok.ToString;

import java.util.Date;
//com.bstek.library.vars.Dept
@Data
@ToString
public class Dept {
    @Label("部门名称")
    private String deptName;
    @Label("部门经理")
    private String deptManager;
    @Label("部门人员数")
    private Integer deptNum;

}
