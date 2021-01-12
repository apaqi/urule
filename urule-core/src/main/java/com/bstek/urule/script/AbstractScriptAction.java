package com.bstek.urule.script;

/**
 * 类注释
 *
 * @author wpx
 * @since 2021/1/11
 */
public abstract class AbstractScriptAction implements ScriptAction{

    private ScriptActionInvoker actionInvoker;

    @Override
    public ScriptActionInvoker actionInvoker() {
        if (this.actionInvoker == null) {
            this.actionInvoker = newActionInvoker();
        }
        return this.actionInvoker;
    }

    protected abstract ScriptActionInvoker newActionInvoker();


}
