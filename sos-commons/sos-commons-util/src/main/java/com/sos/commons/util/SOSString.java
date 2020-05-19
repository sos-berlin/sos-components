package com.sos.commons.util;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.google.common.hash.Hashing;

public class SOSString {

    public static boolean isEmpty(String string) {
        return string == null || string.isEmpty();
    }

    public static String mapToString(Map<String, ?> o, boolean newLine) {
        StringBuilder sb = new StringBuilder();
        String add = newLine ? "\n" : "";
        o.forEach((k, v) -> sb.append("[").append(k).append("->").append(toString(v)).append("]").append(add));
        return sb.toString().trim();
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

    public static String hash(String val) {
        return Hashing.sha256().hashString(val, StandardCharsets.UTF_8).toString();
    }
}