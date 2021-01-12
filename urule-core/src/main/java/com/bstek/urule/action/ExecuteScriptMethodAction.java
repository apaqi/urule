package com.bstek.urule.action;


import com.alibaba.fastjson.JSONObject;
import com.bstek.urule.RuleException;
import com.bstek.urule.Utils;
import com.bstek.urule.debug.MsgType;
import com.bstek.urule.model.library.Datatype;
import com.bstek.urule.model.rule.Parameter;
import com.bstek.urule.runtime.rete.Context;
import com.bstek.urule.runtime.rete.ValueCompute;
import com.bstek.urule.script.AviatorAction;
import com.bstek.urule.script.JsonPathAction;
import com.bstek.urule.script.ScriptType;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;

public class ExecuteScriptMethodAction extends AbstractAction {

    private String beanId;
    private String beanLabel;
    private String methodLabel;
    private String methodName;
    private List<Parameter> parameters;
    private ActionType actionType = ActionType.ExecuteScriptMethod;
    /**
     * 脚本类型
     * @see ScriptType
     */
    private ScriptType scriptType;
    /**
     * 脚本表达式
     */
    private String expression;

    @Override
    public ActionValue execute(Context context, Object matchedObject, List<Object> allMatchedObjects, Map<String, Object> variableMap) {
        String info = (beanLabel == null ? beanId : beanLabel) + (methodLabel == null ? methodName : methodLabel);
        info = "$$$执行动作：" + info;
        try {
            Object obj = context.getApplicationContext().getBean(beanId);
            java.lang.reflect.Method method = null;
            if (parameters != null && parameters.size() > 0) {
                ScriptParametersWrap wrap = buildParameterClasses(context, matchedObject, allMatchedObjects, variableMap);
                Method[] methods = obj.getClass().getMethods();
                Datatype[] targetDatatypes = wrap.getDatatypes();
                boolean match = false;
                for (Method m : methods) {
                    method = m;
                    String name = m.getName();
                    if (!name.equals(methodName)) {
                        continue;
                    }
                    Class<?> parameterClasses[] = m.getParameterTypes();
                    if (parameterClasses.length != parameters.size()) {
                        continue;
                    }
                    for (int i = 0; i < parameterClasses.length; i++) {
                        Class<?> clazz = parameterClasses[i];
                        Datatype datatype = targetDatatypes[i];
                        match = classMatch(clazz, datatype);
                        if (!match) {
                            break;
                        }
                    }
                    if (match) {
                        break;
                    }
                }
                if (!match) {
                    throw new RuleException("Bean [" + beanId + "." + methodName + "] with " + parameters.size() + " parameters not exist");
                }
                String valueKey = methodName;
                ActionId actionId = method.getAnnotation(ActionId.class);
                if (actionId != null) {
                    valueKey = actionId.value();
                }
                Object value = method.invoke(obj, wrap.getValues());
                if (debug && Utils.isDebug()) {
                    String msg = info + "(" + wrap.valuesToString() + ")";
                    context.debugMsg(msg, MsgType.ExecuteBeanMethod, debug);
                }
                if (value != null) {
                    return new ActionValueImpl(valueKey, handleResult(scriptType, expression, value));
                } else {
                    return null;
                }
            } else {
                method = obj.getClass().getMethod(methodName, new Class[]{});
                String valueKey = methodName;
                ActionId actionId = method.getAnnotation(ActionId.class);
                if (actionId != null) {
                    valueKey = actionId.value();
                }
                Object value = method.invoke(obj);
                if (debug && Utils.isDebug()) {
                    String msg = info + "()";
                    context.debugMsg(msg, MsgType.ExecuteBeanMethod, debug);
                }
                if (value != null) {
                    return new ActionValueImpl(valueKey, handleResult(scriptType, expression, value));
                } else {
                    return null;
                }
            }
        } catch (Exception ex) {
            throw new RuleException(ex);
        }
    }

