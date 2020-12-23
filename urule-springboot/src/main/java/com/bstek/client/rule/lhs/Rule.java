package com.bstek.client.rule.lhs;


import com.bstek.client.rule.Other;
import com.bstek.client.rule.Rhs;

/**
 *
 * @author wpx
 * @Description 类注释
 * @date 2020/12/23
 */
public class Rule {
    /**
     * 规则名称
     */
    private String name;
    /**
     * 优先级
     */
    private Integer priority;
    /**
     * 条件
     */
    private Lhs lhs;
    /**
     * 条件命中行为
     */
    private Rhs rhs;
    /**
     * 条件未命中行为
     */
    private Other other;


    public int compareTo(Rule rule) {
        return rule.getPriority() - this.priority;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Lhs getLhs() {
        return lhs;
    }

    public void setLhs(Lhs lhs) {
        this.lhs = lhs;
    }

    public Rhs getRhs() {
        return rhs;
    }

    public void setRhs(Rhs rhs) {
        this.rhs = rhs;
    }

    public Other getOther() {
        return other;
    }

    public void setOther(Other other) {
        this.other = other;
    }
}
