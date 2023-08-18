package org.goldenegg.LScript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import org.goldenegg.LScript.LSErrors.*;
import org.goldenegg.LScript.LSTokenizer.Token;
import org.goldenegg.LScript.LSTokenizer.Token.TokenEnum;
import org.goldenegg.LScript.Types.LString;

public class LSValue {

    public HashMap<String, LSValue> children = new HashMap<String, LSValue>();

    public void setValue(LSValue value) throws LSError {
    };

    public LSValue call(ArrayList<LSValue> val, LSInterpreter self) throws LSError {
        throw new InvalidOperationException();
    };

    public <T> T getValue(Class<T> cazz) throws LSError {
        try {
            return cazz.cast(null);
        } catch (ClassCastException e) {
            throw new InvalidTypeException();
        }
    }

    public <T> T toType(Class<T> cazz) throws LSError {
        try {
            return cazz.cast(this);
        } catch (ClassCastException e) {
            throw new InvalidTypeException();
        }
    }

    public static LSValue toValue(Token value) throws LSError {
        return null;
    }

    public LString getType() {
        return new LString("Null");
    }
}