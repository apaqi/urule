package com.bstek.client.libary;


import java.util.Objects;

public class Variable {
    /**
     * 变量中文名称
     */
    private String name;
    /**
     * 变量英文名称
     */
    private String label;
    /**
     * 变量类型
     */
    private Datatype type;
    /**
     * 默认值
     */
    private String defaultValue;
    /**
     * 变量行为
     */
    private Act act = Act.InOut;

    public String getName() {
        if (Objects.isNull(name) || name.length() <= 0) {
            return label;
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Datatype getType() {
        return type;
    }

    public void setType(Datatype type) {
        this.type = type;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Act getAct() {
        return act;
    }

    public void setAct(Act act) {
        this.act = act;
    }
}
