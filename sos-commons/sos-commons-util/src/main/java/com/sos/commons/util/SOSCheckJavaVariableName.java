package com.sos.commons.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class SOSCheckJavaVariableName {

    private static final List<String> javaReservedWords = Arrays.asList("abstract", "continue", "for", "new", "switch", "assert", "default", "goto",
            "package", "synchronized", "boolean", "do", "if", "private", "this", "break", "double", "implements", "protected", "throw", "byte",
            "else", "import", "public", "throws", "case", "enum", "instanceof", "return", "transient", "catch", "extends", "int", "short", "try",
            "char", "final", "interface", "static", "void", "class", "finally", "long", "strictfp", "volatile", "const", "float", "native", "super",
            "while");
    private static final Pattern controlCharsPattern = Pattern.compile("[\\x00-\\x1F\\x7F\\x80-\\x9F]");
    private static final Predicate<String> controlChars = controlCharsPattern.asPredicate();
    private static final Pattern spaceCharsPattern = Pattern.compile("\\s");
    private static final Predicate<String> spaceChars = spaceCharsPattern.asPredicate();
    // punction and symbol chars without ._-
    private static final Pattern punctuationAndSymbolCharsPattern = Pattern.compile(
            "[\\x21-\\x2C\\x2F\\x3A-\\x40\\x5B-\\x5E\\x60\\x7B-\\x7E\\xA0-\\xBF\\xD7\\xF7]");
    private static final Predicate<String> punctuationAndSymbolChars = punctuationAndSymbolCharsPattern.asPredicate();
//    private static final Predicate<String> digits = Pattern.compile("\\d").asPredicate();
    private static final Pattern leadingHyphensAndDotsPattern = Pattern.compile("^[.-]");
    private static final Predicate<String> leadingHyphensAndDots = leadingHyphensAndDotsPattern.asPredicate();
    private static final Pattern trailingHyphensAndDotsPattern = Pattern.compile("[.-]$");
    private static final Predicate<String> trailingHyphensAndDots = trailingHyphensAndDotsPattern.asPredicate();
    private static final Pattern consecutiveHyphensAndDotsPattern = Pattern.compile("\\.\\.+|--+");
    private static final Predicate<String> consecutiveHyphensAndDots = consecutiveHyphensAndDotsPattern.asPredicate();

    private enum Result {
        CONTROL, PUNCTUATION, DIGIT, SPACE, LEADING_OR_TRAILING, IN_A_ROW, RESERVED, EMPTY, OK
    }
    

    private static final Map<Result, String> errorMessages = Collections.unmodifiableMap(new HashMap<Result, String>() {

        private static final long serialVersionUID = 1L;

        {
            put(Result.CONTROL, "Control characters are not allowed in '%s': '%s'");
            put(Result.PUNCTUATION, "Punctuations (except '_', '-' and '.') or symbols are not allowed in '%s': '%s'");
            put(Result.RESERVED, "'%s': '%s' is a reserved word and must not be used");
            put(Result.EMPTY, "'%s' must not be empty");
            //put(Result.DIGIT, "'%s': '%s' must not begin with a number");
            put(Result.LEADING_OR_TRAILING, "'%s': '%s' must not begin or end with a hypen or dot");
            put(Result.IN_A_ROW, "'%s': '%s' must not contain consecutive hyphens or dots");
            put(Result.SPACE, "Spaces are not allowed in '%s': '%s'");
        }
    });

    /**
     * Checks if 'value' complies all rules of a java variable name
     * @param value
     * @return boolean
     *      true iff 'value' complies all rules 
     */
    public static boolean test(String value) {
        if(check(value) == null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks if 'value' complies all rules of a java variable name
     * @param value
     * @return Either&lt;String, Void&gt;
     *      the 'either' is right iff 'value' complies all rules
     *      otherwise the left hand side string of the 'either' contains an error message 
     *      to use in String.format(either.getLeft(), "myString").
     */
    public static String check(String value) {
        if (value == null || value.isEmpty()) {
            return errorMessages.get(Result.EMPTY);
        }
        if (spaceChars.test(value)) {
            return errorMessages.get(Result.SPACE);
        }
        if (punctuationAndSymbolChars.test(value)) {
            return errorMessages.get(Result.PUNCTUATION);
        }
//        if (digits.test(value.substring(0, 1))) {
//            return errorMessages.get(Result.DIGIT);
//        }
        if (leadingHyphensAndDots.test(value) || trailingHyphensAndDots.test(value)) {
            return errorMessages.get(Result.LEADING_OR_TRAILING);
        }
        if (consecutiveHyphensAndDots.test(value)) {
            return errorMessages.get(Result.IN_A_ROW);
        }
        if (controlChars.test(value)) {
            return errorMessages.get(Result.CONTROL);
        }
        if (javaReservedWords.contains(value)) {
            return errorMessages.get(Result.RESERVED);
        }
        return null;
    }
    
    public static String makeStringRuleConform(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        if (javaReservedWords.contains(value)) {
            return value.substring(0, 1).toUpperCase() + value.substring(1);
        }
        value = leadingHyphensAndDotsPattern.matcher(value).replaceAll("");
        value = trailingHyphensAndDotsPattern.matcher(value).replaceAll("");
        value = controlCharsPattern.matcher(value).replaceAll("");
        value = punctuationAndSymbolCharsPattern.matcher(value).replaceAll("");
        value = value.replaceAll("\\s+", "-");
        value = value.replaceAll("--+", "-");
        value = value.replaceAll("\\.\\.+", ".");
        return value;
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
        String errorMessage = check(value);
        if (errorMessage != null) {
            throw new IllegalArgumentException(String.format(errorMessage, key, value));
        }
    }

}
