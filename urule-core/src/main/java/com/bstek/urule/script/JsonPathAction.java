package com.bstek.urule.script;

public class JsonPathAction extends AbstractScriptAction {

    private String expression;

    @Override
    public ScriptType getActionType() {
        return ScriptType.JSONPATH;
    }

    @Override
    protected ScriptActionInvoker newActionInvoker() {
        return new JsonPathActionInvoker(this);
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }
}
