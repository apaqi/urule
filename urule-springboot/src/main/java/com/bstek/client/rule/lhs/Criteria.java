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
package com.bstek.client.rule.lhs;


import com.bstek.client.rule.Op;
import com.bstek.client.rule.Value;

/**
 * @author Jacky.gao
 * @since 2014年12月29日
 */
public class Criteria extends BaseCriterion {
    private String id;
    private Op op;
    private Left left;
    private Value value;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Op getOp() {
        return op;
    }

    public void setOp(Op op) {
        this.op = op;
    }

    public Left getLeft() {
        return left;
    }

    public void setLeft(Left left) {
        this.left = left;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }
}
