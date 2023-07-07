package com.sos.joc.classes.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class FilenameSanitizer {

    private static final Pattern controlCharsPattern = Pattern.compile("[\\x00-\\x1F\\x7F\\x80-\\x9F]");
    private static final Predicate<String> controlChars = controlCharsPattern.asPredicate();
    private static final Pattern disallowedCharsPattern = Pattern.compile("[*?\"<>]");
    private static final Predicate<String> disallowedCharsChars = disallowedCharsPattern.asPredicate();
    
    private enum Result {
        CONTROL, DISALLOWED, EMPTY
    }
    

    private static final Map<Result, String> errorMessages = Collections.unmodifiableMap(new HashMap<Result, String>() {

        private static final long serialVersionUID = 1L;

        {
            put(Result.CONTROL, "Control characters are not allowed in '%s': '%s'");
            put(Result.DISALLOWED, "*?\"<> are not allowed in '%s': '%s'");
            put(Result.EMPTY, "'%s' must not be empty");
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
     * Checks if 'value' complies all rules
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
        // checks paths too
        if (disallowedCharsChars.test(value.replaceAll("[\\\\/]", ""))) {
            return errorMessages.get(Result.DISALLOWED);
        }
        if (controlChars.test(value.replaceAll("[\\\\/]", ""))) {
            return errorMessages.get(Result.CONTROL);
        }
        return null;
    }
    
    public static String makeStringRuleConform(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        value = controlCharsPattern.matcher(value).replaceAll("");
        value = disallowedCharsPattern.matcher(value).replaceAll("_");
        return value;
    }
    
    /**
     * Checks if 'value' complies all rules
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
