package com.bstek.client.rule.lhs;


import com.bstek.client.libary.VarConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * 规则集信息
 *
 * @author wpx
 * @Description 类注释
 * @date 2020/12/23
 */
public class RuleSet {
    /**
     * 规则集唯一标识
     */
    private String uuid;
    /**
     * 变量配置信息
     */
    private List<VarConfig> varConfigs;
    /**
     * 规则信息
     */
    private List<Rule> rules;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public List<VarConfig> getVarConfigs() {
        return varConfigs;
    }

    public void setVarConfigs(List<VarConfig> varConfigs) {
        this.varConfigs = varConfigs;
    }

    public void addVarConfigs(VarConfig varConfig) {
        if (this.varConfigs == null) {
            this.varConfigs = new ArrayList<VarConfig>();
        }
        this.varConfigs.add(varConfig);
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

}
