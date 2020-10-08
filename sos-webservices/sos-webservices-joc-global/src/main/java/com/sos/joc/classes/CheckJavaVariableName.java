package com.sos.joc.classes;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import io.vavr.control.Either;

public class CheckJavaVariableName {

    private static final List<String> javaReservedWords = Arrays.asList("abstract", "continue", "for", "new", "switch", "assert", "default", "goto",
            "package", "synchronized", "boolean", "do", "if", "private", "this", "break", "double", "implements", "protected", "throw", "byte",
            "else", "import", "public", "throws", "case", "enum", "instanceof", "return", "transient", "catch", "extends", "int", "short", "try",
            "char", "final", "interface", "static", "void", "class", "finally", "long", "strictfp", "volatile", "const", "float", "native", "super",
            "while");
    private static final Predicate<String> controlChars = Pattern.compile("[\\x00-\\x1F\\x7F\\x80-\\x9F]").asPredicate();
    // punction and symbol chars without _, $, ¢, £, ¤, ¥ and µ
    private static final Predicate<String> punctuationAndSymbolChars = Pattern.compile(
            "[\\x20-\\x23\\x25-\\x2F\\x3A-\\x40\\x5B-\\x5E\\x60\\x7B-\\x7E\\xA0\\xA1\\xA6-\\xB4\\xB6-\\xBF\\xD7\\xF7]").asPredicate();
    private static final Predicate<String> digits = Pattern.compile("\\d").asPredicate();

    private enum Result {
        CONTROL, PUNCTUATION, DIGIT, RESERVED, EMPTY, OK
    }
    

    private static final Map<Result, String> errorMessages = Collections.unmodifiableMap(new HashMap<Result, String>() {

        private static final long serialVersionUID = 1L;

        {
            put(Result.CONTROL, "Control characters are not allowed in '%s': '%s'");
            put(Result.PUNCTUATION, "Punctuations (except '_') or symbols (except '$') are not allowed in '%s': '%s'");
            put(Result.RESERVED, "'%s': '%s' is a reserved word and must not be used");
            put(Result.EMPTY, "'%s' must not be empty");
            put(Result.DIGIT, "'%s': '%s' must not begin with a number");
        }
    });

    /**
     * Checks if 'value' complies all rules of a java variable name
     * @param value
     * @return boolean
     *      true iff 'value' complies all rules 
     */
    public static boolean test(String value) {
        return check(value).isRight();
    }

    /**
     * Checks if 'value' complies all rules of a java variable name
     * @param value
     * @return Either&lt;String, Void&gt;
     *      the 'either' is right iff 'value' complies all rules
     *      otherwise the left hand side string of the 'either' contains an error message 
     *      to use in String.format(either.getLeft(), "myString").
     */
    public static Either<String, Void> check(String value) {
        if (value == null || value.isEmpty()) {
            return Either.left(errorMessages.get(Result.EMPTY));
        }
        if (punctuationAndSymbolChars.test(value)) {
            return Either.left(errorMessages.get(Result.PUNCTUATION));
        }
        if (digits.test(value.substring(0, 1))) {
            return Either.left(errorMessages.get(Result.DIGIT));
        }
        if (controlChars.test(value)) {
            return Either.left(errorMessages.get(Result.CONTROL));
        }
        if (javaReservedWords.contains(value)) {
            return Either.left(errorMessages.get(Result.RESERVED));
        }
        return Either.right(null);
    }
    
    /**
     * Checks if 'value' complies all rules of a java variable name
     * @param key
     *      is used in the error message of the IllegalArgumentException
     * @param value
     * @throws IllegalArgumentException
     *      will be raise iff 'value' doesn't comply all rules
     */
    public static void test(String key, String value) throws IllegalArgumentException {
        Either<String, Void> either = check(value);
        if (either.isLeft()) {
            throw new IllegalArgumentException(String.format(either.getLeft(), key, value));
        }
    }

}
