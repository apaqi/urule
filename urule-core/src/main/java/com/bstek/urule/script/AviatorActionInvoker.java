package com.bstek.urule.script;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;

import java.util.Map;

public class AviatorActionInvoker extends AbstractScriptActionInvoker {

    private Expression script;

    protected AviatorActionInvoker(AviatorAction action) {
        super(action);
        this.script = AviatorEvaluator.getInstance().compile(action.getExpression());
    }

    @Override
    public Object invoke(Object context) {
        return this.script.execute((Map<String, Object>) context);
    }
}
