package com.bstek.urule.knowledge;

import com.bstek.urule.BizUtils;
import com.bstek.urule.RuleException;
import com.bstek.urule.Utils;
import com.bstek.urule.builder.KnowledgeBase;
import com.bstek.urule.builder.KnowledgeBuilder;
import com.bstek.urule.model.GeneralEntity;
import com.bstek.urule.model.library.Datatype;
import com.bstek.urule.model.library.constant.ConstantCategory;
import com.bstek.urule.model.library.variable.Variable;
import com.bstek.urule.model.library.variable.VariableCategory;
import com.bstek.urule.model.library.variable.VariableLibrary;
import com.bstek.urule.model.rule.Other;
import com.bstek.urule.model.rule.Rhs;
import com.bstek.urule.model.rule.Rule;
import com.bstek.urule.model.rule.RuleSet;
import com.bstek.urule.model.rule.lhs.Lhs;
import com.bstek.urule.runtime.KnowledgePackage;
import com.bstek.urule.runtime.KnowledgeSession;
import com.bstek.urule.runtime.KnowledgeSessionFactory;
import com.bstek.urule.runtime.response.ExecutionResponse;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author wpx
 * @since  2020/12/29
 */
public class KnowledgeHelper implements ApplicationContextAware {
    protected ApplicationContext applicationContext;

    private KnowledgeBuilder knowledgeBuilder;

    /**
     * 执行规则
     * @param ruleId ruleId
     * @param lhs lhs
     * @param variableCategoryLibs variableCategoryLibs
     * @return com.bstek.urule.runtime.response.ExecutionResponse ExecutionResponse
     * @author wpx
     * @since  2020/12/29 14:09
     */
    public ExecutionResponse execute(String ruleId, Lhs lhs, List<VariableLibrary> variableCategoryLibs) {
        Rhs rhs = Rhs.instance();
        rhs.addAction(BizUtils.buildVariableAssignAction("flag", Datatype.Boolean, "true"));

        Other other = new Other();
        other.addAction(BizUtils.buildVariableAssignAction("flag", Datatype.Boolean, "false"));

        return execute(ruleId, lhs, other, rhs, variableCategoryLibs);
    }


    /**
     * 执行规则
     * @author wpx
     * @param ruleId ruleId
     * @param lhs lhs
     * @param other other
     * @param rhs rhs
     * @param variableCategoryLibs variableCategoryLibs
     * @return com.bstek.urule.runtime.response.ExecutionResponse ExecutionResponse
     * @since  2020/12/29 14:09
     */
    public ExecutionResponse execute(String ruleId, Lhs lhs, Other other, Rhs rhs, List<VariableLibrary> variableCategoryLibs) {
        KnowledgeBase knowledgeBase = this.buildKnowledgeBaseByRuleSet(ruleId, lhs, other, rhs, variableCategoryLibs);
        KnowledgePackage knowledgePackage = knowledgeBase.getKnowledgePackage();
        //构造树
        KnowledgeSession session = KnowledgeSessionFactory.newKnowledgeSession(knowledgePackage);
        Map<VariableCategory, Object> facts = buildFacts(knowledgeBase);
        Map<String, Object> parameters = null;
        for (Object obj : facts.values()) {
            if (!(obj instanceof GeneralEntity) && (obj instanceof HashMap)) {
                parameters = (Map<String, Object>) obj;
            } else {
                session.insert(obj);
            }
        }
        ExecutionResponse response = null;
        if (parameters == null) {
            response = session.fireRules();
        } else {
            response = session.fireRules(parameters);
        }
        Object flag = session.getParameter("flag");
        return response;
    }

    /**
     * 构造参数
     *
     * @param knowledgeBase knowledgeBase
     * @return Map Map
     */
    private Map<VariableCategory, Object> buildFacts(KnowledgeBase knowledgeBase) {
        Map<VariableCategory, Object> facts = new HashMap<VariableCategory, Object>();
        List<VariableCategory> variableCategories = knowledgeBase.getResourceLibrary().getVariableCategories();
        for (VariableCategory vc : variableCategories) {
            String clazz = vc.getClazz();
            Object entity = null;
            if (vc.getName().equals(VariableCategory.PARAM_CATEGORY)) {
                entity = new HashMap<String, Object>();
            } else {
                entity = new GeneralEntity(clazz);
            }
            for (Variable var : vc.getVariables()) {
                buildObject(entity, var);
            }
            facts.put(vc, entity);
        }
        return facts;
    }

