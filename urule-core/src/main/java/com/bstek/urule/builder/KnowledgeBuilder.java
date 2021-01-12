/*******************************************************************************
 * Copyright 2017 Bstek
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.bstek.urule.builder;

import java.io.IOException;
import java.util.*;

import com.bstek.urule.model.library.Datatype;
import com.bstek.urule.model.library.action.ActionConfig;
import com.bstek.urule.model.library.action.ActionLibrary;
import com.bstek.urule.model.library.constant.ConstantLibrary;
import com.bstek.urule.model.library.variable.*;
import com.bstek.urule.model.rule.lhs.*;
import com.google.common.collect.Sets;
import org.dom4j.Element;

import com.bstek.urule.builder.resource.Resource;
import com.bstek.urule.builder.resource.ResourceBuilder;
import com.bstek.urule.builder.resource.ResourceType;
import com.bstek.urule.builder.table.DecisionTableRulesBuilder;
import com.bstek.urule.builder.table.ScriptDecisionTableRulesBuilder;
import com.bstek.urule.dsl.DSLRuleSetBuilder;
import com.bstek.urule.model.decisiontree.DecisionTree;
import com.bstek.urule.model.flow.FlowDefinition;
import com.bstek.urule.model.library.ResourceLibrary;
import com.bstek.urule.model.rete.Rete;
import com.bstek.urule.model.rete.builder.ReteBuilder;
import com.bstek.urule.model.rule.Library;
import com.bstek.urule.model.rule.Rule;
import com.bstek.urule.model.rule.RuleSet;
import com.bstek.urule.model.rule.loop.LoopRule;
import com.bstek.urule.model.scorecard.runtime.ScoreRule;
import com.bstek.urule.model.table.DecisionTable;
import com.bstek.urule.model.table.ScriptDecisionTable;
import com.bstek.urule.runtime.KnowledgePackageWrapper;
import com.bstek.urule.runtime.service.KnowledgePackageService;
import org.springframework.util.CollectionUtils;

/**
 * @author Jacky.gao
 * @since 2014年12月22日
 */
public class KnowledgeBuilder extends AbstractBuilder {
    private ResourceLibraryBuilder resourceLibraryBuilder;
    private ReteBuilder reteBuilder;
    private RulesRebuilder rulesRebuilder;
    private DecisionTreeRulesBuilder decisionTreeRulesBuilder;
    private DecisionTableRulesBuilder decisionTableRulesBuilder;
    private ScriptDecisionTableRulesBuilder scriptDecisionTableRulesBuilder;
    private DSLRuleSetBuilder dslRuleSetBuilder;
    public static final String BEAN_ID = "urule.knowledgeBuilder";

