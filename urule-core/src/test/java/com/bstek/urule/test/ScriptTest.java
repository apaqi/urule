package com.bstek.urule.test;


import com.alibaba.fastjson.JSON;
import com.bstek.urule.script.JsonPathAction;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class ScriptTest {
    @Test
    public void dd(){
        JsonPathAction jsonPathAction = new JsonPathAction();
        Map<String,String> stringMap = new HashMap<>();
        stringMap.put("d","bs,dsa,grtsd");

        jsonPathAction.setExpression("$.d");
        Object invoke = jsonPathAction.actionInvoker().invoke(JSON.toJSON(stringMap));
        System.out.println(invoke);
    }
}
