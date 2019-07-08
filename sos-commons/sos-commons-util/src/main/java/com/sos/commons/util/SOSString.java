package com.sos.commons.util;

import java.util.Collection;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class SOSString {

    public static boolean isEmpty(String string) {
        return string == null || string.isEmpty();
    }

    public static String toString(Object o) {
        return toString(o, null);
    }

    public static String toString(Object o, Collection<String> excludeFieldNames) {
        if (o == null) {
            return null;
        }
        try {
            ReflectionToStringBuilder.setDefaultStyle(ToStringStyle.SHORT_PREFIX_STYLE);

            if (excludeFieldNames == null) {
                return ReflectionToStringBuilder.toString(o);
            } else {
                return ReflectionToStringBuilder.toStringExclude(o, excludeFieldNames);
            }
        } catch (Throwable t) {
        }
        return o.toString();
    }
}