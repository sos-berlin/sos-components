package com.sos.commons.util;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class SOSString {

    public static boolean isEmpty(String string) {
        return string == null || string.isEmpty();
    }

    public static String toString(Object o) {
        if (o == null) {
            return null;
        }
        try {
            return ReflectionToStringBuilder.toString(o);
        } catch (Throwable t) {
        }
        return o.toString();
    }
}