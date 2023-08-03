package org.goldenegg.LScript.Types;

import org.goldenegg.LScript.LSErrors.InvalidTypeException;
import org.goldenegg.LScript.LSErrors.LSError;
import org.goldenegg.LScript.LSTokenizer.Token;
import org.goldenegg.LScript.LSTokenizer.Token.TokenEnum;
import org.goldenegg.LScript.LSValue;

public class LBoolean extends LSValue {

    private Boolean val;

    public LBoolean(Boolean val) {
        this.val = val;
    }

    public boolean getBoolean() {
        return val;
    }

    @Override
    public <T> T getValue(Class<T> cazz) throws LSError {
        try {
            return cazz.cast(val);
        } catch (ClassCastException e) {
            throw new InvalidTypeException();
        }
    }

    public static LSValue toValue(Token value) throws LSError {
        if (value.getToken() == TokenEnum.booleanStatement)
            return new LBoolean(value.getValue().equals("true"));
        return null;
    }

    @Override
    public LString getType() {
        return new LString("Boolean");
    }

    public String toString() {
        return "Boolean: " + val;
    }
}