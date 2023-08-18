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

    private Scope globalVariables = new Scope();

    public void compile(String code) throws LSError { // runs global code
        var tmp = new ArrayList<Scope>();
        tmp.add(globalVariables);
        runCode(code, tmp, true);
    }

    public void compile(String code, Scope locals) throws LSError { // runs global code
        var tmp = new ArrayList<Scope>();
        tmp.add(globalVariables);
        tmp.add(locals);
        runCode(code, tmp, true);
    }

    public LSValue runCode(String code, Scope locals) throws LSError { // runs function
        var tmp = new ArrayList<Scope>();
        tmp.add(globalVariables);
        tmp.add(locals);
        return runCode(code, tmp, false);
    }

    public void compile(ArrayList<ASTNode> code) throws LSError { // runs global code
        var tmp = new ArrayList<Scope>();
        tmp.add(globalVariables);
        runCode(code, tmp, true);
    }

    public void compile(ArrayList<ASTNode> code, HashMap<String, LSValue> locals) throws LSError { // runs global code
        var tmp = new ArrayList<Scope>();
        tmp.add(globalVariables);
        tmp.add(new Scope(locals));
        runCode(code, tmp, true);
    }

    public LSValue runCode(ArrayList<ASTNode> code, HashMap<String, LSValue> locals) throws LSError { // runs function
        var tmp = new ArrayList<Scope>();
        tmp.add(globalVariables);
        tmp.add(new Scope(locals));
        return runCode(code, tmp, false);
    }

    private LSValue runCode(String code, ArrayList<Scope> scopes,
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

        return runCode(tree, scopes, isGlobal);
    }

    private LSValue runCode(ArrayList<ASTNode> code, ArrayList<Scope> scopes,
            boolean isGlobal)
            throws LSError {
        for (var itm : code) {
            if (itm instanceof LSParser.Import) {
                var item = itm.castToType(LSParser.Import.class);

                var val = getImport(item.name);
                if (val == null)
                    throw new ImportNotFoundException();
                setValueFromScope(item.name, val, scopes);
                // if (isGlobal)
                // globals.put(item.name, val);
                // else
                // locals.put(item.name, val);
            }

            else if (itm instanceof LSParser.CreateFunction) {
                var item = itm.castToType(LSParser.CreateFunction.class);

                var val = new LFunction(item.code, item.args);
                setValueFromScope(item.name, val, scopes);
                // if (isGlobal)
                // globals.put(item.name, val);
                // else
                // locals.put(item.name, val);
            }

            else if (itm instanceof LSParser.setVariable) {
                var item = itm.castToType(LSParser.setVariable.class);
                LSValue val = null;

                val = LString.toValue(item.val);
                if (val == null)
                    val = LBoolean.toValue(item.val);

                if (val == null) {
                    val = LVariable.toValue(item.val);
                    if (val != null)
                        val = val.toType(LVariable.class).getValueFromVariable(scopes);
                }

                getLocalScope(scopes).put(item.name, val);
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

                var val = getValueFromScope(thing[0], scopes);
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
                                valuedArgs.get(i).toType(LVariable.class).getValueFromVariable(scopes));
                    }
                }

                val.toType(LFunction.class).call(valuedArgs, this);
            }

            else if (itm instanceof LSParser.IfBlock) {
                var item = itm.castToType(LSParser.IfBlock.class);

                // System.out.println(item);

                LSValue val = null;

                if (item.statement instanceof LVariable) {
                    val = item.statement.toType(LVariable.class).getValueFromVariable(scopes);
                }

                if (val == null)
                    throw new ValueIsNull(item.toString());

                // System.out.println(item.statement.getType().getString());
                if (val.getType().getString().equals("Boolean")) {
                    // System.out.println(item.statement);
                    if (val.getValue(Boolean.class)) {
                        scopes.add(new Scope());
                        this.runCode(item.code, scopes, false);
                        scopes.remove(scopes.size() - 1);
                    }
                }
            }

            else {
                throw new InvalidOperationException("not implemented: " + itm);
            }
        }

        globalVariables = scopes.get(0);

        // for (Scope scope : scopes) {
        // System.out.println(scope);
        // }

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

    public static LSValue getValueFromScope(String name, ArrayList<Scope> scopes) {
        var reversedList = new ArrayList<>(scopes);
        Collections.reverse(reversedList);
        for (Scope scope : reversedList) {
            if (scope.get(name) != null) {
                return scope.get(name);
            }
        }
        return null;
    }

    public static void setValueFromScope(String name, LSValue val, ArrayList<Scope> scopes) {
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

    public static Scope getLocalScope(ArrayList<Scope> scopes) {
        return scopes.get(scopes.size() - 1);
    }

    // LScript std Implementation
    protected LFunction print = new LFunction(arg -> {
        if (arg.get("val") instanceof LString)
            try {
                var out = arg.get("val").toType(LString.class).getString();
                if (out == null)
                    return new LNull();
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