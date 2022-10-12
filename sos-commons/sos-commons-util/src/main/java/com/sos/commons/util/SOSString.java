package com.sos.commons.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class SOSString {

    public static boolean isEmpty(String string) {
        return string == null || string.isEmpty();
    }

    public static boolean equals(String a, String b) {
        return Objects.equals(a, b);
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
            ReflectionToStringBuilder builder = new ReflectionToStringBuilder(o, ToStringStyle.SHORT_PREFIX_STYLE) {

                @Override
                protected Object getValue(Field field) throws IllegalArgumentException, IllegalAccessException {
                    Object val = field.get(this.getObject());
                    if (val != null && val instanceof String) {
                        String v = val.toString();
                        if (v.length() > 255) {
                            val = v.substring(0, 255) + "<truncated>";
                        }
                    }
                    return val;
                }
            };
            if (excludeFieldNames != null) {
                builder.setExcludeFieldNames(excludeFieldNames.stream().toArray(String[]::new));
            }
            return builder.toString();
        } catch (Throwable t) {
        }
        return o.toString();
    }

    public static String hash256(String val) {
        return hash(val, "SHA-256");
    }

    public static String hash512(String val) {
        return hash(val, "SHA-512");
    }

    private static String hash(String val, String hashAlgorithm) {
        try {
            MessageDigest digest = MessageDigest.getInstance(hashAlgorithm);
            byte[] hash = digest.digest(val.getBytes(StandardCharsets.UTF_8));
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String toString(Throwable throwable) {
        try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
            throwable.printStackTrace(pw);
            return sw.toString();
        } catch (Exception e) {
            return throwable.toString();
        }
    }

    public static List<String> toList(String val, String delimiter) {
        if (val == null) {
            return null;
        }
        return Stream.of(val.split(delimiter)).map(String::trim).collect(Collectors.toList());
    }
}