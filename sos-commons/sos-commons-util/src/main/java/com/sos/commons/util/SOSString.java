package com.sos.commons.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SOSString {

    private static final String TO_STRING_JAVA_INTERNAL_REGEX = "^(java|javax|sun|com\\.sun|com\\.oracle|jdk)\\..*";
    private static final String TO_STRING_NULL_VALUE = "<null>";
    private static final String TO_STRING_UNKNOWN_VALUE = "<unknown>";
    private static final String TO_STRING_TRUNCATED_VALUE_SUFFIX = "<truncated>";
    private static final int TO_STRING_TRUNCATE_VALUE_IF_LONGER_THAN = 255;

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
        return toString(o, null, false);
    }

    public static String toString(Object o, boolean excludeNullValues) {
        return toString(o, null, excludeNullValues);
    }

    public static String toString(Object o, Collection<String> excludeFieldNames) {
        return toString(o, excludeFieldNames, false);
    }

    private static String toString(Object o, Collection<String> excludeFieldNames, boolean excludeNullValues) {
        return toString(o, excludeFieldNames, excludeNullValues, 0);
    }

    private static String toString(Object o, Collection<String> excludeFieldNames, boolean excludeNullValues, int error) {
        if (o == null) {
            return TO_STRING_NULL_VALUE;
        }
        if (error > 100) {
            return TO_STRING_UNKNOWN_VALUE;
        }
        try {
            Class<?> clazz = o.getClass();
            String clazzSimpleName = clazz.getSimpleName();
            StringBuilder sb = new StringBuilder();
            if (clazz.isArray()) {
                if (clazzSimpleName.equals("byte[]")) {
                    sb.append("[...]");
                } else {
                    sb.append('[');
                    List<String> r = new ArrayList<>();
                    int len = Array.getLength(o);
                    for (int i = 0; i < len; i++) {
                        r.add(toString(Array.get(o, i), excludeFieldNames, excludeNullValues, error));
                    }
                    sb.append(String.join(",", r));
                    sb.append(']');
                }
            } else if (SOSReflection.isEnum(clazz)) {
                sb.append(clazzSimpleName);
                Enum<?> e = (Enum<?>) o;
                sb.append("[name=");
                sb.append(e.name());
                sb.append("]");
            } else if (o instanceof Collection<?>) {
                sb.append('[');
                List<String> r = new ArrayList<>();
                Collection<?> coll = (Collection<?>) o;
                Iterator<?> it = coll.iterator();
                while ((it.hasNext())) {
                    r.add(toString(it.next(), excludeFieldNames, excludeNullValues, error));
                }
                sb.append(String.join(",", r));
                sb.append(']');
            } else if (o instanceof Map) {
                sb.append('{');
                List<String> r = new ArrayList<>();
                Map<?, ?> map = (Map<?, ?>) o;
                Iterator<?> it = map.keySet().iterator();
                while ((it.hasNext())) {
                    Object key = it.next();
                    r.add(key + "=" + toString(map.get(key), excludeFieldNames, excludeNullValues, error));
                }
                sb.append(String.join(",", r));
                sb.append('}');
            } else if (o instanceof CharSequence) {
                String val = o.toString();
                if (val == null) {
                    sb.append(TO_STRING_NULL_VALUE);
                } else {
                    if (val.length() > TO_STRING_TRUNCATE_VALUE_IF_LONGER_THAN) {
                        val = val.substring(0, TO_STRING_TRUNCATE_VALUE_IF_LONGER_THAN) + TO_STRING_TRUNCATED_VALUE_SUFFIX;
                    }
                    sb.append(val);
                }
            } else {
                if (o.getClass().getCanonicalName().matches(TO_STRING_JAVA_INTERNAL_REGEX)) {
                    sb.append(o.toString());
                } else {
                    List<String> r = new ArrayList<>();
                    // final Field[] fields = o.getClass().getDeclaredFields();
                    // Arrays.sort(fields, Comparator.comparing(Field::getName));
                    final List<Field> fields = SOSReflection.getAllDeclaredFields(o.getClass());
                    for (Field field : fields) {
                        final String fn = field.getName();
                        if (fn.indexOf('$') != -1) {// reject field from inner class
                            continue;
                        }
                        if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
                            continue;
                        }
                        if (excludeFieldNames != null && excludeFieldNames.contains(fn)) {
                            continue;
                        }
                        try {
                            field.setAccessible(true);
                            Object val = field.get(o);
                            if (excludeNullValues && val == null) {
                                continue;
                            }
                            r.add(fn + "=" + toString(val, excludeFieldNames, excludeNullValues, error));
                        } catch (Throwable e) {
                            error++;
                            if (!r.contains(TO_STRING_UNKNOWN_VALUE)) {
                                // r.add(fn + "=<unknown>");
                                r.add(TO_STRING_UNKNOWN_VALUE);
                            }
                        }
                    }
                    if (r.size() > 0) {
                        sb.append(clazzSimpleName);
                        sb.append("[");
                        sb.append(String.join(",", r));
                        sb.append("]");
                    }
                }
            }
            return sb.toString();
        } catch (Throwable t) {
            // t.printStackTrace();
            // return TO_STRING_UNKNOWN_VALUE instead of o.toString() to avoid StackOverflowException(if o.toString() uses SOSString.toString)
            return TO_STRING_UNKNOWN_VALUE;
        }
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