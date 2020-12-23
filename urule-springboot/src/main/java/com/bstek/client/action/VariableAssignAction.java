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
package com.bstek.client.action;

import com.bstek.client.libary.Datatype;
import com.bstek.client.rule.Value;
import com.bstek.client.rule.lhs.LeftType;


/**
 * @author Jacky.gao
 * @since 2014年12月22日
 */
public class VariableAssignAction extends AbstractAction {
	//for script rule
    private String referenceName;
    private String variableName;
    private String variableLabel;
    private String variableCategory;
    private Datatype datatype;
    private Value value;
    private LeftType type;
    private ActionType actionType = ActionType.VariableAssign;


    public LeftType getType() {
        return type;
    }

    public void setType(LeftType type) {
        this.type = type;
    }

    public String getReferenceName() {
        return referenceName;
    }

    public void setReferenceName(String referenceName) {
        this.referenceName = referenceName;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public String getVariableLabel() {
        return variableLabel;
    }

    public void setVariableLabel(String variableLabel) {
        this.variableLabel = variableLabel;
    }

    public String getVariableCategory() {
        return variableCategory;
    }

    public void setVariableCategory(String variableCategory) {
        this.variableCategory = variableCategory;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public Datatype getDatatype() {
        return datatype;
    }

    public void setDatatype(Datatype datatype) {
        this.datatype = datatype;
    }
}
