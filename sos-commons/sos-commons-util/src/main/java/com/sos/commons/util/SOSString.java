package com.sos.commons.util;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class SOSString {

    public static boolean isEmpty(String string) {
        return string == null || string.isEmpty();
    }

    public static String toString(Object o) {
        if (o == null) {
            return null;
        }
        try {
            return ReflectionToStringBuilder.toString(o, ToStringStyle.SHORT_PREFIX_STYLE);
        } catch (Throwable t) {
        }
        return o.toString();
    }
}