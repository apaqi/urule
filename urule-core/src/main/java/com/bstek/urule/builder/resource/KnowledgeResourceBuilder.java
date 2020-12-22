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
package com.bstek.urule.builder.resource;

import com.bstek.urule.model.rule.RuleSet;
import com.bstek.urule.parse.deserializer.KnowledgeDeserializer;
import com.bstek.urule.parse.deserializer.RuleSetDeserializer;
import org.dom4j.Element;

/**
 * @Description
 *
 * @Author wpx
 * @Date 2020/12/22 17:06
 */
public class KnowledgeResourceBuilder implements ResourceBuilder<RuleSet> {
	private KnowledgeDeserializer knowledgeDeserializer;
	public RuleSet build(Element root) {
		return knowledgeDeserializer.deserialize(root);
	}
	public boolean support(Element root) {
		return knowledgeDeserializer.support(root);
	}
	public ResourceType getType() {
		return ResourceType.RuleSet;
	}

	public void setKnowledgeDeserializer(KnowledgeDeserializer knowledgeDeserializer) {
		this.knowledgeDeserializer = knowledgeDeserializer;
	}
}
