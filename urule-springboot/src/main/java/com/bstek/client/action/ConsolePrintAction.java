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

import com.bstek.client.rule.Value;

/**
 * @Description  测试专用，控制台输出
 *
 * @Author wpx
 * @Date 2020/12/23 16:16
 */
public class ConsolePrintAction extends AbstractAction {
	private Value value;
	private ActionType actionType=ActionType.ConsolePrint;

	public Value getValue() {
		return value;
	}

	public void setValue(Value value) {
		this.value = value;
	}
	public ActionType getActionType() {
		return actionType;
	}
}
