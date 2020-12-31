package com.bstek.urule.test;

import com.alibaba.fastjson.JSON;
import com.bstek.urule.BizUtils;
import com.bstek.urule.model.library.Datatype;
import com.bstek.urule.model.rule.Op;
import com.bstek.urule.model.rule.Parameter;
import com.bstek.urule.model.rule.SimpleValue;
import com.bstek.urule.model.rule.lhs.*;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author wpx
 * @Description 类注释
 * @date 2020/12/29
 */
public class UruleTest {
    @Test
    public void dd(){

        Parameter parameter = BizUtils.buildSimpleParameter("username", Datatype.String, "123");
        /**规则组1*/
        /**规则组1--规则1*/
        MethodLeftPart leftPart = BizUtils.buildMethodLeftPart("methodTest", "evalTest", parameter);
        Criteria criteria1 = Criteria.instance()
                .setLeft(Left.instance(leftPart))
                .setOp(Op.Equals)
                .setValue(SimpleValue.instance("testValue"));

        /**规则组1--规则2*/
        MethodLeftPart leftPart2 = BizUtils.buildMethodLeftPart("methodTest", "evalTest", parameter);
        Criteria criteria2 = Criteria.instance()
                .setLeft(Left.instance(leftPart))
                .setOp(Op.Equals)
                .setValue(SimpleValue.instance("testValue"));
        And and = And.instance().addCriterion(false, criteria1, criteria2);
        /**规则组2*/
        /**规则组2 中的规则1*/
        Criteria orCriteria1 = Criteria.instance()
                .setLeft(Left.instance(leftPart))
                .setOp(Op.Equals)
                .setValue(SimpleValue.instance("testValue"));
        /**规则组2 中的规则2*/
        Criteria orCriteria2 = Criteria.instance()
                .setLeft(Left.instance(leftPart))
                .setOp(Op.Equals)
                .setValue(SimpleValue.instance("testValue"));
        Or or = Or.instance().addCriterion(false, orCriteria1, orCriteria2);
        Lhs lhs = Lhs.instance().setCriterion(And.instance().addCriterion(true, and, or));
    }

    @Test
    public void parse() {
        String json = "{\"category\":\"ifUser\",\"version\":1,\"value\":{\"type\":\"and\",\"list\":[{\"type\":\"or\",\"list\":[{\"bizType\":\"tag_var\",\"code\":\"tag_var\",\"index\":0,\"resultKey\":\"\",\"mvelKey\":\"tag_var_0\",\"variableId\":34,\"mvelOper\":\"equal\",\"variableType\":2,\"variableNodeData\":{\"remark\":\"EVENT_ID（事件编号）\",\"label\":\"$S_GP_EVENT_ID\",\"type\":2,\"value\":34},\"valueType\":7,\"inputValue\":\"变量 EVENT_ID（事件编号） $S_GP_EVENT_ID 等于 测试obtain7-618002\",\"tagType\":1,\"mvelValue\":\"7-618002\",\"recheckType\":\"\",\"bizParam\":{\"variableId\":34},\"timeRange\":[]},{\"bizType\":\"tag_var\",\"code\":\"tag_var\",\"index\":1,\"resultKey\":\"\",\"mvelKey\":\"tag_var_1\",\"variableId\":34,\"mvelOper\":\"equal\",\"variableType\":2,\"variableNodeData\":{\"remark\":\"EVENT_ID（事件编号）\",\"label\":\"$S_GP_EVENT_ID\",\"type\":2,\"value\":34},\"valueType\":7,\"inputValue\":\"变量 EVENT_ID（事件编号） $S_GP_EVENT_ID 等于 宋振波测试MQ\",\"tagType\":1,\"mvelValue\":\"1-20201123\",\"recheckType\":\"\",\"bizParam\":{\"variableId\":34},\"timeRange\":[]}]},{\"type\":\"and\",\"list\":[{\"bizType\":\"mall_trade\",\"code\":\"baitiaoAmount\",\"valueType\":999,\"inputValue\":\"用户白条可用余额 等于 用户提交订单的价格 \",\"tagType\":1,\"mvelValue\":\"\",\"index\":2,\"mvelKey\":\"baitiaoAmount_2\",\"mvelOper\":\"==\",\"bizParam\":{\"inputFlag\":1,\"amount\":\"403\",\"labelKey\":\"BAITIAO_AMOUNT\",\"operator\":\"==\"},\"timeRange\":[]}]}]},\"taskId\":11529,\"key\":-2}";
        Map<String, Object> jsonMap = JSON.parseObject(json, Map.class);
        //获取控件类型 json中的
        String category = MapUtils.getString(jsonMap, "category");
        //条件控件
        if ("ifUser".equals(category)) {
            Map<String, Object> map = (Map<String, Object>) jsonMap.get("value");
            Junction parentJunction = null;
            List<Criterion> conditions = this.getContent(map, true, parentJunction, null);
            String type = MapUtils.getString(map, "type", "and");
            Criterion junctionType = getJunctionByType(type);
            Lhs lhs = null;
            if(junctionType instanceof And) {
                lhs = Lhs.instance().setCriterion(And.instance().setCriterions(true, conditions));
            }else {
                lhs = Lhs.instance().setCriterion(Or.instance().setCriterions(true, conditions));
            }

            System.out.println();
        }
    }


