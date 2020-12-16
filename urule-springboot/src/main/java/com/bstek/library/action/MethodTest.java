package com.bstek.library.action;

import com.bstek.library.vars.Customer;
import com.bstek.urule.action.ActionId;
import com.bstek.urule.model.ExposeAction;

import java.text.SimpleDateFormat;
import java.util.Date;
public class MethodTest {
    @ActionId("Hello")
    public String hello(){
        return "hello";
    }
    @ExposeAction(value="方法1",parameters={"用户名"})
    public boolean evalTest(String username){
        if(username==null){
            return false;
        }else if(username.equals("张三")){
            return true;
        }
        return false;
    }

    @ExposeAction(value="测试Int",parameters={"数字1","数字2"})
    public int testInt(int a,int b){
        return a+b;
    }
    public int testInteger(Integer a,int b){
        return a+b*10;
    }

    @ExposeAction(value="打印内容",parameters={"用户名","出生日期"})
    public void printContent(String username, Date birthday){
        SimpleDateFormat sd=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if(birthday!=null){
            System.out.println(username+"今年已经"+sd.format(birthday)+"岁了!");
        }else{
            System.out.println("Hello "+username+"");
        }
    }

    @ExposeAction(value="打印Customer",parameters={"Customer对象"})
    public void printUser(Customer m){
        System.out.println("Hello "+m.getName()+", has house:"+m.isHouse());
    }
}
