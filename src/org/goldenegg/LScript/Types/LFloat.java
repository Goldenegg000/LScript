package org.goldenegg.LScript.Types;

import org.goldenegg.LScript.LSValue;

import org.goldenegg.LScript.LSErrors.LSError;

public class LFloat extends LSValue {

    private Float Value;

    public LFloat(Float value) {
        Value = value;
    }

    public LInt toLInt() {
        return new LInt(Math.round(Value));
    }

    @Override
    public void setValue(LSValue value) throws LSError {
        Value = value.getValue(Float.class);
    }

    @Override
    public LFloat addValue(LSValue value) throws LSError {
        return new LFloat(Value + value.getValue(Float.class));
    }

    @Override
    public LFloat subtractValue(LSValue value) throws LSError {
        return new LFloat(Value - value.getValue(Float.class));
    }

    @Override
    public LFloat multiplyValue(LSValue value) throws LSError {
        return new LFloat(Value * value.getValue(Float.class));
    }

    @Override
    public LSValue divideValue(LSValue value) throws LSError {
        return new LFloat(Value / value.getValue(Float.class));
    }

    @Override
    public LString getType() {
        return new LString("Float");
    }

    public String toString() {
        return "Float: " + Value + "";
    }
}