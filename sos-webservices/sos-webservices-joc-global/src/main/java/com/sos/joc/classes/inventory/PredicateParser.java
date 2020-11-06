package com.sos.joc.classes.inventory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PredicateParser {

    public static void parse(InputStream stream) throws IOException, IllegalArgumentException {
        Reader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        try {
            StreamTokenizer tokenizer = new StreamTokenizer(reader);
            parse(tokenizer);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                }
            }
        }
    }

    public static void parse(byte[] bytes) throws IOException, IllegalArgumentException {
        Reader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes), StandardCharsets.UTF_8));
        try {
            StreamTokenizer tokenizer = new StreamTokenizer(reader);
            parse(tokenizer);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                }
            }
        }
    }

    public static void parse(String str) throws IOException, IllegalArgumentException {
        parse(new StreamTokenizer(new StringReader(str)));
    }

    private static void parse(StreamTokenizer tokenizer) throws IOException, IllegalArgumentException {
        tokenizer.resetSyntax();
        tokenizer.quoteChar('"');
        tokenizer.quoteChar('\'');
        tokenizer.wordChars(38, 38); // &
        tokenizer.wordChars(124, 124); // |
        tokenizer.slashSlashComments(false);
        tokenizer.slashStarComments(false);

        int countRoundBrackets = 0;
        String token = "";
        int position = 0;
        int lastPosition = 0;
        boolean variableBracket = false;

        int currentToken = tokenizer.nextToken();
        while (currentToken != StreamTokenizer.TT_EOF) {
            if (tokenizer.ttype >= 0 && tokenizer.ttype <= 32) {
                // whitespaces
                if (!variableBracket) {
                    token += " ";
                }
                position++;
            } else if (tokenizer.ttype >= 127 && tokenizer.ttype <= 159) {
                // controls
            } else {
                switch (tokenizer.ttype) {
                case StreamTokenizer.TT_WORD:
                    // all chars > 255 have always ttype == TT_WORD
                    int n = (int) tokenizer.sval.charAt(0);
                    if (n != 38 && n != 124) {
                        token += tokenizer.sval;
                    } else if (tokenizer.sval.length() != 2 || tokenizer.sval.charAt(0) != tokenizer.sval.charAt(1)) {
                        throw new IllegalArgumentException("wrong syntax at position " + position + ": '" + tokenizer.sval
                                + "' found. Allowed boolean operations are '&&' and '||'");
                    } else {
                        parseToken(token.trim().replaceFirst("^[!\\s]*", ""), lastPosition);
                        token = "";
                    }
                    position += tokenizer.sval.length();
                    lastPosition = position;
                    // System.out.println("##"+tokenizer.sval);
                    break;
                case '<':
                    token += ":\t:\t:<:\t:\t:";
                    position++;
                    break;
                case '>':
                    token += ":\t:\t:>:\t:\t:";
                    position++;
                    break;
                case '=':
                    if (token.endsWith("!")) {
                        token = token.substring(0, token.length() - 1) + ":\t:\t:!=:\t:\t:";
                    } else if (token.endsWith("=")) {
                        token = token.substring(0, token.length() - 1) + ":\t:\t:==:\t:\t:";
                    } else if (token.endsWith(">:\t:\t:") || token.endsWith("<:\t:\t:")) {
                        token = token.substring(0, token.length() - 5) + "=:\t:\t:";
                    } else {
                        token += "=";
                    }
                    position++;
                    break;
                case 's':
                    if (token.endsWith(" matche")) {
                        token = token.substring(0, token.length() - 6) + ":\t:\t:matches:\t:\t:";
                    } else {
                        token += "s";
                    }
                    position++;
                    break;
                case 'n':
                    if (token.endsWith(" i")) {
                        token = token.substring(0, token.length() - 1) + ":\t:\t:in:\t:\t:";
                    } else {
                        token += "n";
                    }
                    position++;
                    break;
                case '(':
                    countRoundBrackets++;
                    if (token.trim().endsWith("variable") || token.trim().endsWith("argument")) {
                        variableBracket = true;
                        token = token.replaceFirst("\\s*$", "") + "(";
                    }
                    position++;
                    break;
                case ')':
                    countRoundBrackets--;
                    if (variableBracket) {
                        variableBracket = false;
                        token += ")";
                    }
                    position++;
                    break;
                case '\'':
                case '"':
                    String quote = Character.toString((char) tokenizer.ttype);
                    token += quote + tokenizer.sval + quote;
                    position += tokenizer.sval.length() + 2;
                    // System.out.println(quote + tokenizer.sval + quote);
                    break;
                default:
                    token += Character.toString((char) tokenizer.ttype);
                    position++;
                    // System.out.println((char) tokenizer.ttype);
                    break;
                }
            }

            currentToken = tokenizer.nextToken();
            if (currentToken == StreamTokenizer.TT_EOF) {
                parseToken(token.trim().replaceFirst("^[!\\s]*", ""), lastPosition);
                token = "";
                if (countRoundBrackets > 0) {
                    throw new IllegalArgumentException("wrong syntax: missing closing round bracket ')' or a wrong quotation character is used");
                } else if (countRoundBrackets < 0) {
                    throw new IllegalArgumentException("wrong syntax: missing opening round bracket '('");
                }
            }
        }
    }

    private static String withoutQuotedParts(String str) throws IOException, IllegalArgumentException {
        StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(str));
        tokenizer.resetSyntax();
        tokenizer.quoteChar('"');
        tokenizer.quoteChar('\'');
        tokenizer.slashSlashComments(false);
        tokenizer.slashStarComments(false);
        tokenizer.wordChars(32, 33);
        tokenizer.wordChars(35, 38);
        tokenizer.wordChars(40, 126);
        tokenizer.wordChars(160, 255); // 2. part latin1

        String token = "";

        int currentToken = tokenizer.nextToken();
        while (currentToken != StreamTokenizer.TT_EOF) {
            switch (tokenizer.ttype) {
            case StreamTokenizer.TT_WORD:
                token += tokenizer.sval;
                break;
            case '\'':
            case '"':
                // for (int i = 0; i < tokenizer.sval.length() + 2; i++) {
                // token += " ";
                // }
                break;
            default:
                token += Character.toString((char) tokenizer.ttype);
                break;
            }

            currentToken = tokenizer.nextToken();
        }

        return token;
    }
    
    protected static int parseVariable(String str, int pos) throws IOException, IllegalArgumentException {
        return parseVariable(str, pos, null);
    }

    private static int parseVariable(String str, int pos, String parentStr) throws IOException, IllegalArgumentException {
        StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(str));
        tokenizer.resetSyntax();
        tokenizer.quoteChar('"');
        tokenizer.quoteChar('\'');
        tokenizer.slashSlashComments(false);
        tokenizer.slashStarComments(false);
        // word char 0-9a-zA-Z_
        // tokenizer.wordChars(33, 33); // !
        // tokenizer.wordChars(34, 34); // "
        // tokenizer.wordChars(35, 35); // #
        // tokenizer.wordChars(36, 36); // $
        // tokenizer.wordChars(37, 38); // % &
        // tokenizer.wordChars(39, 41); // ' ( )
        // tokenizer.wordChars(42, 43); // * +
        // tokenizer.wordChars(44, 44); // ,
        // tokenizer.wordChars(45, 45); // -
        // tokenizer.wordChars(46, 46); // .
        tokenizer.wordChars(48, 57); // 0-9
        // tokenizer.wordChars(60, 60); // <
        // tokenizer.wordChars(61, 61); // =
        // tokenizer.wordChars(62, 62); // >
        tokenizer.wordChars(65, 90); // A-Z
        tokenizer.wordChars(95, 95); // _
        tokenizer.wordChars(97, 122); // a-z
        // tokenizer.wordChars(123, 123); // {
        // tokenizer.wordChars(124, 124); // |
        // tokenizer.wordChars(125, 125); // }
        // tokenizer.wordChars(126, 126); // ~
        tokenizer.wordChars(160, 255); // 2. part latin1

        boolean isArgument = false;
        int position = pos;
        List<String> varKeywords = new ArrayList<>(Arrays.asList("key", "label", "default"));
        List<String> argKeywords = new ArrayList<>(Arrays.asList("key", "default"));
        
        if (parentStr == null || parentStr.isEmpty()) {
            parentStr = str; 
        }

        int currentToken = tokenizer.nextToken();
        if (currentToken == StreamTokenizer.TT_EOF) {
            throw new IllegalArgumentException("unexpected empty expression at position " + position);
        }
        if (tokenizer.ttype == StreamTokenizer.TT_WORD) {
            if (tokenizer.sval.equals("variable") || tokenizer.sval.equals("argument")) {
                isArgument = tokenizer.sval.equals("argument");
                position += tokenizer.sval.length();
            } else {
                throwUnexpectedCharErrMsg(tokenizer, parentStr, position);
            }
        } else {
            throwUnexpectedCharErrMsg(tokenizer, parentStr, position);
        }

        currentToken = tokenizer.nextToken();
        throwUnexpectedEndErrMsg(currentToken, parentStr, position);

        if (tokenizer.ttype == '(') {
            position++;
        } else {
            throwUnexpectedCharErrMsg(tokenizer, parentStr, position);
        }

        currentToken = tokenizer.nextToken();
        throwUnexpectedEndErrMsg(currentToken, parentStr, position);

        if (tokenizer.ttype == '"' || tokenizer.ttype == '\'') {
            // variable name without key=
            position += tokenizer.sval.length() + 2;
        } else if (tokenizer.ttype == StreamTokenizer.TT_WORD) {
            // key=, label=, default=
            String keyword = "";
            if (!isArgument && varKeywords.contains(tokenizer.sval)) {
                keyword = tokenizer.sval;
                varKeywords.remove(keyword);
                position += tokenizer.sval.length();
            } else if (isArgument && argKeywords.contains(tokenizer.sval)) {
                keyword = tokenizer.sval;
                argKeywords.remove(keyword);
                position += tokenizer.sval.length();
            } else {
                throwUnexpectedCharErrMsg(tokenizer, parentStr, position);
            }

            currentToken = tokenizer.nextToken();
            throwUnexpectedEndErrMsg(currentToken, parentStr, position);

            if (tokenizer.ttype == '=') {
                position++;

                currentToken = tokenizer.nextToken();
                throwUnexpectedEndErrMsg(currentToken, parentStr, position);

                if (!isArgument && "label".equals(keyword)) {
                    if (tokenizer.ttype == StreamTokenizer.TT_WORD) {
                        position += tokenizer.sval.length();
                    } else {
                        throwUnexpectedCharErrMsg(tokenizer, parentStr, position);
                    }
                } else if (isArgument || (!isArgument && !"label".equals(keyword))) {
                    if (tokenizer.ttype == '"' || tokenizer.ttype == '\'') {
                        position += tokenizer.sval.length() + 2;
                    } else {
                        throwUnexpectedCharErrMsg(tokenizer, parentStr, position);
                    }
                } else {
                    throwUnexpectedCharErrMsg(tokenizer, parentStr, position);
                }
            } else {
                throwUnexpectedCharErrMsg(tokenizer, parentStr, position);
            }

        } else {
            throwUnexpectedCharErrMsg(tokenizer, parentStr, position);
        }

        currentToken = tokenizer.nextToken();
        throwUnexpectedEndErrMsg(currentToken, parentStr, position);

        if (tokenizer.ttype == ')') {
            position++;
        } else if (tokenizer.ttype == ',') {

            currentToken = tokenizer.nextToken();
            throwUnexpectedEndErrMsg(currentToken, parentStr, position);

            if (tokenizer.ttype == StreamTokenizer.TT_WORD) {
                // key=, label=, default=
                String keyword = "";
                if (!isArgument && varKeywords.contains(tokenizer.sval)) {
                    keyword = tokenizer.sval;
                    varKeywords.remove(keyword);
                    position += tokenizer.sval.length();
                } else if (isArgument && argKeywords.contains(tokenizer.sval)) {
                    keyword = tokenizer.sval;
                    argKeywords.remove(keyword);
                    position += tokenizer.sval.length();
                } else {
                    throwUnexpectedCharErrMsg(tokenizer, parentStr, position);
                }

                currentToken = tokenizer.nextToken();
                throwUnexpectedEndErrMsg(currentToken, parentStr, position);

                if (tokenizer.ttype == '=') {
                    position++;

                    currentToken = tokenizer.nextToken();
                    throwUnexpectedEndErrMsg(currentToken, parentStr, position);

                    if (!isArgument && "label".equals(keyword)) {
                        if (tokenizer.ttype == StreamTokenizer.TT_WORD) {
                            position += tokenizer.sval.length();
                        } else {
                            throwUnexpectedCharErrMsg(tokenizer, parentStr, position);
                        }
                    } else if (isArgument || (!isArgument && !"label".equals(keyword))) {
                        if (tokenizer.ttype == '"' || tokenizer.ttype == '\'') {
                            position += tokenizer.sval.length() + 2;
                        } else {
                            throwUnexpectedCharErrMsg(tokenizer, parentStr, position);
                        }
                    } else {
                        throwUnexpectedCharErrMsg(tokenizer, parentStr, position);
                    }
                } else {
                    throwUnexpectedCharErrMsg(tokenizer, parentStr, position);
                }
            }

            currentToken = tokenizer.nextToken();
            throwUnexpectedEndErrMsg(currentToken, parentStr, position);

            if (isArgument) {
                if (tokenizer.ttype == ')') {
                    position++;
                } else {
                    throwUnexpectedCharErrMsg(tokenizer, parentStr, position);
                }
            } else {
                if (tokenizer.ttype == ')') {
                    position++;
                } else if (tokenizer.ttype == ',') {

                    currentToken = tokenizer.nextToken();
                    throwUnexpectedEndErrMsg(currentToken, parentStr, position);

                    if (tokenizer.ttype == StreamTokenizer.TT_WORD) {
                        // key=, label=, default=
                        String keyword = "";
                        if (varKeywords.contains(tokenizer.sval)) {
                            keyword = tokenizer.sval;
                            varKeywords.remove(keyword);
                            position += tokenizer.sval.length();
                        } else {
                            throwUnexpectedCharErrMsg(tokenizer, parentStr, position);
                        }

                        currentToken = tokenizer.nextToken();
                        throwUnexpectedEndErrMsg(currentToken, parentStr, position);

                        if (tokenizer.ttype == '=') {
                            position++;

                            currentToken = tokenizer.nextToken();
                            throwUnexpectedEndErrMsg(currentToken, parentStr, position);

                            if ("label".equals(keyword)) {
                                if (tokenizer.ttype == StreamTokenizer.TT_WORD) {
                                    position += tokenizer.sval.length();
                                } else {
                                    throwUnexpectedCharErrMsg(tokenizer, parentStr, position);
                                }
                            } else if (!"label".equals(keyword)) {
                                if (tokenizer.ttype == '"' || tokenizer.ttype == '\'') {
                                    position += tokenizer.sval.length() + 2;
                                } else {
                                    throwUnexpectedCharErrMsg(tokenizer, parentStr, position);
                                }
                            } else {
                                throwUnexpectedCharErrMsg(tokenizer, parentStr, position);
                            }
                        } else {
                            throwUnexpectedCharErrMsg(tokenizer, parentStr, position);
                        }
                    }

                    currentToken = tokenizer.nextToken();
                    throwUnexpectedEndErrMsg(currentToken, parentStr, position);

                    if (tokenizer.ttype == ')') {
                        position++;
                    } else {
                        throwUnexpectedCharErrMsg(tokenizer, parentStr, position);
                    }

                } else {
                    throwUnexpectedCharErrMsg(tokenizer, parentStr, position);
                }
            }

        } else {
            throwUnexpectedCharErrMsg(tokenizer, parentStr, position);
        }

        currentToken = tokenizer.nextToken();
        if (currentToken == StreamTokenizer.TT_EOF) {
            // $xxx or ${xxx} without .toNumber or .toBoolean
            return position;
        } else if (tokenizer.ttype == '.') {
            position++;
        } else {
            throwUnexpectedCharErrMsg(tokenizer, parentStr, position);
        }

        return considerToNumberBoolean(tokenizer, str, position, parentStr);
    }
    
    protected static int parseDollarVariable(String str, int pos) throws IOException, IllegalArgumentException {
        return parseDollarVariable(str, pos, null);
    }

    private static int parseDollarVariable(String str, int pos, String parentStr) throws IOException, IllegalArgumentException {
        StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(str));
        tokenizer.resetSyntax();
        tokenizer.quoteChar('"');
        tokenizer.quoteChar('\'');
        tokenizer.slashSlashComments(false);
        tokenizer.slashStarComments(false);
        // word char 0-9a-zA-Z_
        // tokenizer.wordChars(33, 33); // !
        // tokenizer.wordChars(34, 34); // "
        // tokenizer.wordChars(35, 35); // #
        // tokenizer.wordChars(36, 36); // $
        // tokenizer.wordChars(37, 38); // % &
        // tokenizer.wordChars(39, 41); // ' ( )
        // tokenizer.wordChars(42, 43); // * +
        // tokenizer.wordChars(44, 44); // ,
        // tokenizer.wordChars(45, 45); // -
        // tokenizer.wordChars(46, 46); // .
        tokenizer.wordChars(48, 57); // 0-9
        // tokenizer.wordChars(60, 60); // <
        // tokenizer.wordChars(61, 61); // =
        // tokenizer.wordChars(62, 62); // >
        tokenizer.wordChars(65, 90); // A-Z
        tokenizer.wordChars(95, 95); // _
        tokenizer.wordChars(97, 122); // a-z
        // tokenizer.wordChars(123, 123); // {
        // tokenizer.wordChars(124, 124); // |
        // tokenizer.wordChars(125, 125); // }
        // tokenizer.wordChars(126, 126); // ~
        tokenizer.wordChars(160, 255); // 2. part latin1

        int position = pos;
        if (parentStr == null || parentStr.isEmpty()) {
            parentStr = str; 
        }

        int currentToken = tokenizer.nextToken();
        if (currentToken == StreamTokenizer.TT_EOF) {
            throw new IllegalArgumentException("unexpected empty expression at position " + position);
        }
        if (tokenizer.ttype == '$') {
            position++;
        } else {
            throwUnexpectedCharErrMsg(tokenizer, parentStr, position);
        }
        currentToken = tokenizer.nextToken();
        throwUnexpectedEndErrMsg(currentToken, parentStr, position);
        if (tokenizer.ttype == '{') {
            position++;
            currentToken = tokenizer.nextToken();
            throwUnexpectedEndErrMsg(currentToken, parentStr, position);
            if (tokenizer.ttype == StreamTokenizer.TT_WORD) {
                position += tokenizer.sval.length();
                currentToken = tokenizer.nextToken();
                throwUnexpectedEndErrMsg(currentToken, parentStr, position);
                if (tokenizer.ttype == '}') {
                    position++;
                    // consider .toNumber, toBoolean
                } else {
                    throwUnexpectedCharErrMsg(tokenizer, parentStr, position);
                }
            } else {
                throwUnexpectedCharErrMsg(tokenizer, parentStr, position);
            }

        } else if (tokenizer.ttype == StreamTokenizer.TT_WORD) {
            position += tokenizer.sval.length();
            // consider .toNumber, toBoolean
        } else {
            throwUnexpectedCharErrMsg(tokenizer, parentStr, position);
        }

        currentToken = tokenizer.nextToken();
        if (currentToken == StreamTokenizer.TT_EOF) {
            // $xxx or ${xxx} without .toNumber or .toBoolean
            return position;
        } else if (tokenizer.ttype == '.') {
            position++;
        } else {
            throwUnexpectedCharErrMsg(tokenizer, parentStr, position);
        }

        return considerToNumberBoolean(tokenizer, str, position, parentStr);
    }

    private static int considerToNumberBoolean(StreamTokenizer tokenizer, String str, int pos, String parentStr) throws IOException {
        int position = pos;
        int currentToken = tokenizer.nextToken();
        throwUnexpectedEndErrMsg(currentToken, parentStr, position);

        if (tokenizer.ttype == StreamTokenizer.TT_WORD) {
            if (tokenizer.sval.equals("toNumber") || tokenizer.sval.equals("toBoolean")) {
                position += tokenizer.sval.length();
            } else {
                throwUnexpectedCharErrMsg(tokenizer, parentStr, position);
            }
        } else {
            throwUnexpectedCharErrMsg(tokenizer, parentStr, position);
        }

        currentToken = tokenizer.nextToken();
        if (currentToken == StreamTokenizer.TT_EOF) {
            // $xxx or ${xxx} with .toNumber or .toBoolean
            return position;
        } else {
            throwUnexpectedCharErrMsg(tokenizer, parentStr, position);
        }
        return position;
    }

    private static void throwUnexpectedCharErrMsg(StreamTokenizer tokenizer, String str, int position) {
        if (tokenizer.ttype == StreamTokenizer.TT_WORD) {
            throw new IllegalArgumentException("wrong syntax near '" + str + "': unexpected characters [" + tokenizer.sval + "] at position "
                    + position);
        } else {
            throw new IllegalArgumentException("wrong syntax near '" + str + "': unexpected character [" + Character.toString((char) tokenizer.ttype)
                    + "] at position " + position);
        }
    }

    private static void throwUnexpectedEndErrMsg(int currentToken, String str, int position) {
        if (currentToken == StreamTokenizer.TT_EOF) {
            throw new IllegalArgumentException("wrong syntax near '" + str + "': unexpected end of the expression at position " + position);
        }
    }

    private static void parseToken(String str, int position) throws IllegalArgumentException, IOException {
        String separator = ":\t:\t:";
        String replaceSeparatorExpr = "(" + separator + ")*";
        String[] tokens = (str + separator + separator).split(separator, 3);
        tokens[0] = tokens[0].replaceAll(replaceSeparatorExpr, "").trim();
        tokens[1] = tokens[1].replaceAll(replaceSeparatorExpr, "").trim();
        tokens[2] = tokens[2].replaceAll(replaceSeparatorExpr, "").trim();
        str = tokens[0];
        if (!tokens[1].isEmpty()) {
            str += " " + tokens[1];
        }
        int startPos2 = str.length();
        if (!tokens[2].isEmpty()) {
            str += " " + tokens[2];
            startPos2 += 1;
        }
        String errMessage = "wrong syntax near '" + str + "'";
        String extendedErrMessage = errMessage + (tokens[1].isEmpty() ? "" : " using '" + tokens[1] + "' operator: ");

        switch (tokens[1]) {
        case "in":
            // check left is numeric and right is array
            if (tokens[0].isEmpty() || tokens[2].isEmpty()) {
                throw new IllegalArgumentException(extendedErrMessage + "value to compare is missing.");
            }
            if (!tokens[2].replaceAll("\\s", "").matches("\\[(-?[0-9]+(.[0-9]+)?)(,-?[0-9]+(.[0-9]+)?)*\\]")) {
                throw new IllegalArgumentException(extendedErrMessage + "[" + tokens[2] + "] is not an array of numbers.");
            }
            if (isNumeric(tokens[0])) {
                if (tokens[0].endsWith(".toNumber")) {
                    checkVariableSyntax(tokens[0], 0, str);
                }
            } else {
                throw new IllegalArgumentException(extendedErrMessage + "[" + tokens[0]
                        + "] is not numeric. Expects variable(...).toNumber, argument(...).toNumber, returnCode or an unquoted number.");
            }
            break;
        case "matches":
            // check string left and right
            if (tokens[0].isEmpty() || tokens[2].isEmpty()) {
                throw new IllegalArgumentException(extendedErrMessage + "value to compare is missing.");
            }
            if (!isString(tokens[2])) {
                throw new IllegalArgumentException(extendedErrMessage + "[" + tokens[2]
                        + "] is not a string. Expects a ' or \"  enclosed expression.");
            } else if (isString(tokens[0]) || isStringVariable(tokens[0])) {
                if (isStringVariable(tokens[0])) {
                    checkVariableSyntax(tokens[0], 0, str);
                }
            } else {
                throw new IllegalArgumentException(extendedErrMessage + "[" + tokens[0]
                        + "] is not a string. Expects variable(...), argument(...) or a ' or \"  enclosed expression.");
            }
            break;
        case "==":
        case "!=":
            // check datatype left and right has to be the same
            if (tokens[0].isEmpty() || tokens[2].isEmpty()) {
                throw new IllegalArgumentException(extendedErrMessage + "value to compare is missing.");
            }
            if (isNumeric(tokens[0]) && isNumeric(tokens[2])) {
                if (tokens[0].endsWith(".toNumber")) {
                    checkVariableSyntax(tokens[0], 0, str);
                }
                if (tokens[2].endsWith(".toNumber")) {
                    checkVariableSyntax(tokens[2], startPos2, str);
                }
            } else if ((isString(tokens[0]) || isStringVariable(tokens[0])) && (isString(tokens[2]) || isStringVariable(tokens[2]))) {
                if (isStringVariable(tokens[0])) {
                    checkVariableSyntax(tokens[0], 0, str);
                }
                if (isStringVariable(tokens[2])) {
                    checkVariableSyntax(tokens[2], startPos2, str);
                }
            } else if (isBoolean(tokens[0]) && isBoolean(tokens[2])) {
                if (tokens[0].endsWith(".toBoolean")) {
                    checkVariableSyntax(tokens[0], 0, str);
                }
                if (tokens[2].endsWith(".toBoolean")) {
                    checkVariableSyntax(tokens[2], startPos2, str);
                }
            } else {
                throw new IllegalArgumentException(extendedErrMessage + "[" + tokens[0] + "] and [" + tokens[2] + "] don't have the same data type.");
            }
            break;
        case "<":
        case "<=":
        case ">":
        case ">=":
            // check numeric left and right
            if (tokens[0].isEmpty() || tokens[2].isEmpty()) {
                throw new IllegalArgumentException(extendedErrMessage + "value to compare is missing.");
            }
            if (isNumeric(tokens[0])) {
                if (tokens[0].endsWith(".toNumber")) {
                    checkVariableSyntax(tokens[0], 0, str);
                }
            } else {
                throw new IllegalArgumentException(extendedErrMessage + "[" + tokens[0]
                        + "] is not numeric. Expects variable(...).toNumber, argument(...).toNumber, returnCode or an unquoted number.");
            }
            if (isNumeric(tokens[2])) {
                if (tokens[2].endsWith(".toNumber")) {
                    checkVariableSyntax(tokens[2], startPos2, str);
                }
            } else {
                throw new IllegalArgumentException(extendedErrMessage + "[" + tokens[2]
                        + "] is not numeric. Expects variable(...).toNumber, argument(...).toNumber, returnCode or an unquoted number.");
            }
            break;
        case "":
            // check toBoolean
            if (isBoolean(tokens[0])) {
                if (tokens[0].endsWith(".toBoolean")) {
                    checkVariableSyntax(tokens[0], 0, str);
                }
            } else {
                throw new IllegalArgumentException(errMessage + ": [" + tokens[0]
                        + "] is neither a boolean value nor a comparison operator is specified.");
            }
            break;
        default:
            throw new IllegalArgumentException(errMessage);
        }
    }

    private static void checkVariableSyntax(String str, int pos, String parentStr) throws IllegalArgumentException, IOException {
        String strWithoutQuotedParts = withoutQuotedParts(str);
        if (strWithoutQuotedParts.contains("variable(") || strWithoutQuotedParts.contains("argument(")) {
            parseVariable(str, pos, parentStr);
        } else if (strWithoutQuotedParts.contains("$")) {
            parseDollarVariable(str, pos, parentStr);
        }
    }

    private static boolean isStringVariable(String str) {
        if (str.startsWith("variable(") && str.endsWith(")")) {
            return true;
        } else if (str.startsWith("${") && str.endsWith("}")) {
            return true;
        } else if (str.startsWith("$") && !(str.endsWith(".toNumber") || str.endsWith(".toBoolean"))) {
            return true;
        }
        return false;
    }

    private static boolean isString(String str) {
        return (str.startsWith("\"") && str.endsWith("\"")) || (str.startsWith("'") && str.endsWith("'"));
    }

    private static boolean isBoolean(String str) {
        return "true".equals(str) || "false".equals(str) || str.endsWith(".toBoolean");
    }

    private static boolean isNumeric(String str) {
        boolean isNumeric = "returnCode".equals(str) || str.endsWith(".toNumber");
        if (!isNumeric) {
            try {
                Double.parseDouble(str);
                isNumeric = true;
            } catch (NumberFormatException e) {
            }
        }
        return isNumeric;
    }

}