    private boolean classMatch(Class<?> clazz, Datatype datatype) {
        boolean match = false;
        switch (datatype) {
            case String:
                if (clazz.equals(String.class)) {
                    match = true;
                } else {
                    match = false;
                }
                break;
            case BigDecimal:
                if (clazz.equals(BigDecimal.class)) {
                    match = true;
                } else {
                    match = false;
                }
                break;
            case Boolean:
                if (clazz.equals(Boolean.class) || clazz.equals(boolean.class)) {
                    match = true;
                } else {
                    match = false;
                }
                break;
            case Date:
                if (clazz.equals(Date.class)) {
                    match = true;
                } else {
                    match = false;
                }
                break;
            case Double:
                if (clazz.equals(Double.class) || clazz.equals(double.class)) {
                    match = true;
                } else {
                    match = false;
                }
                break;
            case Enum:
                if (Enum.class.isAssignableFrom(clazz)) {
                    match = true;
                } else {
                    match = false;
                }
                break;
            case Float:
                if (clazz.equals(Float.class) || clazz.equals(float.class)) {
                    match = true;
                } else {
                    match = false;
                }
                break;
            case Integer:
                if (clazz.equals(Integer.class) || clazz.equals(int.class)) {
                    match = true;
                } else {
                    match = false;
                }
                break;
            case Char:
                if (clazz.equals(Character.class) || clazz.equals(char.class)) {
                    match = true;
                } else {
                    match = false;
                }
                break;
            case List:
                if (List.class.isAssignableFrom(clazz)) {
                    match = true;
                } else {
                    match = false;
                }
                break;
            case Long:
                if (clazz.equals(Long.class) || clazz.equals(long.class)) {
                    match = true;
                } else {
                    match = false;
                }
                break;
            case Map:
                if (Map.class.isAssignableFrom(clazz)) {
                    match = true;
                } else {
                    match = false;
                }
                break;
            case Set:
                if (Set.class.isAssignableFrom(clazz)) {
                    match = true;
                } else {
                    match = false;
                }
                break;
            case Object:
                match = true;
                break;
        }
        return match;
    }

    private ScriptParametersWrap buildParameterClasses(Context context, Object matchedObject, List<Object> allMatchedObjects, Map<String, Object> variableMap) {
        List<Datatype> list = new ArrayList<Datatype>();
        List<Object> values = new ArrayList<Object>();
        ValueCompute valueCompute = context.getValueCompute();
        for (Parameter param : parameters) {
            Datatype type = param.getType();
            list.add(type);
            Object value = valueCompute.complexValueCompute(param.getValue(), matchedObject, context, allMatchedObjects, variableMap);
            values.add(type.convert(value));
        }
        Datatype[] datatypes = new Datatype[list.size()];
        list.toArray(datatypes);
        Object[] objs = new Object[values.size()];
        values.toArray(objs);
        ScriptParametersWrap wrap = new ScriptParametersWrap();
        wrap.setDatatypes(datatypes);
        wrap.setValues(objs);
        return wrap;
    }

    public String getMethodLabel() {
        return methodLabel;
    }

    public void setMethodLabel(String methodLabel) {
        this.methodLabel = methodLabel;
    }

    public String getBeanId() {
        return beanId;
    }

    public void setBeanId(String beanId) {
        this.beanId = beanId;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getBeanLabel() {
        return beanLabel;
    }

    public void setBeanLabel(String beanLabel) {
        this.beanLabel = beanLabel;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public void addParameter(Parameter parameter) {
        if (parameters == null) {
            parameters = new ArrayList<Parameter>();
        }
        parameters.add(parameter);
    }

    @Override
    public ActionType getActionType() {
        return actionType;
    }

    private Object handleResult(ScriptType scriptType,String expression, Object value) {
        if (null != scriptType && StringUtils.isNotBlank(expression)) {
            switch (scriptType) {
                case JSONPATH:
                    JsonPathAction jsonPathAction = new JsonPathAction();
                    jsonPathAction.setExpression(expression);
                    if(value instanceof Map) {
                        return jsonPathAction.actionInvoker().invoke(value);
                    }else if(value instanceof String) {
                        JSONObject rowData = JSONObject.parseObject((String) value);
                        return jsonPathAction.actionInvoker().invoke(rowData);
                    }
                case AVIATOR:
                    AviatorAction aviatorAction = new AviatorAction();
                    aviatorAction.setExpression(expression);
                    //value是map结构
                    return aviatorAction.actionInvoker().invoke(value);
                default:
                    return value;
            }

        } else {
            return value;
        }
    }

    public ScriptType getScriptType() {
        return scriptType;
    }

    public void setScriptType(ScriptType scriptType) {
        this.scriptType = scriptType;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }
}

class ScriptParametersWrap {
    private Datatype[] datatypes;
    private Object[] values;

    public Datatype[] getDatatypes() {
        return datatypes;
    }

    public void setDatatypes(Datatype[] datatypes) {
        this.datatypes = datatypes;
    }

    public Object[] getValues() {
        return values;
    }

    public void setValues(Object[] values) {
        this.values = values;
    }

    public String valuesToString() {
        if (values == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (Object obj : values) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            if (obj == null) {
                sb.append("null");
            } else {
                sb.append(obj);
            }
        }
        return sb.toString();
    }
}
