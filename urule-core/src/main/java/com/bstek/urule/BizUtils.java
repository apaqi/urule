package com.bstek.urule;

import com.bstek.urule.action.Action;
import com.bstek.urule.action.ExecuteMethodAction;
import com.bstek.urule.action.VariableAssignAction;
import com.bstek.urule.model.library.Datatype;
import com.bstek.urule.model.rule.*;
import com.bstek.urule.model.rule.lhs.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wpx
 * @Description 类注释
 * @date 2020/12/23
 */
public class BizUtils {

    /**
     * 构建方法信息
     *
     * @param beanId       接口beanId
     * @param methodId     方法签名
     * @param desiredValue 期望值
     * @param parameters   参数列表
     * @return
     */
    public static Criteria buildMethodLeftCriteria(String beanId, String methodId, Boolean desiredValue, Parameter... parameters) {
        Criteria criteria = new Criteria();
        Left left = new Left();
        MethodLeftPart leftPart = buildMethodLeftPart(beanId, methodId, parameters);
        left.setLeftPart(leftPart);
        left.setType(LeftType.method);
        criteria.setLeft(left);
        criteria.setOp(Op.Equals);
        SimpleValue simpleValue = new SimpleValue();
        simpleValue.setContent(desiredValue == null ? "true" : desiredValue.toString());
        criteria.setValue(simpleValue);
        return criteria;
    }


    /**
     * 构建参数信息
     *
     * @param parameter
     * @return
     */
    public static Criteria buildVariableLeftCriteria(Op op, Parameter parameter) {
        Criteria criteria = new Criteria();
        Left left = new Left();
        VariableLeftPart leftPart = buildVariableLeftPart(parameter);
        left.setLeftPart(leftPart);
        left.setType(LeftType.parameter);
        criteria.setLeft(left);
        criteria.setOp(op);
        criteria.setValue(parameter.getValue());
        return criteria;
    }

    /**
     * 构建方法左侧部分
     *
     * @param beanId
     * @param methodId
     * @param parameters
     * @return
     */
    public static MethodLeftPart buildMethodLeftPart(String beanId, String methodId, Parameter... parameters) {
        MethodLeftPart leftPart = new MethodLeftPart();
        leftPart.setBeanId(beanId);
        leftPart.setBeanLabel(beanId);
        leftPart.setMethodName(methodId);
        leftPart.setMethodLabel(methodId);
        List<Parameter> parameterList = new ArrayList<>();
        for (int i = 0; i < parameters.length; i++) {
            parameterList.add(parameters[i]);
        }
        leftPart.setParameters(parameterList);
        return leftPart;
    }


    /**
     * 构建方法左侧部分
     *
     * @param parameter
     * @return
     */
    public static VariableLeftPart buildVariableLeftPart(Parameter parameter) {
        VariableLeftPart leftPart = new VariableLeftPart();
        leftPart.setDatatype(parameter.getType());
        leftPart.setVariableCategory(parameter.getName());
        leftPart.setVariableLabel(parameter.getName());
        leftPart.setVariableName(parameter.getName());
        return leftPart;
    }

    /**
     * 构建简捷参数
     *
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
        simpleValue.setContent(content);
        parameter.setValue(simpleValue);
        return parameter;
    }

    /**
     * 构建变量参数
     *
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
     * 构建变量参数
     *
     * @param parName
     * @param type
     * @param obj
     * @return
     */
    public static Parameter buildComplexObjectValueParameter(String parName, Datatype type, Object obj) {
        Parameter parameter = new Parameter();
        parameter.setName(parName);
        parameter.setType(type);
        ComplexObjectValue complexObjectValue = new ComplexObjectValue();
        complexObjectValue.setContent(obj);
        parameter.setValue(complexObjectValue);
        return parameter;
    }


    /**
     * 构建统一规则结尾信息
     *
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
        value.setContent(content);
        variableAssignAction.setValue(value);
        return variableAssignAction;
    }

    /**
     * 构建Methonaction
     *
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
