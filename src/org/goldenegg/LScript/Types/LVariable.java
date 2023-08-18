package org.goldenegg.LScript.Types;

import java.util.ArrayList;
import java.util.Collections;

import org.goldenegg.LScript.LSErrors.LSError;
import org.goldenegg.LScript.LSInterpreter.Scope;
import org.goldenegg.LScript.LSTokenizer.Token;
import org.goldenegg.LScript.LSTokenizer.Token.TokenEnum;
import org.goldenegg.LScript.LSInterpreter;
import org.goldenegg.LScript.LSValue;

public class LVariable extends LSValue {

    public String name;

    public LVariable(String name) {
        this.name = name;
    }

    public LSValue getValueFromVariable(ArrayList<Scope> scopes) {
        return LSInterpreter.getValueFromScope(name, scopes);
        // return local.get(name) != null ? local.get(name) : scopes.get(name);
    }

    public static LSValue toValue(Token value) throws LSError {
        if (value.getToken() == TokenEnum.name)
            return new LVariable(value.getValue());
        return null;
    }

    @Override
    public LString getType() {
        return new LString("Variable");
    }

    public String toString() {
        return "Variable: " + name;
    }
}