    /**
     * 根据资源文件构造知识包：
     * 1.循环资源文件，使用com.bstek.urule.builder.resource.Resource接口实现
     * 2.如果规则是UL(规则文本文件），直接加载。
     * 3.其他均为xml定义，使用ResourceProvider加载文件或数据库中的xml文件
     * 4.循环调用resourceBuilders，解析xml到各类规则文件中
     * 5.构建Rete树
     *
     * @param resourceBase resourceBase
     * @return KnowledgeBase KnowledgeBase
     * @throws IOException IOException
     */
    public KnowledgeBase buildKnowledgeBase(ResourceBase resourceBase) throws IOException {
        KnowledgePackageService knowledgePackageService = (KnowledgePackageService) applicationContext.getBean(KnowledgePackageService.BEAN_ID);
        List<Rule> rules = new ArrayList<Rule>();
        Map<String, Library> libMap = new HashMap<String, Library>();
        Map<String, FlowDefinition> flowMap = new HashMap<String, FlowDefinition>();
        //循环资源文件
        for (Resource resource : resourceBase.getResources()) {
            //如果规则是UL(规则文本文件），直接加载
            if (dslRuleSetBuilder.support(resource)) {
                RuleSet ruleSet = dslRuleSetBuilder.build(resource.getContent());
                addToLibraryMap(libMap, ruleSet.getLibraries());
                if (ruleSet.getRules() != null) {
                    rules.addAll(ruleSet.getRules());
                }
                continue;
            }
            //循环调用resourceBuilders，解析xml到各类规则文件中
            Element root = parseResource(resource.getContent());
            for (ResourceBuilder<?> builder : resourceBuilders) {
                if (!builder.support(root)) {
                    continue;
                }
                Object object = builder.build(root);
                ResourceType type = builder.getType();
                if (type.equals(ResourceType.RuleSet)) {
                    RuleSet ruleSet = (RuleSet) object;
                    addToLibraryMap(libMap, ruleSet.getLibraries());
                    if (ruleSet.getRules() != null) {
                        List<Rule> ruleList = ruleSet.getRules();
                        rulesRebuilder.convertNamedJunctions(ruleList);
                        for (Rule rule : ruleList) {
                            if (rule.getEnabled() != null && rule.getEnabled() == false) {
                                continue;
                            }
                            rules.add(rule);
                        }
                    }
                } else if (type.equals(ResourceType.DecisionTree)) {
                    DecisionTree tree = (DecisionTree) object;
                    addToLibraryMap(libMap, tree.getLibraries());
                    RuleSet ruleSet = decisionTreeRulesBuilder.buildRules(tree);
                    addToLibraryMap(libMap, ruleSet.getLibraries());
                    if (ruleSet.getRules() != null) {
                        rules.addAll(ruleSet.getRules());
                    }
                } else if (type.equals(ResourceType.DecisionTable)) {
                    DecisionTable table = (DecisionTable) object;
                    addToLibraryMap(libMap, table.getLibraries());
                    List<Rule> tableRules = decisionTableRulesBuilder.buildRules(table);
                    rules.addAll(tableRules);
                } else if (type.equals(ResourceType.ScriptDecisionTable)) {
                    ScriptDecisionTable table = (ScriptDecisionTable) object;
                    RuleSet ruleSet = scriptDecisionTableRulesBuilder.buildRules(table);
                    addToLibraryMap(libMap, ruleSet.getLibraries());
                    if (ruleSet.getRules() != null) {
                        rules.addAll(ruleSet.getRules());
                    }
                } else if (type.equals(ResourceType.Flow)) {
                    FlowDefinition fd = (FlowDefinition) object;
                    fd.initNodeKnowledgePackage(this, knowledgePackageService, dslRuleSetBuilder);
                    addToLibraryMap(libMap, fd.getLibraries());
                    flowMap.put(fd.getId(), fd);
                } else if (type.equals(ResourceType.Scorecard)) {
                    ScoreRule rule = (ScoreRule) object;
                    rules.add(rule);
                    addToLibraryMap(libMap, rule.getLibraries());
                }
                break;
            }
        }
        ResourceLibrary resourceLibrary = resourceLibraryBuilder.buildResourceLibrary(libMap.values());
        buildLoopRules(rules, resourceLibrary);
        //构建Rete树
        Rete rete = reteBuilder.buildRete(rules, resourceLibrary);
        return new KnowledgeBase(rete, flowMap, retriveNoLhsRules(rules));
    }

    private void buildLoopRules(List<Rule> rules, ResourceLibrary resourceLibrary) {
        for (Rule rule : rules) {
            if (!(rule instanceof LoopRule)) {
                continue;
            }
            LoopRule loopRule = (LoopRule) rule;
            List<Rule> ruleList = buildRules(loopRule);
            Rete rete = reteBuilder.buildRete(ruleList, resourceLibrary);
            KnowledgeBase base = new KnowledgeBase(rete);
            KnowledgePackageWrapper knowledgeWrapper = new KnowledgePackageWrapper(base.getKnowledgePackage());
            loopRule.setKnowledgePackageWrapper(knowledgeWrapper);
        }
    }

    private List<Rule> buildRules(LoopRule loopRule) {
        Rule rule = new Rule();
        rule.setDebug(loopRule.getDebug());
        rule.setName("loop-rule");
        rule.setLhs(loopRule.getLhs());
        rule.setRhs(loopRule.getRhs());
        rule.setOther(loopRule.getOther());
        List<Rule> rules = new ArrayList<Rule>();
        rules.add(rule);
        return rules;
    }

