package org.goldenegg.LScript;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import org.goldenegg.LScript.LSErrors.*;
import org.goldenegg.LScript.LSParser.ASTNode;
import org.goldenegg.LScript.Types.LBoolean;
import org.goldenegg.LScript.Types.LFunction;
import org.goldenegg.LScript.Types.LNull;
import org.goldenegg.LScript.Types.LObject;
import org.goldenegg.LScript.Types.LString;
import org.goldenegg.LScript.Types.LVariable;

public class LSInterpreter {

    public static class Scope extends HashMap<String, LSValue> {

        public Scope() {
            super();
        }

        public Scope(HashMap<String, LSValue> locals) {
            for (var itm : locals.entrySet()) {
                put(itm.getKey(), itm.getValue());
            }
        }
    }

    public ArrayList<Scope> globalVariables = new ArrayList<Scope>();

    public void compile(String code) throws LSError { // runs global code
        runCode(code, true);
    }

    public void compile(String code, Scope locals) throws LSError { // runs global code
        globalVariables.add(locals);
        runCode(code, true);
    }

    public LSValue runCode(String code, Scope locals) throws LSError { // runs function
        globalVariables.add(locals);
        return runCode(code, false);
    }

    public void compile(ArrayList<ASTNode> code) throws LSError { // runs global code
        runCode(code, true);
    }

    public void compile(ArrayList<ASTNode> code, HashMap<String, LSValue> locals) throws LSError { // runs global code
        globalVariables.add(new Scope(locals));
        runCode(code, true);
    }

    public LSValue runCode(ArrayList<ASTNode> code, HashMap<String, LSValue> locals) throws LSError { // runs function
        globalVariables.add(new Scope(locals));
        return runCode(code, false);
    }

    private LSValue runCode(String code,
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

        globalVariables.add(new Scope());
        return runCode(tree, isGlobal);
    }

    private LSValue runCode(ArrayList<ASTNode> code,
            boolean isGlobal)
            throws LSError {
        for (var itm : code) {
            if (itm instanceof LSParser.Import) {
                var item = itm.castToType(LSParser.Import.class);

                var val = getImport(item.name);
                if (val == null)
                    throw new ImportNotFoundException();
                setValueFromScopes(item.name, val, globalVariables);
                // if (isGlobal)
                // globals.put(item.name, val);
                // else
                // locals.put(item.name, val);
            }

            else if (itm instanceof LSParser.CreateFunction) {
                var item = itm.castToType(LSParser.CreateFunction.class);

                var val = new LFunction(item.code, item.args);
                setValueFromScopes(item.name, val, globalVariables);
                // if (isGlobal)
                // globals.put(item.name, val);
                // else
                // locals.put(item.name, val);
            }

            else if (itm instanceof LSParser.setVariable) {
                var item = itm.castToType(LSParser.setVariable.class);
                LSValue val = item.val;
                if (val instanceof LVariable) {
                    val = val.toType(LVariable.class).getValueFromVariable(globalVariables);
                }

                getLocalScope(globalVariables).put(item.name, item.val);
                // if (val != null)
                // if (isGlobal)
                // globals.put(item.name, val);
                // else {
                // if (globals.containsKey(item.name)) {
                // globals.put(item.name, val);
                // } else
                // locals.put(item.name, val);
                // }
                // else
                // throw new LSErrors.InvalidEndOfStatementException("could not get the value
                // from: " + item.val);
            }

            else if (itm instanceof LSParser.CallFunction) {
                var item = itm.castToType(LSParser.CallFunction.class);

                var thing = item.name.split("\\.");

                var val = getValueFromScopes(thing[0], globalVariables);
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
                                valuedArgs.get(i).toType(LVariable.class).getValueFromVariable(globalVariables));
                    }
                }

                globalVariables.add(new Scope());
                val.toType(LFunction.class).call(valuedArgs, this);
                globalVariables.remove(globalVariables.size() - 1);
            }

            else if (itm instanceof LSParser.IfBlock) {
                var item = itm.castToType(LSParser.IfBlock.class);

                // System.out.println(item);

                LSValue val = null;

                if (item.statement instanceof LVariable) {
                    val = item.statement.toType(LVariable.class).getValueFromVariable(globalVariables);
                }

                if (val == null)
                    throw new ValueIsNull(item.toString());

                // System.out.println(item.statement.getType().getString());
                if (val.getType().getString().equals("Boolean")) {
                    // System.out.println(item.statement);
                    if (val.getValue(Boolean.class)) {
                        globalVariables.add(new Scope());
                        this.runCode(item.code, false);
                        globalVariables.remove(globalVariables.size() - 1);
                    }
                }
            }

            else {
                throw new InvalidOperationException("not implemented: " + itm);
            }
        }

        // for (Scope scope : scopes) {
        // System.out.println(scope);
        // }

        return null;
    }

    public LSValue getImport(String name) {
        return null;
    }

    public LSValue getScopeVariable(Scope scope, String name) {
        return scope.get(name);
    }

    public String globalVariablesToString() {
        return globalVariables.toString();
    }

    public void setScopeVariable(Scope scope, String name, LSValue value) {
        scope.put(name, value);
    }

    public static LSValue getValueFromScopes(String name, ArrayList<Scope> scopes) {
        var reversedList = new ArrayList<>(scopes);
        Collections.reverse(reversedList);
        for (Scope scope : reversedList) {
            if (scope.get(name) != null) {
                return scope.get(name);
            }
        }
        return null;
    }

    public static void setValueFromScopes(String name, LSValue val, ArrayList<Scope> scopes) {
        var reversedList = new ArrayList<>(scopes);
        Collections.reverse(reversedList);
        for (Scope scope : reversedList) {
            if (scope.get(name) != null) {
                scope.put(name, val);
                return;
            }
        }
        getLocalScope(scopes).put(name, val);
    }

    public LSValue getVariable(String name) {
        return getValueFromScopes(name, this.globalVariables);
    }

    public void setVariable(String name, LSValue val) {
        setValueFromScopes(name, val, this.globalVariables);
    }

    public static Scope getLocalScope(ArrayList<Scope> scopes) {
        if (scopes.size() < 1)
            return null;
        return scopes.get(scopes.size() - 1);
    }

    public static Scope getGlobalScope(ArrayList<Scope> scopes) {
        return scopes.get(0);
    }

    // LScript std Implementation
    protected LFunction print = new LFunction(params -> {
        var arg = params.args;
        if (arg.get("val") instanceof LVariable) {
            try {
                arg.put("val", arg.get("val").toType(LVariable.class).getValueFromVariable(params.scopes));
            } catch (LSError e) {
                e.printStackTrace();
            }
        }
        if (arg.get("val") instanceof LString) {
            try {
                var out = arg.get("val").toType(LString.class).getString();
                if (out == null)
                    return new LNull();
                System.out.println(out);
            } catch (LSError f) {
                f.printStackTrace();
            }
        } else
            System.out.println("INVALID: " + arg.get("val"));
        return new LNull();
    }, new ArrayList<>(Arrays.asList(new String[] { "val" })));

    protected LObject std = new LObject();
    {
        std.children.put("print", print);
    }
}