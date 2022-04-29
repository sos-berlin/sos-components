package com.sos.commons.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class SOSReflection {

    @SuppressWarnings("unchecked")
    public static void changeAnnotationValue(Annotation annotation, String key, Object newValue) {
        Object handler = Proxy.getInvocationHandler(annotation);
        Field f;
        try {
            f = handler.getClass().getDeclaredField("memberValues");
        } catch (NoSuchFieldException | SecurityException e) {
            throw new IllegalStateException(e);
        }
        f.setAccessible(true);
        Map<String, Object> memberValues;
        try {
            memberValues = (Map<String, Object>) f.get(handler);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
        Object oldValue = memberValues.get(key);
        if (oldValue == null || oldValue.getClass() != newValue.getClass()) {
            throw new IllegalArgumentException();
        }
        memberValues.put(key, newValue);
    }

    public static List<Field> getAllDeclaredFields(Class<?> type) {
        List<Field> result = new ArrayList<>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            result.addAll(Arrays.asList(c.getDeclaredFields()));
        }
        return result;
    }

    public static List<Method> getAllDeclaredMethods(Class<?> type) {
        List<Method> result = new ArrayList<>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            result.addAll(Arrays.asList(c.getDeclaredMethods()));
        }
        return result;
    }

    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        }
        // TODO getAllDeclaredFields?
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object val = field.get(obj);
                if (val != null) {
                    if (val instanceof CharSequence) {
                        if (!SOSString.isEmpty(val.toString())) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            } catch (Throwable e) {
                return false;
            }
        }
        return true;
    }

    public static boolean isList(Type type) throws ClassNotFoundException {
        return isList(Class.forName(normalizeClassForName(type.getTypeName())));
    }

    public static boolean isList(Class<?> cls) {
        return List.class.isAssignableFrom(cls);
    }

    public static boolean isEnum(Type type) throws ClassNotFoundException {
        return isEnum(Class.forName(normalizeClassForName(type.getTypeName())));
    }

    public static boolean isEnum(Class<?> cls) {
        return Enum.class.isAssignableFrom(cls);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Enum<?> enumValueOf(String enumClassName, String val) throws ClassNotFoundException {
        return Enum.valueOf((Class<? extends Enum>) Class.forName(enumClassName), val);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Enum<?>> T enumIgnoreCaseValueOf(String enumClassName, String val) throws ClassNotFoundException {
        return enumIgnoreCaseValueOf((Class<T>) Class.forName(enumClassName), val);
    }

    public static <T extends Enum<?>> T enumIgnoreCaseValueOf(Class<T> enumeration, String val) {
        return Stream.of(enumeration.getEnumConstants()).filter(e -> e.name().equalsIgnoreCase(val)).findAny().orElse(null);
    }

    private static String normalizeClassForName(final String className) {
        int indx = className.indexOf('<');
        return indx > -1 ? className.substring(0, indx) : className;
    }
}