    /**
     * 根据RuleSet构建知识包
     *
     * @param ruleSet  ruleSet
     * @return com.bstek.urule.builder.KnowledgeBase KnowledgeBase
     * @since  2020/12/22 14:17
     */
    public KnowledgeBase buildKnowledgeBase(RuleSet ruleSet) {
        List<Rule> rules = new ArrayList<Rule>();
        Map<String, Library> libMap = new HashMap<String, Library>();
        addToLibraryMap(libMap, ruleSet.getLibraries());
        if (ruleSet.getRules() != null) {
            rules.addAll(ruleSet.getRules());
        }
        ResourceLibrary resourceLibrary = resourceLibraryBuilder.buildResourceLibrary(libMap.values());
        Rete rete = reteBuilder.buildRete(rules, resourceLibrary);
        return new KnowledgeBase(rete, null, retriveNoLhsRules(rules));
    }

    /**
     * 根据规则集和资源库构造知识库
     * @param ruleSet ruleSet
     * @param  variableCategoryLibs variableCategoryLibs
     * @return KnowledgeBase KnowledgeBase
     */
    public KnowledgeBase buildKnowledgeBase(RuleSet ruleSet, List<VariableLibrary> variableCategoryLibs) {
        List<Rule> rules = new ArrayList<Rule>();
        if (ruleSet.getRules() != null) {
            rules.addAll(ruleSet.getRules());
        }
        List<ActionConfig> actionConfigs2 = this.buildActionConfigs(ruleSet);
        ResourceLibrary resourceLibrary = resourceLibraryBuilder.buildResourceLibrary(actionConfigs2, variableCategoryLibs);
        //todo 同一个规则集，resourceLibrary 可以缓存
        Rete rete = reteBuilder.buildRete(rules, resourceLibrary);
        return new KnowledgeBase(rete, null, retriveNoLhsRules(rules));
    }

    /**
     * 构造客户端端行为配置信息
     * @param ruleSet
     * @return
     */
    private List<ActionConfig> buildActionConfigs(RuleSet ruleSet) {
        List<ActionConfig> actionConfigs = new ArrayList<>();
        Set<String> methodSet = Sets.newHashSet();
        List<Rule> rules = ruleSet.getRules();
        for(Rule rule : rules) {
            Lhs lhs = rule.getLhs();
            Criterion criterion = lhs.getCriterion();
            parseMethod(criterion, actionConfigs, methodSet);
        }
        return actionConfigs;
    }

    private List<Rule> retriveNoLhsRules(List<Rule> rules) {
        List<Rule> noLhsRules = new ArrayList<Rule>();
        for (Rule rule : rules) {
            Lhs lhs = rule.getLhs();
            if ((rule instanceof LoopRule) || (lhs == null || lhs.getCriterion() == null)) {
                noLhsRules.add(rule);
            }
        }
        return noLhsRules;
    }

    private void addToLibraryMap(Map<String, Library> map, List<Library> libraries) {
        if (libraries == null) {
            return;
        }
        for (Library lib : libraries) {
            String path = lib.getPath();
            if (map.containsKey(path)) {
                continue;
            }
            map.put(path, lib);
        }
    }

