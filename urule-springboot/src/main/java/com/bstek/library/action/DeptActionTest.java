package com.bstek.library.action;

import com.bstek.library.vars.Dept;
import com.bstek.urule.action.ActionId;
import com.bstek.urule.model.ExposeAction;

public class DeptActionTest {
    @ActionId("deptAction")
    public String deptAction(){
        return "deptAction";
    }
    @ExposeAction(value="是否有部门经理",parameters={"经理名"})
    public boolean hasDeptManage(String deptManager){
        if(deptManager==null || deptManager.isEmpty()){
            return false;
        }else {
            return true;
        }
    }

    @ExposeAction(value="打印部门信息",parameters={"部门信息"})
    public void printDept(Dept m){
        System.out.println("Dept："+ m.toString());
    }
}
