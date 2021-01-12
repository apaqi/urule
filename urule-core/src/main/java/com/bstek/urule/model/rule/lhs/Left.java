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
package com.bstek.urule.model.rule.lhs;

import com.bstek.urule.RuleException;
import com.bstek.urule.model.rete.JsonUtils;
import com.bstek.urule.model.rule.SimpleArithmetic;

/**
 * @author Jacky.gao
 * @since 2014年12月29日
 */
public class Left {
    private String id;
    private LeftPart leftPart;
    private LeftType type;
    private SimpleArithmetic arithmetic;

    /**
     * 获取Left实例
     *
     * @return Left Left
     */
    public static Left instance() {
        return new Left();
    }

    /**
     * 根据leftPart构造Left
     *
     * @param leftPart leftPart
     * @return Left Left
     */
    public static Left instance(LeftPart leftPart) {
        Left left = new Left();
        if (null != leftPart) {
            left.setLeftPart(leftPart);
            if (leftPart instanceof VariableLeftPart) {
                left.setType(LeftType.variable);
            } else if (leftPart instanceof MethodLeftPart) {
                left.setType(LeftType.method);
            } else if (leftPart instanceof FunctionLeftPart) {
                left.setType(LeftType.function);
            } else if (leftPart instanceof EvalLeftPart) {
				left.setType(LeftType.eval);
			} else if (leftPart instanceof AllLeftPart) {
                left.setType(LeftType.all);
            } else if (leftPart instanceof ExistLeftPart) {
                left.setType(LeftType.exist);
            } else if (leftPart instanceof CommonFunctionLeftPart) {
                left.setType(LeftType.commonfunction);
            } else if (leftPart instanceof ScriptMethodLeftPart) {
                left.setType(LeftType.scriptMethod);
            }else {
            	throw new RuleException("does not support current leftPart,id=" + leftPart.getId());
			}
        }
        return left;
    }

    public LeftPart getLeftPart() {
        return leftPart;
    }

    public Left setLeftPart(LeftPart leftPart) {
        this.leftPart = leftPart;
        return this;
    }

    public SimpleArithmetic getArithmetic() {
        return arithmetic;
    }

    public Left setArithmetic(SimpleArithmetic arithmetic) {
        this.arithmetic = arithmetic;
        return this;
    }

    public LeftType getType() {
        return type;
    }

    public Left setType(LeftType type) {
        this.type = type;
        return this;
    }

    public String getId() {
        if (id == null) {
            id = leftPart.getId();
            if (arithmetic != null) {
                id = id + arithmetic.getId();
            }
        }
        return id;
    }
}
