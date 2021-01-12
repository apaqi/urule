package com.bstek.urule.script;

/**
 * AbstractActionInvoker
 */
public abstract class AbstractScriptActionInvoker implements ScriptActionInvoker {

    private final ScriptAction action;

    protected AbstractScriptActionInvoker(ScriptAction action) {
        this.action = action;
    }

    public ScriptAction getAction() {
        return action;
    }
}