    public List<Criterion> getContent(Map<String, Object> param, boolean isRoot, Criterion parentJunction,List<Criterion> junctions) {
        //1、判断type如果为空设置默认值and 单个参数默认为and 多个参数可能是and或者or
        String type = MapUtils.getString(param, "type", "and");
        //2、获取参数列表
        List<Map<String, Object>> list = (List<Map<String, Object>>) param.get("list");
        Criterion junction = getJunctionByType(type);

        //3、便利参数列表
        List<Criterion> criterias = Lists.newArrayList();
        for (Map<String, Object> para : list) {
            //MKT_RULE 拼装规则时不需要 mvelOper 和 mvelValue
            if ("mkt_rule".equals(MapUtils.getString(para, "bizType", "")) || "hit_group".equals(MapUtils.getString(para, "bizType", "")) || "feature_enhance".equals(MapUtils.getString(para, "bizType", ""))) {
                para.put("mvelOper", "");
                para.put("mvelValue", "");
            }

            //4、解析多重条件
            if (para.containsKey("list")) {
                junctions = getContent(para, false, junction, junctions);
            } else {
                //1、表达式运算符处理
                String mvelOper;
                //5、如果操作表达式符号是固定的值，侧翻译替换成指定的符号
                if ("true".equals(MapUtils.getString(para, "mvelOper", ""))) {
                    mvelOper = "==";
                } else if ("false".equals(MapUtils.getString(para, "mvelOper", ""))) {
                    mvelOper = "!=";
                } else {
                    if (StringUtils.isBlank(MapUtils.getString(para, "mvelOper", ""))) {
                        mvelOper = "==";
                    } else {
                        mvelOper = MapUtils.getString(para, "mvelOper", "");
                    }
                }
                //6、获取前端设置的表达式的值
                String mvelValue = MapUtils.getString(para, "mvelValue", "true");
                //2.1空值处理
                if (StringUtils.isBlank(mvelValue)) {
                    mvelValue = "true";
                }

                String condition = "";
                //7、如果前端设置的值是多个（一般以"["开头结尾的）则便利拼装成 （a==b||c==d）
                if (getMvelValue(mvelValue) instanceof List) {
                    List<String> conditions = Lists.newArrayList();
                    ((List) getMvelValue(mvelValue)).forEach(l -> {
                        conditions.add("request.headers." + MapUtils.getString(para, "mvelKey") + mvelOper + l);
                    });
                    condition = " ( " + Joiner.on("||").skipNulls().join(conditions) + " ) ";
                } else {
                    //字符串处理
                    if (Constant.EQUAL.equals(mvelOper)) {
                        condition = "request.headers." + MapUtils.getString(para, "mvelKey") + " == '" + mvelValue + "'";
                    } else if (Constant.CONTAINS.equals(mvelOper)) {
                        condition = "request.headers." + MapUtils.getString(para, "mvelKey") + " " + mvelOper + " '" + mvelValue + "'";
                    } else if (Constant.NO_CONTAINS.equals(mvelOper)) {
                        condition = "!(request.headers." + MapUtils.getString(para, "mvelKey") + " " + Constant.CONTAINS + " '" + mvelValue + "')";
                    } else if (Constant.ED_CONTAINS.equals(mvelOper)) {
                        mvelValue = handleStr(mvelValue);
                        condition = "{" + mvelValue + "} " + Constant.CONTAINS + " " + "request.headers." + MapUtils.getString(para, "mvelKey");
                    } else if (Constant.ED_NO_CONTAINS.equals(mvelOper)) {
                        mvelValue = handleStr(mvelValue);
                        condition = "!({" + mvelValue + "} " + Constant.CONTAINS + " " + "request.headers." + MapUtils.getString(para, "mvelKey") + ")";
                    } else {
                        //非字符串处理
                        condition = "request.headers." + MapUtils.getString(para, "mvelKey") + mvelOper + mvelValue;
                    }
                }

                //todo
                Parameter parameter = BizUtils.buildSimpleParameter("username", Datatype.String, mvelValue);
                /**规则组1*/
                /**规则组1--规则1*/
                MethodLeftPart leftPart = BizUtils.buildMethodLeftPart("methodTest", "evalTest", parameter);
                Criteria criteria = Criteria.instance()
                        .setLeft(Left.instance(leftPart))
                        .setOp(Op.Equals)
                        .setValue(SimpleValue.instance("testValue"));
                criterias.add(criteria);
            }


        }

        if(CollectionUtils.isNotEmpty(criterias)) {
            if(junction instanceof And) {
                And and = ((And) junction).setCriterions(isRoot, criterias);
                if(null != parentJunction) {
                    if(parentJunction instanceof Or) {
                        ((Or)parentJunction).setCriterions(isRoot, criterias);
                        if(null == junctions) {
                            junctions = Lists.newArrayList();
                        }
                        junctions.add(and);
                    }else {
                        ((And)parentJunction).setCriterions(isRoot, criterias);
                        if(null == junctions) {
                            junctions = Lists.newArrayList();
                        }
                        junctions.add(and);
                    }
                }
            }else if(junction instanceof Or) {
                Or or = ((Or) junction).setCriterions(isRoot, criterias);
                if(null != parentJunction) {
                    if(parentJunction instanceof Or) {
                        ((Or)parentJunction).setCriterions(isRoot, criterias);
                        if(null == junctions) {
                            junctions = Lists.newArrayList();
                        }
                        junctions.add(or);

                    }else {
                        ((And)parentJunction).setCriterions(isRoot, criterias);
                        if(null == junctions) {
                            junctions = Lists.newArrayList();
                        }
                        junctions.add(or);
                    }
                }
            }
        }
        return junctions;
    }

