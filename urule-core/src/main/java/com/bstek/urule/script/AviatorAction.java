package com.bstek.urule.script;


public class AviatorAction extends AbstractScriptAction {

    private String expression;

    @Override
    protected ScriptActionInvoker newActionInvoker() {
        return new AviatorActionInvoker(this);
    }

    @Override
    public ScriptType getActionType() {
        return ScriptType.AVIATOR;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }
}
