package com.bstek.urule.script;

import java.util.HashMap;
import java.util.Map;

public enum ScriptType {
    JSONPATH,
    AVIATOR
    //,GROOVY
    ;

    private final static Map<String, ScriptType> TYPE = new HashMap<>(2);

    static {
        for (ScriptType s : ScriptType.values()) {
            TYPE.put(s.name(), s);
        }
    }

    public static ScriptType getByName(String name) {
        return TYPE.get(name);
    }
}
