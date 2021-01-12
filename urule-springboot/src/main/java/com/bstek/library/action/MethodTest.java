package com.bstek.library.action;

import com.alibaba.fastjson.JSON;
import com.bstek.library.vars.Customer;
import com.bstek.library.vars.Dept;
import com.bstek.urule.action.ActionId;
import com.bstek.urule.console.DefaultUser;
import com.bstek.urule.model.ExposeAction;
import com.bstek.urule.model.rete.JsonUtils;
import org.apache.commons.collections.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.*;

public class MethodTest {
    //@ActionId("Hello")
    public String hello(String hello){
        System.out.println("#####################################" + hello);
        if("hello".equals(hello)) {
            return "hello";
        }else if("exception".equals(hello)){
            throw new RuntimeException("exception，异常了！");
        }else {
            return "falseStr";
        }
    }
    //@ExposeAction(value="方法1",parameters={"用户名"})
    public boolean evalTest(String username){
        System.out.println("#####################################" + username);
        if(username==null){
            return false;
        }else if(username.equals("张三")){
            return true;
        }else {
            throw new RuntimeException("不是张三，异常了！");
        }
    }

    //@ExposeAction(value="测试Int",parameters={"数字1","数字2"})
    public int testInt(int a,int b){
        return a+b;
    }
    public int testInteger(Integer a,int b){
        return a+b*10;
    }

    //@ExposeAction(value="打印内容",parameters={"用户名","出生日期"})
    public void printContent(String username, Date birthday){
        SimpleDateFormat sd=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if(birthday!=null){
            System.out.println(username+"今年已经"+sd.format(birthday)+"岁了!");
        }else{
            System.out.println("Hello "+username+"");
        }
    }

    //@ExposeAction(value="打印Customer",parameters={"Customer对象"})
    public void printUser(Customer m){
        System.out.println("Hello "+m.getName()+", has house:"+m.isHouse());
    }

    public String printUsers(List<String> customers, Map<String, DefaultUser> maps){
        if (CollectionUtils.isNotEmpty(customers)) {
            customers.forEach(m->{
                System.out.println("Hello "+m);
            });
        }
        for(Map.Entry<String, DefaultUser> entry :  maps.entrySet()) {
            System.out.println("key="+entry.getKey()+"###CompanyId="+entry.getValue().getCompanyId());
        }
        Map<String,String> map = new HashMap<String,String>(){{
            put("tag_var_0","3456");
        }};

        return JSON.toJSONString(map);
    }

    public boolean ltZero(int a,int b){
        return a/b >1;
    }

    public List<String> containsTest(String users){
        List<String> list = Arrays.asList("12","32","432","45");
        return list;
    }
}