    /**
     * 根据规则xml文件生成知识包
     *
     * @param xml xml
     * @return com.bstek.urule.builder.KnowledgeBase KnowledgeBase
     * @author wpx
     * @since  2020/12/22 14:19
     * @throws  IOException IOException
     */
    public KnowledgeBase buildKnowledgeBase(String xml) throws IOException {

        /**
         * 2. 解析规则
         */
        //依赖常量库
        List<ConstantLibrary> constantLibraries = new ArrayList<>();
        //依赖的springbean
        List<ActionLibrary> actionLibraries = new ArrayList<>();
        ActionLibrary actionLibrary = new ActionLibrary();
        actionLibraries.add(actionLibrary);
        //依赖的变量
        List<VariableLibrary> variableLibraries = new ArrayList<>();
        VariableLibrary variableLibrary = new VariableLibrary();

        //依赖的变量->变量类型
        List<VariableCategory> variableCategories = new ArrayList<>();
        VariableCategory variableCategory = new VariableCategory();
        variableCategory.setClazz("java.util.HashMap");
        variableCategory.setName("参数");
        variableCategory.setType(CategoryType.Clazz);

        //依赖的变量->变量信息
        List<Variable> variables = new ArrayList<>();
        Variable variable = new Variable();
        variable.setAct(Act.InOut);
        variable.setName("商品名称");
        variable.setLabel("skuName");
        variable.setType(Datatype.String);
        variables.add(variable);

        Variable variable2 = new Variable();
        variable2.setAct(Act.InOut);
        variable2.setName("商品id");
        variable2.setLabel("skuId");
        variable2.setType(Datatype.Long);
        variables.add(variable2);

        variableCategory.setVariables(variables);
        variableCategories.add(variableCategory);
        variableLibrary.setVariableCategories(variableCategories);
        variableLibraries.add(variableLibrary);
        ResourceLibrary resourceLibrary = new ResourceLibrary( variableLibraries, actionLibraries, constantLibraries);




        KnowledgePackageService knowledgePackageService = (KnowledgePackageService) applicationContext.getBean(KnowledgePackageService.BEAN_ID);
        List<Rule> rules = new ArrayList<Rule>();
       // Map<String, Library> libMap = new HashMap<String, Library>();
        Map<String, FlowDefinition> flowMap = new HashMap<String, FlowDefinition>();
        //循环调用resourceBuilders，解析xml到各类规则文件中
        Element root = parseResource(xml);
        for (ResourceBuilder<?> builder : resourceBuilders) {
            if (!(builder.support(root))) {
                continue;
            }
            Object object = builder.build(root);
            ResourceType type = builder.getType();
            if (type.equals(ResourceType.RuleSet)) {
                RuleSet ruleSet = (RuleSet) object;
                //addToLibraryMap(libMap, ruleSet.getLibraries());
                if (ruleSet.getRules() != null) {
                    List<Rule> ruleList = ruleSet.getRules();
                    rulesRebuilder.convertNamedJunctions(ruleList);
                    for (Rule rule : ruleList) {
                        if (rule.getEnabled() != null && rule.getEnabled() == false) {
                            continue;
                        }
                        rules.add(rule);
                    }
                }
            } else if (type.equals(ResourceType.DecisionTree)) {
                DecisionTree tree = (DecisionTree) object;
               // addToLibraryMap(libMap, tree.getLibraries());
                RuleSet ruleSet = decisionTreeRulesBuilder.buildRules(tree);
               // addToLibraryMap(libMap, ruleSet.getLibraries());
                if (ruleSet.getRules() != null) {
                    rules.addAll(ruleSet.getRules());
                }
            } else if (type.equals(ResourceType.DecisionTable)) {
                DecisionTable table = (DecisionTable) object;
                //addToLibraryMap(libMap, table.getLibraries());
                List<Rule> tableRules = decisionTableRulesBuilder.buildRules(table);
                rules.addAll(tableRules);
            } else if (type.equals(ResourceType.ScriptDecisionTable)) {
                ScriptDecisionTable table = (ScriptDecisionTable) object;
                RuleSet ruleSet = scriptDecisionTableRulesBuilder.buildRules(table);
                //addToLibraryMap(libMap, ruleSet.getLibraries());
                if (ruleSet.getRules() != null) {
                    rules.addAll(ruleSet.getRules());
                }
            } else if (type.equals(ResourceType.Flow)) {
                FlowDefinition fd = (FlowDefinition) object;
                fd.initNodeKnowledgePackage(this, knowledgePackageService, dslRuleSetBuilder);
                //addToLibraryMap(libMap, fd.getLibraries());
                flowMap.put(fd.getId(), fd);
            } else if (type.equals(ResourceType.Scorecard)) {
                ScoreRule rule = (ScoreRule) object;
                rules.add(rule);
                //addToLibraryMap(libMap, rule.getLibraries());
            }
            break;
        }

        //ResourceLibrary resourceLibrary = resourceLibraryBuilder.buildResourceLibrary(libMap.values());
        buildLoopRules(rules, resourceLibrary);
        //构建Rete树
        Rete rete = reteBuilder.buildRete(rules, resourceLibrary);
        return new KnowledgeBase(rete, flowMap, retriveNoLhsRules(rules));
    }

