package com.bstek.urule.script;

import com.jayway.jsonpath.JsonPath;

/**
 * JsonPathActionInvoker
 */
public class JsonPathActionInvoker extends AbstractScriptActionInvoker {

    private JsonPath jsonPath;

    public JsonPathActionInvoker(JsonPathAction jsonPathAction) {
        super(jsonPathAction);
        this.jsonPath = JsonPath.compile(jsonPathAction.getExpression());
    }

    @Override
    public Object invoke(Object context) {
        return this.jsonPath.read(context);
    }
}
