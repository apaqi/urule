package com.bstek.client;

import com.bstek.client.action.Action;
import com.bstek.client.action.ExecuteMethodAction;
import com.bstek.client.action.VariableAssignAction;
import com.bstek.client.libary.Datatype;
import com.bstek.client.libary.VariableValue;
import com.bstek.client.rule.Op;
import com.bstek.client.rule.Parameter;
import com.bstek.client.rule.SimpleValue;
import com.bstek.client.rule.lhs.Criteria;
import com.bstek.client.rule.lhs.Left;
import com.bstek.client.rule.lhs.LeftType;
import com.bstek.client.rule.lhs.MethodLeftPart;

import java.util.List;

/**
 *
 * @author wpx
 * @Description 类注释
 * @date 2020/12/23
 */
public class Utils {

    /**
     * 构建参数信息
     * @param beanId
     * @param methodId
     * @param parameters
     * @return
     */
    public static Criteria buildCriteria(String beanId, String methodId, Boolean hopeVaule, Parameter... parameters) {
        Criteria criteria = new Criteria();
        Left left = new Left();
        MethodLeftPart leftPart = buildMethodLeftPart(beanId, methodId, parameters);
        left.leftPart(leftPart);
        left.type(LeftType.method);
        criteria.left(left);
        criteria.op(Op.Equals);
        SimpleValue simpleValue = new SimpleValue();
        simpleValue.content(hopeVaule == null? "true" : hopeVaule.toString());
        criteria.value(simpleValue);
        return criteria;
    }

    /**
     * 构建方法左侧部分
     * @param beanId
     * @param methodId
     * @param parameters
     * @return
     */
    public static MethodLeftPart buildMethodLeftPart(String beanId, String methodId, Parameter... parameters) {
        MethodLeftPart leftPart = new MethodLeftPart();
        leftPart.setBeanId(beanId);
        leftPart.setMethodName(methodId);
        for (int i = 0; i < parameters.length; i++) {
            leftPart.addParameter(parameters[i]);
        }
        return leftPart;
    }

    /**
     * 构建通用参数
     * @param parName
     * @param type
     * @param varName
     * @return
     */
    public static Parameter buildParameter(String parName, Datatype type, String varName) {
        Parameter parameter = new Parameter();
        parameter.setName(parName);
        parameter.setType(type);
        VariableValue variableValue = new VariableValue();
        variableValue.setVariableName(varName);
        variableValue.setDatatype(type);
        variableValue.setVariableCategory("参数");
        parameter.setValue(variableValue);
        return parameter;
    }

    /**
     * 构建简捷参数
     * @param parName
     * @param type
     * @param content
     * @return
     */
    public static Parameter buildSimpleParameter(String parName, Datatype type, String content) {
        Parameter parameter = new Parameter();
        parameter.setName(parName);
        parameter.setType(type);
        SimpleValue simpleValue = new SimpleValue();
        simpleValue.content(content);
        parameter.setValue(simpleValue);
        return parameter;
    }

    /**
     * 构建变量参数
     * @param parName
     * @param type
     * @param varName
     * @return
     */
    public static Parameter buildVariableParameter(String parName, Datatype type, String varName) {
        Parameter parameter = new Parameter();
        parameter.setName(parName);
        parameter.setType(type);
        VariableValue variableValue = new VariableValue();
        variableValue.setVariableName(varName);
        variableValue.setDatatype(type);
        variableValue.setVariableCategory("参数");
        parameter.setValue(variableValue);
        return parameter;
    }


    /**
     * 构建统一规则结尾信息
     * @param flag
     * @param datatype
     * @param content
     * @return
     */
    public static Action buildVariableAssignAction(String flag, Datatype datatype, String content) {
        VariableAssignAction variableAssignAction = new VariableAssignAction();
        variableAssignAction.setDatatype(datatype);
        variableAssignAction.setType(LeftType.parameter);
        variableAssignAction.setVariableName(flag);
        SimpleValue value = new SimpleValue();
        value.content(content);
        variableAssignAction.setValue(value);
        return variableAssignAction;
    }

    /**
     * 构建Methonaction
     * @param beanId
     * @param methodName
     * @param paramList
     * @return
     */
    public static ExecuteMethodAction buildMethodAction(String beanId, String methodName, List<Parameter> paramList) {
        ExecuteMethodAction methodAction = new ExecuteMethodAction();
        methodAction.setBeanId(beanId);
        methodAction.setMethodName(methodName);
        methodAction.setParameters(paramList);
        return methodAction;
    }
}
