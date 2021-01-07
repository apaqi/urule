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

import java.util.List;

/**
 * @author Jacky.gao
 * @since 2014年12月29日
 */
public class Or extends Junction {
	@Override
	public String getJunctionType() {
		return JunctionType.or.name();
	}

	/**
	 * 添加 OR 条件规则项
	 * @param isRoot isRoot
	 * @param criterions criterions
	 * @return Or Or
	 */
	public Or addCriterion(boolean isRoot, Criterion... criterions) {
		if (null != criterions) {
			for (int i = 0, len = criterions.length; i < len; i++) {
				super.addCriterion(criterions[i]);
			}
		}
		if(!isRoot) {
			super.setParent(this);
		}

		return this;
	}

	/**
	 *添加 OR 条件规则项
	 *
	 * @param isRoot 是否是根节点 OR 条件
	 * @param criterions criterions
	 * @return Or Or
	 */
	public Or setCriterions(boolean isRoot, List<Criterion> criterions) {
		if (null != criterions) {
			super.setCriterions(criterions);
		}
		if(!isRoot) {
			super.setParent(this);
		}
		return this;
	}

	public static Or instance() {
		return new Or();
	}
}
