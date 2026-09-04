package com.sos.joc.classes.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class StringSanitizer {
    private static final char FORBIDDEN_LEFT_ANGLE_BRACKET = '\u003C';
    private static final char FORBIDDEN_RIGHT_ANGLE_BRACKET = '\u003E';
    private static final char LEFT_ANGLE_BRACKET = '\u02C2'; //https://www.compart.com/de/unicode/U+02C2
    private static final char RIGHT_ANGLE_BRACKET = '\u02C3'; //https://www.compart.com/de/unicode/U+02C3
    /*
     * allowed control chars are:
     *      x09 = HT horizontal tab
     *      x0A = LF line feed
     *      x0D = CR carriage return
     * */
    private static final Pattern FORBIDDEN_CONTROL_CHARS_PATTERN = Pattern.compile("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F\\x80-\\x9F]");
    private static final Predicate<String> FORBIDDEN_CONTROL_CHARS_PREDICATE = FORBIDDEN_CONTROL_CHARS_PATTERN.asPredicate();
    private static final Pattern FORBIDDEN_CHARS_PATTERN = Pattern.compile("[<>]");
    private static final Predicate<String> FORBIDDEN_CHARS_PREDICATE = FORBIDDEN_CHARS_PATTERN.asPredicate();
    
    private enum Result {
        CONTROL, FORBIDDEN, EMPTY
    }
    

    private static final Map<Result, String> ERROR_MESSAGES = Collections.unmodifiableMap(new HashMap<Result, String>() {
        private static final long serialVersionUID = 1L;
        {
            put(Result.CONTROL, "Control characters are not allowed in '%s': '%s'");
            put(Result.FORBIDDEN, "*?\"<> are forbidden in '%s': '%s'");
            put(Result.EMPTY, "'%s' must not be empty");
        }
    });

    public static String sanitize(String s) {
        // < and > will be replaced to avoid injection via html and svg
        return s.replace(FORBIDDEN_LEFT_ANGLE_BRACKET, LEFT_ANGLE_BRACKET).replace(FORBIDDEN_RIGHT_ANGLE_BRACKET, RIGHT_ANGLE_BRACKET);
    }
    
    /**
     * Checks if 'value' complies all rules
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
     * @return String errorMessage or null 
     */
    public static String check(String value) {
        if (value == null || value.isEmpty()) {
            return ERROR_MESSAGES.get(Result.EMPTY);
        }
        // checks paths too
        if (FORBIDDEN_CHARS_PREDICATE.test(value)) {
            return ERROR_MESSAGES.get(Result.FORBIDDEN);
        }
        if (FORBIDDEN_CONTROL_CHARS_PREDICATE.test(value)) {
            return ERROR_MESSAGES.get(Result.CONTROL);
        }
        return null;
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
