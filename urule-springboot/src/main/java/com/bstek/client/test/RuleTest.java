package com.bstek.client.test;

import com.bstek.client.Utils;
import com.bstek.client.libary.Datatype;
import com.bstek.client.rule.Op;
import com.bstek.client.rule.Parameter;
import com.bstek.client.rule.SimpleValue;
import com.bstek.client.rule.lhs.*;
import com.bstek.urule.BizUtils;
import com.bstek.urule.model.rule.Rhs;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wpx
 * @Description 类注释
 * @date 2020/12/28
 */
public class RuleTest {
    @Test
    public void testLhs() {

        Parameter parameter = Utils.buildSimpleParameter("username", Datatype.String, "123");
        /**规则组1*/
        /**规则组1--规则1*/
        MethodLeftPart leftPart = buildMethodLeftPart("methodTest", "evalTest", parameter);
        Criteria criteria1 = Criteria.instance()
                .left(Left.instance().leftPart(leftPart).type(LeftType.method))
                .op(Op.Equals)
                .value(SimpleValue.instance().content("testValue"));

        /**规则组1--规则2*/
        MethodLeftPart leftPart2 = buildMethodLeftPart("methodTest", "evalTest", parameter);
        Criteria criteria2 = Criteria.instance()
                .left(Left.instance().leftPart(leftPart2).type(LeftType.method))
                .op(Op.Equals)
                .value(SimpleValue.instance().content("testValue"));
        And and = And.instance().addCriterion(false, criteria1, criteria2);
        /**规则组2*/
        /**规则组2 中的规则1*/
        Criteria orCriteria1 = Criteria.instance()
                .left(Left.instance().leftPart(leftPart).type(LeftType.method))
                .op(Op.Equals)
                .value(SimpleValue.instance().content("testValue"));
        /**规则组2 中的规则2*/
        Criteria orCriteria2 = Criteria.instance()
                .left(Left.instance().leftPart(leftPart).type(LeftType.method))
                .op(Op.Equals)
                .value(SimpleValue.instance().content("testValue"));
        Or or = Or.instance().addCriterion(false, orCriteria1, orCriteria2);

        Lhs lhs = Lhs.instance().setCriterion(And.instance().addCriterion(true, and, or));

        Assert.assertNotNull(lhs, "criteria cannot be null");
    }

    public static MethodLeftPart buildMethodLeftPart(String beanId, String methodId, Parameter... parameters) {
        MethodLeftPart leftPart = new MethodLeftPart();
        leftPart.setBeanId(beanId);
        leftPart.setMethodName(methodId);
        List<Parameter> parameterList = new ArrayList<>();
        for (int i = 0; i < parameters.length; i++) {
            parameterList.add(parameters[i]);
        }
        leftPart.setParameters(parameterList);
        return leftPart;
    }
}
