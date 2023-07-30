package org.goldenegg.LScript;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.goldenegg.LScript.LSErrors.*;
import org.goldenegg.LScript.LSParser.ASTNode;
import org.goldenegg.LScript.Types.LFunction;
import org.goldenegg.LScript.Types.LNull;
import org.goldenegg.LScript.Types.LObject;
import org.goldenegg.LScript.Types.LString;
import org.goldenegg.LScript.Types.LVariable;

public class LSInterpreter {
    private HashMap<String, LSValue> globalVariables = new HashMap<>();

    public void compile(String code) throws LSError { // runs global code
        runCode(code, globalVariables, new HashMap<>(), true);
    }

    public void compile(String code, HashMap<String, LSValue> locals) throws LSError { // runs global code
        runCode(code, globalVariables, locals, true);
    }

    public LSValue runCode(String code, HashMap<String, LSValue> locals) throws LSError { // runs function
        return runCode(code, globalVariables, locals, false);
    }

    public void compile(ArrayList<ASTNode> code) throws LSError { // runs global code
        runCode(code, globalVariables, new HashMap<>(), true);
    }

    public void compile(ArrayList<ASTNode> code, HashMap<String, LSValue> locals) throws LSError { // runs global code
        runCode(code, globalVariables, locals, true);
    }

    public LSValue runCode(ArrayList<ASTNode> code, HashMap<String, LSValue> locals) throws LSError { // runs function
        return runCode(code, globalVariables, locals, false);
    }

    private LSValue runCode(String code, HashMap<String, LSValue> globals, HashMap<String, LSValue> locals,
            boolean isGlobal)
            throws LSError {
        var tokens = LSTokenizer.tokenize(code);
        // var spacingCount = 0;

        // for (var token : tokens) {
        // System.out.println("\t".repeat(spacingCount) + token.toString());
        // if (token.getToken() == TokenEnum.codeBlockOpen)
        // spacingCount++;
        // else if (token.getToken() == TokenEnum.codeBlockClose)
        // spacingCount--;
        // }
        var tree = LSParser.parse(tokens);

        // for (ASTNode astNode : tree) {
        // System.out.println(astNode);
        // }

        return runCode(tree, globals, locals, isGlobal);
    }

    private LSValue runCode(ArrayList<ASTNode> code, HashMap<String, LSValue> globals, HashMap<String, LSValue> locals,
            boolean isGlobal)
            throws LSError {
        for (var itm : code) {
            if (itm instanceof LSParser.Import) {
                var item = itm.castToType(LSParser.Import.class);

                var val = getImport(item.name);
                if (val == null)
                    throw new ImportNotFoundException();
                if (isGlobal)
                    globals.put(item.name, val);
                else
                    locals.put(item.name, val);
            }

            else if (itm instanceof LSParser.CreateFunction) {
                var item = itm.castToType(LSParser.CreateFunction.class);

                var val = new LFunction(item.code, item.args);
                if (isGlobal)
                    globals.put(item.name, val);
                else
                    locals.put(item.name, val);
            }

            else if (itm instanceof LSParser.setVariable) {
                var item = itm.castToType(LSParser.setVariable.class);
                LSValue val = null;

                val = LString.toValue(item.val);
                if (val == null) {
                    val = LVariable.toValue(item.val);
                    if (val != null)
                        val = val.toType(LVariable.class).getValueFromVariable(globals, locals);
                }

                if (val != null)
                    if (isGlobal)
                        globals.put(item.name, val);
                    else
                        locals.put(item.name, val);
                else
                    throw new LSErrors.InvalidEndOfStatementException("could not get the value from: " + item.val);
            }

            else if (itm instanceof LSParser.CallFunction) {
                var item = itm.castToType(LSParser.CallFunction.class);

                var thing = item.name.split("\\.");

                var val = globals.get(thing[0]);
                if (val == null)
                    val = locals.get(thing[0]);
                // System.out.println(item.args);

                if (val == null)
                    throw new VariableNotFound(item.name);

                for (int i = 1; i < thing.length; i++) {
                    val = val.children.get(thing[i]);
                    if (val == null)
                        throw new VariableNotFound(item.name + " : " + thing[i]);
                }

                var valuedArgs = new ArrayList<>(item.args); // added Arraylist to copy it before modifying it!!!!

                for (int i = 0; i < valuedArgs.size(); i++) {
                    if (valuedArgs.get(i) instanceof LVariable) {
                        valuedArgs.set(i,
                                valuedArgs.get(i).toType(LVariable.class).getValueFromVariable(globals, locals));
                    }
                }

                val.toType(LFunction.class).call(valuedArgs, this);
            }

            else {
                throw new InvalidOperationException("not implemented: " + itm);
            }
        }

        globalVariables = globals;

        // System.out.println(globalVariables);

        return null;
    }

    public LSValue getImport(String name) {
        return null;
    }

    public LSValue getGlobalVariable(String name) {
        return globalVariables.get(name);
    }

    public String globalVariablesToString() {
        return globalVariables.toString();
    }

    public void setGlobalVariable(String name, LSValue value) {
        globalVariables.put(name, value);
    }

    // LScript std Implementation
    protected LFunction print = new LFunction(arg -> {
        if (arg.get("val") instanceof LString)
            try {
                var out = arg.get("val").toType(LString.class).getString();
                if (out == null)
                    return new LNull();
                out = out.replace("\\n", "\n");
                out = out.replace("\\t", "\t");
                out = out.replace("\\s", "\s");
                System.out.println(out);
            } catch (LSError f) {
                f.printStackTrace();
            }
        else
            System.out.println(arg.get("val"));
        return new LNull();
    }, new ArrayList<>(Arrays.asList(new String[] { "val" })));

    protected LObject std = new LObject();
    {
        std.children.put("print", print);
    }
}