package org.goldenegg.LScript.Types;

import org.goldenegg.LScript.LSValue;

import org.goldenegg.LScript.LSErrors.LSError;

public class LInt extends LSValue {

    private Integer Value;

    public LInt(Integer value) {
        Value = value;
    }

    public LFloat toFloat() {
        return new LFloat(Value.floatValue());
    }

    @Override
    public void setValue(LSValue value) throws LSError {
        Value = value.getValue(Integer.class);
    }

    @Override
    public LString getType() {
        return new LString("Integer");
    }

    public String toString() {
        return "Int: " + Value + "";
    }
}