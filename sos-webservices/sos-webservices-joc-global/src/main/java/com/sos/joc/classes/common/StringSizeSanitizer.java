package com.sos.joc.classes.common;

import java.nio.charset.StandardCharsets;

public class StringSizeSanitizer {

    private static final int oneMB = 1024 * 1024; //1MB
    private static final int maxSize = 2 * oneMB; //2MB
    private static final String errorMessagePattern = "The value of %s has %dB more than the limit of 2MB";
    private static final String errorMessagePattern2 = "The value has %dB more than the limit of 2MB";
    
    /**
     * Checks if 'value' complies max size
     * @param value
     * @return boolean
     *      true iff 'value' complies all rules 
     */
    public static boolean test(Object value) {
        return check(value) == null;
    }
    
    public static boolean test(String value) {
        return check(value) == null;
    }

    /**
     * Checks if 'value' complies max size
     * @param value
     * @return Integer size of bytes that are more than the limit iff too big otherwise null
     */
    public static Integer check(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return check((String) value);
        }
        return null;
    }
    
    public static Integer check(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        int size = value.getBytes(StandardCharsets.UTF_8).length;
        if (size > maxSize) {
            return size - maxSize;
        }
        return null;
    }
    
    /**
     * Checks if 'value' complies max size
     * @param key
     *      is used in the error message of the IllegalArgumentException
     * @param value
     * @throws IllegalArgumentException
     *      will be raise iff 'value' doesn't comply max size
     */
    public static void test(String key, Object value) throws IllegalArgumentException {
        Integer size = check(value);
        if (size != null) {
            if (key != null) {
                throw new IllegalArgumentException(String.format(errorMessagePattern, key, size));
            } else {
                throw new IllegalArgumentException(String.format(errorMessagePattern2, size));
            }
        }
    }
    
    public static void test(String key, String value) throws IllegalArgumentException {
        Integer size = check(value);
        if (size != null) {
            if (key != null) {
                throw new IllegalArgumentException(String.format(errorMessagePattern, key, size));
            } else {
                throw new IllegalArgumentException(String.format(errorMessagePattern2, size));
            }
        }
    }

}
