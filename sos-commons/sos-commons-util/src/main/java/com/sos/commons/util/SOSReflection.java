package com.sos.commons.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
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
        List<Field> fields = new ArrayList<Field>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
        }
        return fields;
    }

    public static boolean isEnum(String className) throws ClassNotFoundException {
        return isEnum(Class.forName(className));
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

}
