package org.goldenegg.LScript.Types;

import org.goldenegg.LScript.LSValue;
import org.goldenegg.LScript.LSErrors.InvalidTypeException;
import org.goldenegg.LScript.LSErrors.LSError;
import org.goldenegg.LScript.LSTokenizer.Token;
import org.goldenegg.LScript.LSTokenizer.Token.TokenEnum;

public class LString extends LSValue {

    private String Value;

    public LString(String string) {
        string = string.replace("\\n", "\n");
        string = string.replace("\\t", "\t");
        string = string.replace("\\s", "\s");
        Value = string;
    }

    @Override
    public void setValue(LSValue value) throws LSError {
        var val = value.<String>getValue(String.class);
        if (val instanceof String)
            Value = (String) val;
        throw new InvalidTypeException();
    }

    @Override
    public LString addValue(LSValue value) throws LSError {
        var val = value.<String>getValue(String.class);
        if (val instanceof String)
            return new LString(Value + (String) val);
        throw new InvalidTypeException();
    }

    @Override
    public LString getType() {
        return new LString("String");
    }

    public String getString() {
        return Value;
    }

    public static LSValue toValue(Token value) throws LSError {
        if (value.getToken() == TokenEnum.stringToken)
            return new LString(value.getValue().substring(1, value.getValue().length() - 1));
        return null;
    }

    public String toString() {
        return "String: \"" + Value + "\"";
    }
}