    private Object getMvelValue(String mvelValue){
        if(StringUtils.isBlank(mvelValue)){
            return null;
        }
        if(StringUtils.startsWith(mvelValue,"[")&&StringUtils.endsWith(mvelValue,"]")){
            mvelValue=mvelValue.substring(1,mvelValue.length()-1);
            return Splitter.on(Constant.COMMA).omitEmptyStrings().splitToList(mvelValue);
        }else{
            return mvelValue;
        }
    }

    public String handleStr(String mvelValue){
        StringBuilder sb = new StringBuilder();
        String[] split = StringUtils.split(mvelValue, ",");
        for (String s : split) {
            sb.append("\"").append(s).append("\"").append(",");
        }
        String result = sb.toString();
        return result.substring(0,result.length()-1);
    }

    /**
     * 获取方法信息
     * @param mvelKey
     */
    private void getMethod(String mvelKey){

    }

    private Criterion getJunctionByType(String type){
        if(StringUtils.equals("and", type)) {
            return And.instance();
        }else {
            return Or.instance();
        }
    }


    public interface Constant {
        /**
         * 符号
         */
        String COMMA = ",";
        String MIDLINE = "-";
        String UNDERLINE = "_";
        String COLON = ":";
        String DOT = ".";
        String SPECIAL_SEPARATOR = "#";

        /**
         * 包含被包含定义
         */
        String EQUAL = "equal";
        String CONTAINS = "contains";
        String NO_CONTAINS = "!contains";
        String ED_CONTAINS = "<contains";
        String ED_NO_CONTAINS = "<!contains";

    }
}
