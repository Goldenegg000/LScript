package org.goldenegg.LScript.Types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;

import org.goldenegg.LScript.LSInterpreter;
import org.goldenegg.LScript.LSParser.ASTNode;
import org.goldenegg.LScript.LSValue;
import org.goldenegg.LScript.LSErrors.InvalidArgumentLengthException;
import org.goldenegg.LScript.LSErrors.InvalidOperationException;
import org.goldenegg.LScript.LSErrors.InvalidTypeException;
import org.goldenegg.LScript.LSErrors.LSError;
import org.goldenegg.LScript.LSInterpreter.Scope;

public class LFunction extends LSValue {

    public static class FunctionParams {
        public HashMap<String, LSValue> args;
        public ArrayList<Scope> scopes;

        public FunctionParams(HashMap<String, LSValue> args, ArrayList<Scope> scopes) {
            this.args = args;
            this.scopes = scopes;
        }
    }

    private ArrayList<ASTNode> code;
    private Function<FunctionParams, LSValue> altValue;

    private ArrayList<String> functionParameterNames;

    public LFunction(ArrayList<ASTNode> code, ArrayList<String> names) {
        altValue = null;
        this.code = code;
        functionParameterNames = names;
    }

    public LFunction(Function<FunctionParams, LSValue> code, ArrayList<String> names) {
        this.code = null;
        altValue = code;
        functionParameterNames = names;
    }

    public ArrayList<String> getParamNames() throws LSError {
        return functionParameterNames;
    }

    @Override
    public void setValue(LSValue value) throws LSError {
        var val = value.getValue(LFunction.class);
        if (val instanceof LFunction)
            code = ((LFunction) val).code;
        throw new InvalidTypeException();
    }

    @Override
    public LSValue call(ArrayList<LSValue> val, LSInterpreter self) throws LSError {
        var out = new HashMap<String, LSValue>();
        if (functionParameterNames.size() != val.size())
            throw new InvalidArgumentLengthException("invalid amount of arguments");
        for (LSValue lsVal : val) {
            out.put(functionParameterNames.get(out.size()), lsVal);
        }

        if (code != null) {
            return self.runCode(code, out);
        }
        if (altValue != null) {
            return altValue.apply(new FunctionParams(out, self.globalVariables));
        }
        throw new InvalidOperationException();
    }

    @Override
    public LString getType() {
        return new LString("Function");
    }

    // public LString getArgs() {
    // return functionParameterNames;
    // }

    public String toString() {
        if (code != null)
            return "(Function: " + code + ": " + functionParameterNames + ")";
        if (altValue != null) {
            return "(Function: " + altValue + ": " + functionParameterNames + ")";
        }
        return "Function: NULL";
    }
}