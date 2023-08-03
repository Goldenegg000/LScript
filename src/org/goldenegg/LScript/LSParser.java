package org.goldenegg.LScript;

import java.util.ArrayList;

import org.goldenegg.LScript.LSErrors.*;
import org.goldenegg.LScript.LSTokenizer.Token;
import org.goldenegg.LScript.LSTokenizer.Token.TokenEnum;
import org.goldenegg.LScript.Types.LString;
import org.goldenegg.LScript.Types.LVariable;

public class LSParser {

    public static class ASTNode {
        public <T> T castToType(Class<T> type) {
            return type.cast(this);
        }
    }

    public static ArrayList<ASTNode> parse(ArrayList<Token> tokens) throws LSError {
        var tree = new ArrayList<ASTNode>();
        for (int i = 0; i < tokens.size(); i++) {
            var node = tokens.get(i);
            if (node.getToken() == TokenEnum.importStatement) {
                if (tokens.get(i + 1).getToken() == Token.TokenEnum.moduleNameToken)
                    if (tokens.get(i + 2).getToken() == Token.TokenEnum.endOfStatement) {
                        var name = tokens.get(i + 1).getValue();
                        name = name.substring(1, name.length() - 1);
                        tree.add(new Import(name));
                        i++;
                    }
            }

            else if (node.getToken() == TokenEnum.name && tokens.get(i + 1).getToken() == TokenEnum.openingParen) {
                var cod = new ArrayList<LSValue>();
                var name = node.getValue();
                i += 2;
                i = getExpression(tokens, i, cod);
                tree.add(new CallFunction(name, cod));
            }

            else if (node.getToken() == TokenEnum.ifStatement
                    && tokens.get(i + 1).getToken() == TokenEnum.openingParen) {
                var cod = new ArrayList<LSValue>();
                i += 2;
                i = getExpression(tokens, i, cod);

                i++;
                if (tokens.get(i).getToken() != TokenEnum.codeBlockOpen)
                    throw new InvalidTokenException("missing 'then' : " + i);

                i++;
                var code = new ArrayList<Token>();
                var ignore = 0;
                while (tokens.get(i).getToken() != TokenEnum.codeBlockClose || ignore != 0) {
                    code.add(tokens.get(i));
                    if (tokens.get(i).getToken() == TokenEnum.codeBlockOpen)
                        ignore++;
                    if (tokens.get(i).getToken() == TokenEnum.codeBlockClose)
                        ignore--;
                    i++;
                }

                // i++;
                System.out.println(tokens.get(i));
                tree.add(new IfBlock(cod.get(0), parse(code)));
                continue;
            }

            else if (node.getToken() == TokenEnum.name && tokens.get(i + 1).getToken() == TokenEnum.setOperator) {
                tree.add(new setVariable(node.getValue(), tokens.get(i + 2)));
                i += 2;
            }

            else if (node.getToken() == TokenEnum.functionStatement) {
                if (tokens.get(i + 1).getToken() == TokenEnum.name)
                    if (tokens.get(i + 2).getToken() == TokenEnum.openingParen) {
                        var funcname = tokens.get(i + 1).getValue();
                        var args = new ArrayList<String>();
                        i += 3;
                        while (tokens.get(i).getToken() != TokenEnum.closingParen) {
                            var name = tokens.get(i);
                            if (name.getToken() == TokenEnum.name)
                                if (tokens.get(i + 1).getToken() == TokenEnum.separatorToken) {
                                    // System.out.println("s " + name.toString());
                                    args.add(name.getValue());
                                    i += 2;
                                } else if (tokens.get(i + 1).getToken() == TokenEnum.closingParen) {
                                    // System.out.println("e " + name.toString());
                                    args.add(name.getValue());
                                    i += 1;
                                    break;
                                } else {
                                    throw new InvalidTokenException(
                                            name.toString() + " : " + i);
                                }
                        }

                        i++;
                        if (tokens.get(i).getToken() != TokenEnum.codeBlockOpen)
                            throw new InvalidTokenException("missing 'then' : " + i);

                        i++;
                        var cod = new ArrayList<Token>();
                        var ignore = 0;
                        while (tokens.get(i).getToken() != TokenEnum.codeBlockClose || ignore != 0) {
                            cod.add(tokens.get(i));
                            if (tokens.get(i).getToken() == TokenEnum.codeBlockOpen)
                                ignore++;
                            if (tokens.get(i).getToken() == TokenEnum.codeBlockClose)
                                ignore--;
                            i++;
                        }

                        tree.add(new CreateFunction(funcname, args, parse(cod)));
                        continue;
                    }
            }

            else if (node.getToken() == TokenEnum.EOF)
                break;

            else {
                throw new InvalidTokenException(node.toString() + " : " + i);
            }

            if (tokens.size() <= i + 1)
                throw new InvalidEndOfStatementException(node.toString() + " : " + i + " # missing endOfStatement");
            if (tokens.get(i + 1).getToken() != TokenEnum.endOfStatement)
                throw new InvalidEndOfStatementException(node.toString() + " : " + i);
            else
                i++;
        }
        return tree;
    }

    private static int getExpression(ArrayList<Token> tokens, int i, ArrayList<LSValue> cod)
            throws LSError, InvalidOperationException {
        var ignore = 0;
        while (tokens.get(i).getToken() != TokenEnum.closingParen || ignore != 0) {
            var token = tokens.get(i);
            LSValue currentVal = null;
            // if (token.getToken() == TokenEnum.stringToken) {
            // cod.add(new LString(token.getValue().substring(1, token.getValue().length() -
            // 1)));
            // }
            currentVal = LString.toValue(token);

            if (token.getToken() == TokenEnum.name) {
                currentVal = new LVariable(token.getValue());
            }

            if (currentVal != null)
                cod.add(currentVal);
            else
                throw new InvalidOperationException("could not get current value type");

            if (tokens.get(i).getToken() == TokenEnum.openingParen)
                ignore++;
            if (tokens.get(i).getToken() == TokenEnum.closingParen)
                ignore--;
            i++;
        }
        return i;
    }

    public static class Import extends ASTNode {
        public String name;

        public Import(String name) {
            this.name = name;
        }

        public String toString() {
            return "import: " + name;
        }
    }

    public static class setVariable extends ASTNode {
        public String name;
        public Token val;

        public setVariable(String name, Token val) {
            this.name = name;
            this.val = val;
        }

        public String toString() {
            return "setVar: " + name;
        }
    }

    public static class CreateFunction extends ASTNode {
        public String name;
        public ArrayList<String> args;
        public ArrayList<ASTNode> code;

        public CreateFunction(String name, ArrayList<String> args, ArrayList<ASTNode> code) {
            this.name = name;
            this.args = args;
            this.code = code;
        }

        public String toString() {
            return "createFunction: name::" + name + ", args::" + args + ", code::" + code;
        }
    }

    public static class CallFunction extends ASTNode {
        public String name;
        public ArrayList<LSValue> args;

        public CallFunction(String name, ArrayList<LSValue> args) {
            this.name = name;
            this.args = args;
        }

        public String toString() {
            return "callFunction: name::" + name + ", args::" + args;
        }
    }

    public static class IfBlock extends ASTNode {
        public LSValue statement;
        public ArrayList<ASTNode> code;

        public IfBlock(LSValue condition, ArrayList<ASTNode> code) {
            this.statement = condition;
            this.code = code;
        }

        public String toString() {
            return "IfBlock: statement::" + statement + ", code::" + code;
        }
    }
}
