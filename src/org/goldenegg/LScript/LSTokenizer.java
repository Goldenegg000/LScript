package org.goldenegg.LScript;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.goldenegg.LScript.LSErrors.*;
import org.goldenegg.LScript.LSTokenizer.Token.TokenEnum;

public class LSTokenizer {

    public static class Token {
        public enum TokenEnum {
            importStatement,
            functionStatement,
            ifStatement,
            elseStatement,
            name,
            setOperator,
            equalOperator,
            lessOperator,
            moreOperator,
            lessEqualOperator,
            moreEqualOperator,
            openingParen,
            closingParen,
            curlyBracketOpen,
            curlyBracketClose,
            codeBlockOpen,
            codeBlockClose,
            intToken,
            stringToken,
            moduleNameToken,
            separatorToken,
            getChildToken,
            endOfStatement,
            Nothing,
            EOF,
            booleanStatement;
        }

        private TokenEnum token;
        private String val;

        public int LineNumber = 0;
        public int CharNumber = 0;

        public Token() {
            this.token = null;
            val = null;
        }

        public Token(TokenEnum token, String value) {
            this.token = token;
            val = value;
        }

        public void setToken(TokenEnum token) {
            this.token = token;
        }

        public TokenEnum getToken() {
            return token;
        }

        public void setValue(String value) {
            val = value;
        }

        public String getValue() {
            return val;
        }

        @Override
        public String toString() {
            return getToken() + " -> " + getValue();
        }
    }

    public static ArrayList<Token> tokenize(String code) throws LSError {
        ArrayList<Token> tokens = new ArrayList<>();

        // Define regular expressions for different token patterns
        String commentPattern = "--.*";
        String importPattern = "\\s*import\\s*";
        String functionPattern = "\\s*function\\s*|\\s*func\\s*";
        String ifPattern = "\\s*if\\s*";
        String elsePattern = "\\s*else\\s*";
        String namePattern = "\\s*[a-zA-Z_][a-zA-Z_0-9\\.]*\\s*"; // Exclude reserved keywords
        String bracketsPattern = "[()]"; // Matches either an opening or closing parenthesis
        String curlyBracketOpenPattern = "\\{";
        String curlyBracketClosePattern = "}";
        String codeBlockOpenPattern = "\\s*then\\s*";
        String codeBlockClosePattern = "\\s*end\\s*";
        String intPattern = "\\s*\\d+\\s*"; // Matches one or more digits for intToken
        String stringPattern = "\".*?\""; // Matches double-quoted strings
        String separatorPattern = ","; // gets separator
        String endOfStatementPattern = ";"; // gets statement end
        String typePattern = "\\s*<[a-zA-Z_][a-zA-Z_0-9]*>\\s*"; // gets a type
        String operatorPattern = "\\s*==\\s*|\\s*<=\\s*|\\s*>=\\s*|\\s*<\\s*|\\s*>\\s*|\\s*=\\s*"; // Matches
                                                                                                   // the any
        String booleanPattern = "\\s*true\\s*|\\s*false\\s*";
        // operator

        // Combine all patterns into a single regex
        String combinedPattern = String.join("|", importPattern, functionPattern, ifPattern, elsePattern,
                curlyBracketOpenPattern, curlyBracketClosePattern, codeBlockOpenPattern,
                codeBlockClosePattern, intPattern, stringPattern, typePattern, separatorPattern,
                endOfStatementPattern, bracketsPattern, operatorPattern, booleanPattern, namePattern, "\s+", "\t+",
                "\n+", "."); // which
        // is
        // BIG

        // Create the regex pattern and get a matcher
        Pattern pattern = Pattern.compile(combinedPattern);
        Matcher matcher = pattern.matcher(code.replaceAll(commentPattern, "")); // get rid of comments

        // Find and add the tokens to the list
        while (matcher.find()) {
            String matchedToken = matcher.group().trim();
            TokenEnum token = null;
            switch (matchedToken) {
                case "\s":
                case "\t":
                case "\n":
                    continue;
                case "func":
                case "function":
                    token = TokenEnum.functionStatement;
                    break;
                case "import":
                    token = TokenEnum.importStatement;
                    break;
                case "if":
                    token = TokenEnum.ifStatement;
                    break;
                case "true":
                    token = TokenEnum.booleanStatement;
                    break;
                case "false":
                    token = TokenEnum.booleanStatement;
                    break;
                case "else":
                    token = TokenEnum.elseStatement;
                    break;
                case "then":
                    token = TokenEnum.codeBlockOpen;
                    break;
                case "end":
                    token = TokenEnum.codeBlockClose;
                    break;
                case "{":
                    token = TokenEnum.curlyBracketOpen;
                    break;
                case "}":
                    token = TokenEnum.curlyBracketClose;
                    break;
                case "(":
                    token = TokenEnum.openingParen;
                    break;
                case ")":
                    token = TokenEnum.closingParen;
                    break;
                case "==":
                    token = TokenEnum.equalOperator;
                    break;
                case "<":
                    token = TokenEnum.lessOperator;
                    break;
                case ">":
                    token = TokenEnum.moreOperator;
                    break;
                case "<=":
                    token = TokenEnum.lessEqualOperator;
                    break;
                case ">=":
                    token = TokenEnum.moreEqualOperator;
                    break;
                case "=":
                    token = TokenEnum.setOperator;
                    break;
                case ",":
                    token = TokenEnum.separatorToken;
                    break;
                case ";":
                    token = TokenEnum.endOfStatement;
                    break;
                default:
                    // Check if the matched token is an integer
                    if (matchedToken.matches("")) {
                        token = TokenEnum.Nothing;
                        break;
                    }
                    if (matchedToken.matches("\\s*\\d+\\s*")) {
                        token = TokenEnum.intToken;
                        break;
                    }
                    if (matchedToken.matches("\".*?\"")) { // Check if the matched token is a string
                        token = TokenEnum.stringToken;
                        break;
                    }
                    if (matchedToken.matches("<[a-zA-Z_][a-zA-Z_0-9]*>")) { // Check if the matched token is a
                                                                            // type
                        token = TokenEnum.moduleNameToken;
                        break;
                    }
                    if (matchedToken.matches("[a-zA-Z_][a-zA-Z_0-9\\.]*")) { // Check if the matched token is valid
                                                                             // name
                        token = TokenEnum.name;
                    }
            }
            if (token == null) // if no token is matched throw an error
                throw new InvalidTokenException(
                        "token: " + matchedToken + " at line " + getLine(code, matcher.start()) + " is invalid");
            if (token == TokenEnum.Nothing)
                continue;
            var TheToken = new Token();
            TheToken.setToken(token); // Store the matched token type inside the token
            TheToken.setValue(matchedToken); // Store the matched value inside the token

            TheToken.LineNumber = getLine(code, matcher.start());

            tokens.add(TheToken);
        }

        tokens.add(new Token(TokenEnum.EOF, ""));

        return tokens;
    }

    static int getLine(String data, int start) {
        int line = 1;
        Pattern pattern = Pattern.compile("\n");
        Matcher matcher = pattern.matcher(data);
        matcher.region(0, start);
        while (matcher.find()) {
            line++;
        }
        return (line);
    }
}