    /**
     * 根据规则集构造知识包（注意：不能缓存，因为每次请求的 lhs 不一样。）
     *
     * @param ruleId ruleId
     * @param lhs lhs
     * @param other other
     * @param rhs rhs
     * @param variableCategoryLibs variableCategoryLibs
     * @return com.bstek.urule.builder.KnowledgeBase KnowledgeBase
     * @author wpx
     * @since  2020/12/29 13:44
     */
    private KnowledgeBase buildKnowledgeBaseByRuleSet(String ruleId, Lhs lhs, Other other, Rhs rhs, List<VariableLibrary> variableCategoryLibs) {
        //KnowledgeBase knowledgeBase = KNOWLEDGE_CACHE.getIfPresent(ruleId);
       // if (null == knowledgeBase) {
            RuleSet ruleSet = new RuleSet();
            ruleSet.setRemark("RuleRegister");
            ruleSet.setRules(Arrays.asList(this.buildRule(lhs, other, rhs)));
            KnowledgeBase knowledgeBase = knowledgeBuilder.buildKnowledgeBase(ruleSet, variableCategoryLibs);
           // KNOWLEDGE_CACHE.put(ruleId, knowledgeBase);
       // }
        return knowledgeBase;
    }

    /**
     * 构建规则
     *
     * @return
     */
    private Rule buildRule(Lhs lhs, Other other, Rhs rhs) {
        Rule rule = new Rule();
        rule.setName("test001");
        rule.setRemark("test001");
        if (null != other) {
            rule.setOther(other);
        }
        if (null != rhs) {
            rule.setRhs(rhs);
        }
        rule.setLhs(lhs);
        return rule;
    }

    /**
     * 知识包缓存
     */
    public final LoadingCache<String, KnowledgeBase> KNOWLEDGE_CACHE = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(24, TimeUnit.HOURS)
            .build(new CacheLoader<String, KnowledgeBase>() {
                @Override
                public KnowledgeBase load(String name) throws Exception {
                    return null;
                }
            });


    private void buildObject(Object obj, Variable var) {
        String name = var.getName();
        if (name.indexOf(".") != -1) {
            instanceChildObject(obj, name);
        }
        String defaultValue = var.getDefaultValue();
        if (StringUtils.isBlank(defaultValue)) {
            return;
        }
        Datatype type = var.getType();
        if (type.equals(Datatype.List)) {
            Utils.setObjectProperty(obj, name, buildList(defaultValue));
        } else if (type.equals(Datatype.Set)) {
            Utils.setObjectProperty(obj, name, buildSet(defaultValue));
        } else if (type.equals(Datatype.Map)) {
            return;
        } else {
            Object value = type.convert(defaultValue);
            Utils.setObjectProperty(obj, name, value);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Set<GeneralEntity> buildSet(String value) {
        try {
            Set<GeneralEntity> result = new HashSet<GeneralEntity>();
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> map = mapper.readValue(value, HashMap.class);
            if (map.containsKey("rows")) {
                List<Object> list = (List<Object>) map.get("rows");
                for (Object obj : list) {
                    if (obj instanceof Map) {
                        GeneralEntity entity = new GeneralEntity((String) map.get("type"));
                        entity.putAll((Map) obj);
                        result.add(entity);
                    }
                }
                return result;
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new RuleException(e);
        }
    }

    private void instanceChildObject(Object obj, String propertyName) {
        int pointIndex = propertyName.indexOf(".");
        if (pointIndex == -1) {
            return;
        }
        String name = propertyName.substring(0, pointIndex);
        propertyName = propertyName.substring(pointIndex + 1);
        try {
            Object instance = PropertyUtils.getProperty(obj, name);
            if (instance != null) {
                instanceChildObject(instance, propertyName);
                return;
            }
            Object targetEntity = new GeneralEntity(name);
            PropertyUtils.setProperty(obj, name, targetEntity);
            instanceChildObject(targetEntity, propertyName);
        } catch (Exception e) {
            throw new RuleException(e);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<GeneralEntity> buildList(String value) {
        try {
            List<GeneralEntity> result = new ArrayList<GeneralEntity>();
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> map = mapper.readValue(value, HashMap.class);
            if (map.containsKey("rows")) {
                List<Object> list = (List<Object>) map.get("rows");
                for (Object obj : list) {
                    if (obj instanceof Map) {
                        GeneralEntity entity = new GeneralEntity((String) map.get("type"));
                        entity.putAll((Map) obj);
                        result.add(entity);
                    }
                }
                return result;
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new RuleException(e);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void setKnowledgeBuilder(KnowledgeBuilder knowledgeBuilder) {
        this.knowledgeBuilder = knowledgeBuilder;
    }
}
