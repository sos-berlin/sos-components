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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SOSString {

    private static final String DEFAULT_JOIN_DELIMITER = ",";

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
        return toString(o, excludeFieldNames, excludeNullValues, 0, 0);
    }

    private static String toString(Object o, Collection<String> excludeFieldNames, boolean excludeNullValues, int error, int recursion) {
        if (o == null) {
            return TO_STRING_NULL_VALUE;
        }
        if (error > 100) {
            return TO_STRING_UNKNOWN_VALUE;
        }

        recursion += 1;
        if (recursion > 5) {
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
                        r.add(toString(Array.get(o, i), excludeFieldNames, excludeNullValues, error, recursion));
                    }
                    sb.append(String.join(",", r));
                    sb.append(']');
                }
            } else if (SOSReflection.isEnum(clazz)) {
                sb.append(clazzSimpleName);
                Enum<?> e = (Enum<?>) o;
                sb.append("[");
                sb.append(e.name());
                sb.append("]");
            } else if (o instanceof Collection<?>) {
                sb.append('[');
                List<String> r = new ArrayList<>();
                Collection<?> coll = (Collection<?>) o;
                Iterator<?> it = coll.iterator();
                while ((it.hasNext())) {
                    r.add(toString(it.next(), excludeFieldNames, excludeNullValues, error, recursion));
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
                    r.add(key + "=" + toString(map.get(key), excludeFieldNames, excludeNullValues, error, recursion));
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
            } else if (o instanceof Date) {
                try {
                    sb.append(SOSDate.getDateTimeAsString((Date) o));
                } catch (Throwable e) {
                    sb.append(o == null ? TO_STRING_NULL_VALUE : o.toString());
                }
            } else {
                String clazzCanonicalName = clazz.getCanonicalName();// can be null
                if (clazzCanonicalName != null && clazzCanonicalName.matches(TO_STRING_JAVA_INTERNAL_REGEX)) {
                    sb.append(o.toString());
                } else {
                    List<String> r = new ArrayList<>();
                    // final Field[] fields = clazz.getDeclaredFields();
                    // Arrays.sort(fields, Comparator.comparing(Field::getName));
                    final List<Field> fields = SOSReflection.getAllDeclaredFields(clazz);
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
                            r.add(fn + "=" + toString(val, excludeFieldNames, excludeNullValues, error, recursion));
                            // r.add(fn + "=" + val);
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

    public static String removePrefix(Object val, String prefix) {
        if (val == null) {
            return null;
        }
        String v = (val instanceof String) ? (String) val : val.toString();
        if (prefix == null) {
            return v;
        }
        int prefixLength = prefix.length();
        int valLength = v.length();
        if (prefixLength > valLength) {
            return v;
        }
        for (int i = 0; i < prefixLength; i++) {
            if (v.charAt(i) != prefix.charAt(i)) {
                return v;
            }
        }
        return v.substring(prefixLength);
    }

    public static List<String> splitByLength(String input, int maxLength) {
        return splitByLength(input, maxLength, " ");
    }

    public static List<String> splitByLength(String input, int maxLength, String splitter) {
        List<String> result = new ArrayList<>();
        int length = input.length();
        int lastBreak = 0;

        while (lastBreak < length) {
            int nextBreak = lastBreak + maxLength;
            if (nextBreak >= length) {
                result.add(input.substring(lastBreak));
                break;
            }
            int lastSpace = input.lastIndexOf(splitter, nextBreak);
            if (lastSpace > lastBreak) {
                result.add(input.substring(lastBreak, lastSpace));
                lastBreak = lastSpace + 1;
            } else {
                result.add(input.substring(lastBreak, nextBreak));
                lastBreak = nextBreak;
            }
        }
        return result;
    }

    public static String trim(String input, String... trimChars) {
        if (isEmpty(input) || trimChars.length == 0) {
            return input;
        }
        // String regex = "^[" + trimChars + "]+|[" + trimChars + "]+$";
        // input = input.replaceAll(regex, "");
        String val = input.trim();
        for (String trim : trimChars) {
            if (SOSString.isEmpty(trim)) {
                continue;
            }
            while (val.startsWith(trim)) {
                val = val.substring(trim.length()).trim();
            }
            while (val.endsWith(trim)) {
                val = val.substring(0, val.length() - trim.length()).trim();
            }
        }
        return val.trim();
    }

    public static String remove4ByteCharacters(String val) {
        if (isEmpty(val)) {
            return val;
        }
        StringBuilder result = new StringBuilder();
        int length = val.length();
        for (int i = 0; i < length; i++) {
            char ch = val.charAt(i);
            if (Character.isHighSurrogate(ch)) {
                if (i + 1 < length && Character.isLowSurrogate(val.charAt(i + 1))) {
                    i++;
                } else {
                    result.append(ch);
                }
            } else if (Character.isLowSurrogate(ch)) {
                result.append(ch);
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }

    public static String zeroPad(int val, int zeroCount) {
        return String.format("%0" + zeroCount + "d", val);
    }

    public static String join(Collection<?> collection) {
        return join(collection, DEFAULT_JOIN_DELIMITER, Object::toString);
    }

    /** e.g.: SOSString.join(myCollection, n -> SOSString.zeroPad(n, 2))
     * 
     * @param <T>
     * @param collection
     * @param transformation
     * @return */
    public static <T> String join(Collection<T> collection, Function<T, String> transformation) {
        return join(collection, DEFAULT_JOIN_DELIMITER, transformation);
    }

    /** e.g.: SOSString.join(myCollection,";", n -> SOSString.zeroPad(n, 2))
     * 
     * @param <T>
     * @param collection
     * @param delimiter
     * @param transformation
     * @return */
    public static <T> String join(Collection<T> collection, String delimiter, Function<T, String> transformation) {
        return collection.stream().map(transformation).collect(Collectors.joining(delimiter));
    }
}