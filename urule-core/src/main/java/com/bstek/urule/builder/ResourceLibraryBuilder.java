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
package com.bstek.urule.builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.bstek.urule.model.library.Datatype;
import com.bstek.urule.model.library.action.ActionConfig;
import com.bstek.urule.model.library.variable.*;
import org.dom4j.Element;

import com.bstek.urule.builder.resource.Resource;
import com.bstek.urule.builder.resource.ResourceBuilder;
import com.bstek.urule.builder.resource.ResourceType;
import com.bstek.urule.model.library.ResourceLibrary;
import com.bstek.urule.model.library.action.ActionLibrary;
import com.bstek.urule.model.library.action.SpringBean;
import com.bstek.urule.model.library.constant.ConstantLibrary;
import com.bstek.urule.model.rule.Library;
import com.bstek.urule.runtime.BuiltInActionLibraryBuilder;
import org.springframework.util.CollectionUtils;

/**
 * @author Jacky.gao
 * @since 2015年2月16日
 */
public class ResourceLibraryBuilder extends AbstractBuilder {
    private BuiltInActionLibraryBuilder builtInActionLibraryBuilder;

    @SuppressWarnings("unchecked")
    public ResourceLibrary buildResourceLibrary(Collection<Library> libraries) {
        if (libraries == null) {
            libraries = Collections.EMPTY_LIST;
        }
        List<ConstantLibrary> constantLibraryLibs = new ArrayList<ConstantLibrary>();
        List<ActionLibrary> actionLibraryLibs = new ArrayList<ActionLibrary>();
        List<VariableLibrary> variableCategoryLibs = new ArrayList<VariableLibrary>();
        List<VariableCategory> parameterVariableCategories = new ArrayList<VariableCategory>();
        ResourceBase resourceBase = newResourceBase();
        for (Library lib : libraries) {
            resourceBase.addResource(lib.getPath(), lib.getVersion());
        }
        for (Resource resource : resourceBase.getResources()) {
            String content = resource.getContent();
            Element root = parseResource(content);
            for (ResourceBuilder<?> builder : resourceBuilders) {
                if (!builder.support(root)) {
                    continue;
                }
                Object object = builder.build(root);
                ResourceType type = builder.getType();
                if (type.equals(ResourceType.ActionLibrary)) {
                    ActionLibrary al = (ActionLibrary) object;
                    actionLibraryLibs.add(al);
                } else if (type.equals(ResourceType.VariableLibrary)) {
                    VariableLibrary vl = (VariableLibrary) object;
                    variableCategoryLibs.add(vl);
                } else if (type.equals(ResourceType.ConstantLibrary)) {
                    ConstantLibrary cl = (ConstantLibrary) object;
                    constantLibraryLibs.add(cl);
                } else if (type.equals(ResourceType.ParameterLibrary)) {
                    VariableCategory category = (VariableCategory) object;
                    parameterVariableCategories.add(category);
                }
                break;
            }
        }
        if (parameterVariableCategories.size() > 0) {
            VariableCategory category = parameterVariableCategories.get(0);
            for (VariableCategory vc : parameterVariableCategories) {
                if (vc.equals(category)) {
                    continue;
                }
                if (vc.getVariables() == null) {
                    continue;
                }
                for (Variable v : vc.getVariables()) {
                    category.addVariable(v);
                }
            }
            VariableLibrary parameterLib = new VariableLibrary();
            parameterLib.addVariableCategory(category);
            variableCategoryLibs.add(parameterLib);
        }
        List<SpringBean> builtInActions = builtInActionLibraryBuilder.getBuiltInActions();
        //todo todo todo todo
        //此处加载规则以来的bean
        builtInActions = builtInActionLibraryBuilder.addActionBean("methodTest");
        if (builtInActions.size() > 0) {
            ActionLibrary al = new ActionLibrary();
            al.setSpringBeans(builtInActions);
            actionLibraryLibs.add(al);
        }
        return new ResourceLibrary(variableCategoryLibs, actionLibraryLibs, constantLibraryLibs);
    }

    @SuppressWarnings("unchecked")
    public ResourceLibrary buildResourceLibrary(List<ActionConfig> actionConfigs, List<Variable> variables) {
        List<SpringBean> builtInActions = builtInActionLibraryBuilder.getBuiltInActions();
        if(!CollectionUtils.isEmpty(actionConfigs)) {
            for (ActionConfig actionConfig : actionConfigs) {
                builtInActions = builtInActionLibraryBuilder.addActionBean(actionConfig.getActionFlag());
            }
        }

        List<VariableLibrary> variableCategoryLibs = new ArrayList<VariableLibrary>();
        if(!CollectionUtils.isEmpty(variables)) {
            //依赖的变量
            VariableLibrary variableLibrary = new VariableLibrary();
            //依赖的变量->变量类型，只支持map结构
            List<VariableCategory> variableCategories = new ArrayList<>();
            VariableCategory variableCategory = new VariableCategory();
            variableCategory.setClazz("java.util.HashMap");
            variableCategory.setName("参数");
            variableCategory.setType(CategoryType.Clazz);
            //依赖的变量->变量信息
            variableCategory.setVariables(variables);
            variableCategories.add(variableCategory);
            variableLibrary.setVariableCategories(variableCategories);
            variableCategoryLibs.add(variableLibrary);
        }

        List<ActionLibrary> actionLibraryLibs = new ArrayList<ActionLibrary>();
        if (builtInActions.size() > 0) {
            ActionLibrary al = new ActionLibrary();
            al.setSpringBeans(builtInActions);
            actionLibraryLibs.add(al);
        }
        return new ResourceLibrary(variableCategoryLibs, actionLibraryLibs, new ArrayList<ConstantLibrary>());
    }

    public void setBuiltInActionLibraryBuilder(
            BuiltInActionLibraryBuilder builtInActionLibraryBuilder) {
        this.builtInActionLibraryBuilder = builtInActionLibraryBuilder;
    }
}