    public void setRulesRebuilder(RulesRebuilder rulesRebuilder) {
        this.rulesRebuilder = rulesRebuilder;
    }

    public void setReteBuilder(ReteBuilder reteBuilder) {
        this.reteBuilder = reteBuilder;
    }

    public void setDecisionTableRulesBuilder(DecisionTableRulesBuilder decisionTableRulesBuilder) {
        this.decisionTableRulesBuilder = decisionTableRulesBuilder;
    }

    public void setScriptDecisionTableRulesBuilder(ScriptDecisionTableRulesBuilder scriptDecisionTableRulesBuilder) {
        this.scriptDecisionTableRulesBuilder = scriptDecisionTableRulesBuilder;
    }

    public void setDslRuleSetBuilder(DSLRuleSetBuilder dslRuleSetBuilder) {
        this.dslRuleSetBuilder = dslRuleSetBuilder;
    }

    public void setResourceLibraryBuilder(ResourceLibraryBuilder resourceLibraryBuilder) {
        this.resourceLibraryBuilder = resourceLibraryBuilder;
    }

    public void setDecisionTreeRulesBuilder(DecisionTreeRulesBuilder decisionTreeRulesBuilder) {
        this.decisionTreeRulesBuilder = decisionTreeRulesBuilder;
    }

    private void parseMethod(Criterion criterion, List<ActionConfig> actionConfigs, Set<String> methodSet){
        if(criterion instanceof And) {
            List<Criterion> criterions = ((And) criterion).getCriterions();
            if(!CollectionUtils.isEmpty(criterions)) {
                for(Criterion andCriterion : criterions) {
                    if(andCriterion instanceof And) {
                        parseMethod(andCriterion, actionConfigs, methodSet);
                    } else if(andCriterion instanceof Or){
                        parseMethod(andCriterion, actionConfigs, methodSet);
                    }else {
                        Criteria c = (Criteria)andCriterion;
                        if(Objects.nonNull(c.getLeft()) && Objects.nonNull(c.getLeft().getLeftPart())
                                && c.getLeft().getLeftPart() instanceof MethodLeftPart) {
                            String beanId = ((MethodLeftPart) c.getLeft().getLeftPart()).getBeanId();
                            if(!methodSet.contains(beanId)) {
                                ActionConfig actionConfig = new ActionConfig();
                                actionConfig.setActionFlag(beanId);
                                actionConfigs.add(actionConfig);
                                //methodSet.add(beanId);
                            }

                        }
                    }

                }
            }
        }
        if(criterion instanceof Or ) {
            List<Criterion> criterions = ((Or)criterion).getCriterions();
            if(!CollectionUtils.isEmpty(criterions)) {
                for(Criterion orCriterion : criterions) {
                    if(orCriterion instanceof Or) {
                        parseMethod(orCriterion, actionConfigs, methodSet);
                    }else if(orCriterion instanceof And){
                        parseMethod(orCriterion, actionConfigs, methodSet);
                    }else {
                        Criteria c = (Criteria)orCriterion;
                        if(Objects.nonNull(c.getLeft()) && Objects.nonNull(c.getLeft().getLeftPart())
                                && c.getLeft().getLeftPart() instanceof MethodLeftPart ) {
                            String beanId = ((MethodLeftPart) c.getLeft().getLeftPart()).getBeanId();
                            if(!methodSet.contains(beanId)) {
                                ActionConfig actionConfig = new ActionConfig();
                                actionConfig.setActionFlag(beanId);
                                actionConfigs.add(actionConfig);
                                //methodSet.add(beanId);
                            }

                        }
                    }

                }
            }
        }
    }
}
