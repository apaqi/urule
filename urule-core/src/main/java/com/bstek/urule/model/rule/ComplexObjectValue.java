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
package com.bstek.urule.model.rule;

/**
 * 复杂对象支持
 * @author wpx

 */
public class ComplexObjectValue extends AbstractValue {
    private Object content;
    private ValueType valueType = ValueType.ComplexInput;

    @Override
    public ValueType getValueType() {
        return valueType;
    }

    public Object getContent() {
        return content;
    }

    public ComplexObjectValue setContent(Object content) {
        this.content = content;
        return this;
    }

    @Override
    public String getId() {
        String id = "[ComplexObjectValue]" + System.identityHashCode(content);
        if (arithmetic != null) {
            id += arithmetic.getId();
        }
        return id;
    }

    public static ComplexObjectValue instance() {
        return new ComplexObjectValue();
    }

    public static ComplexObjectValue instance(Object content) {
        ComplexObjectValue simpleValue = new ComplexObjectValue();
        simpleValue.setContent(content);
        return simpleValue;
    }
}
