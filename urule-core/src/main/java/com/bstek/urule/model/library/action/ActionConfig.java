package com.bstek.urule.model.library.action;

/**
 *
 * @author wpx
 * @since  2020/12/23
 */
public class ActionConfig {
    /**
     * 行为类型
     */
    private ActionTriggerType actionTriggerType;
    /**
     * 行为动作标识（接口标识）
     */
    private String actionFlag;

    public ActionTriggerType getActionTriggerType() {
        return actionTriggerType;
    }

    public void setActionTriggerType(ActionTriggerType actionTriggerType) {
        this.actionTriggerType = actionTriggerType;
    }

    public String getActionFlag() {
        return actionFlag;
    }

    public void setActionFlag(String actionFlag) {
        this.actionFlag = actionFlag;
    }
}
