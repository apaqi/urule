package com.bstek.urule.test;

import com.bstek.urule.BizUtils;
import com.bstek.urule.model.library.Datatype;
import com.bstek.urule.model.rule.Op;
import com.bstek.urule.model.rule.Parameter;
import com.bstek.urule.model.rule.SimpleValue;
import com.bstek.urule.model.rule.lhs.*;

/**
 * @author wpx
 * @Description 类注释
 * @date 2020/12/29
 */
public class UruleTest {
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
}
