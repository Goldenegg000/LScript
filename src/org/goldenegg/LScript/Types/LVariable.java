package org.goldenegg.LScript.Types;

import java.util.HashMap;

import org.goldenegg.LScript.LSValue;

public class LVariable extends LSValue {

    public String name;

    public LVariable(String name) {
        this.name = name;
    }

    public LSValue getValueFromVariable(HashMap<String, LSValue> global, HashMap<String, LSValue> local) {
        return local.get(name) != null ? local.get(name) : global.get(name);
    }

    @Override
    public LString getType() {
        return new LString("Variable");
    }

    public String toString() {
        return "Variable: " + name;
    }
}