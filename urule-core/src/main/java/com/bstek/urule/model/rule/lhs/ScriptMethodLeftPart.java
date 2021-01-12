package com.bstek.urule.model.rule.lhs;

import com.bstek.urule.model.rule.Parameter;
import com.bstek.urule.model.rule.lhs.LeftPart;
import com.bstek.urule.script.ScriptAction;
import com.bstek.urule.script.ScriptType;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.List;

/**
 * 参照MethodLeftPart，新增脚本行为能力
 *
 * @author wpx
 */
public class ScriptMethodLeftPart implements LeftPart {
    @JsonIgnore
    private String id;
    private String beanId;
    private String beanLabel;
    private String methodName;
    private String methodLabel;
    private List<Parameter> parameters;

    /**
     * 脚本类型
     */
    private ScriptType scriptType;
    /**
     * 脚本表达式
     */
    private String expression;
    public String getBeanId() {
        return beanId;
    }
    public void setBeanId(String beanId) {
        this.beanId = beanId;
    }
    public String getBeanLabel() {
        return beanLabel;
    }
    public void setBeanLabel(String beanLabel) {
        this.beanLabel = beanLabel;
    }
    public String getMethodName() {
        return methodName;
    }
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
    public String getMethodLabel() {
        return methodLabel;
    }
    public void setMethodLabel(String methodLabel) {
        this.methodLabel = methodLabel;
    }
    public List<Parameter> getParameters() {
        return parameters;
    }
    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
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

    @Override
    public String getId() {
        if(id==null){
            if(parameters!=null){
                String parametersId="";
                int i=0;
                for(Parameter parameter:parameters){
                    if(i>0){
                        parametersId+=",";
                    }
                    parametersId+=parameter.getId();
                    i++;
                }
                id="[方法]"+beanLabel+"."+methodLabel+"("+parametersId+")";
            }else{
                id="[方法]"+beanLabel+"."+methodLabel;
            }
        }
        return id;
    }

}
