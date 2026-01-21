package com.sos.commons.util;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
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

    public static void setDeclaredFieldValue(Object o, String fieldName, Object fieldValue) throws Exception {
        Class<?> clazz = o.getClass();
        Field field = null;

        while (clazz != null) {
            try {
                field = clazz.getDeclaredField(fieldName);
                break;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }

        if (field == null) {
            throw new NoSuchFieldException("Field '" + fieldName + "' not found in class hierarchy of " + o.getClass().getName());
        }

        field.setAccessible(true);
        field.set(o, fieldValue);

    }

    public static List<Field> getFields(Class<?> type) {
        if (type == null) {
            return null;
        }
        return Arrays.asList(type.getFields());
    }

    public static List<Field> getDeclaredFields(Class<?> type) {
        if (type == null) {
            return null;
        }
        return Arrays.asList(type.getDeclaredFields());
    }

    public static List<Field> getAllDeclaredFields(Class<?> type) {
        if (type == null) {
            return null;
        }
        List<Field> result = new ArrayList<>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            result.addAll(Arrays.asList(c.getDeclaredFields()));
        }
        return result;
    }

    public static List<Method> getDeclaredMethods(Class<?> type) {
        if (type == null) {
            return null;
        }
        return Arrays.asList(type.getDeclaredMethods());
    }

    public static List<Method> getAllDeclaredMethods(Class<?> type) {
        if (type == null) {
            return null;
        }
        List<Method> result = new ArrayList<>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            result.addAll(Arrays.asList(c.getDeclaredMethods()));
        }
        return result;
    }

    public static Map<String, Method> getAllDeclaredMethodsAsMap(Class<?> type) {
        if (type == null) {
            return null;
        }
        Map<String, Method> result = new HashMap<>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            for (Method m : c.getDeclaredMethods()) {
                result.putIfAbsent(m.getName(), m);
            }
        }
        return result;
    }

    public static List<Method> getMethods(Class<?> type) {
        if (type == null) {
            return null;
        }
        return Arrays.asList(type.getMethods());
    }

    public static List<Method> getAllMethods(Class<?> type) {
        if (type == null) {
            return null;
        }
        Set<Method> result = new HashSet<>(); // without duplicate methods e.g. from this, super etc
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            result.addAll(Arrays.asList(c.getMethods()));
        }
        // result.sort(Comparator.comparing(Method::getName));
        return new ArrayList<>(result);
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
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    public static List<Path> getJarsFromClassPath(String specificDirectory) {
        String[] arr = System.getProperty("java.class.path").split(File.pathSeparator);
        // .../patches/*
        return Stream.of(arr).filter(f -> f.endsWith(".jar")).map(Paths::get).filter(path -> {
            File file = path.toFile();
            return file.isFile() && file.getParentFile().getName().equals(specificDirectory);
        }).collect(Collectors.toList());
    }

    public static List<Class<?>> findClassesInJarFile(Path filePath, Class<?> targetInterface) throws Exception {
        List<Class<?>> r = new ArrayList<>();

        final File file = filePath.toFile();
        URL[] urls = { new URL("jar:file:" + file.getAbsolutePath() + "!/") };
        try (URLClassLoader classLoader = URLClassLoader.newInstance(urls)) {
            try (JarFile jarFile = new JarFile(file)) {
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (entry.getName().endsWith(".class")) {
                        String className = entry.getName().replace("/", ".").replace(".class", "");
                        try {
                            Class<?> clazz = classLoader.loadClass(className);
                            int modifiers = clazz.getModifiers();
                            if (targetInterface.isAssignableFrom(clazz) && !clazz.isInterface() && !java.lang.reflect.Modifier.isAbstract(
                                    modifiers)) {
                                r.add(clazz);
                            }
                        } catch (NoClassDefFoundError | ClassNotFoundException e) {
                            // System.err.println("Error loading class " + className + " from " + file.getName() + ": " + e.getMessage());
                        }
                    }
                }
            }
        }
        return r;
    }

    public static boolean isArray(Class<?> cls) {
        return cls.isArray();
    }

    public static boolean isCollection(Type type) throws ClassNotFoundException {
        return isCollection(Class.forName(normalizeClassForName(type.getTypeName())));
    }

    public static boolean isCollection(Class<?> cls) {
        return Collection.class.isAssignableFrom(cls);
    }

    public static boolean isSet(Type type) throws ClassNotFoundException {
        return isSet(Class.forName(normalizeClassForName(type.getTypeName())));
    }

    public static boolean isSet(Class<?> cls) {
        return Set.class.isAssignableFrom(cls);
    }

    public static boolean isMap(Type type) throws ClassNotFoundException {
        return isMap(Class.forName(normalizeClassForName(type.getTypeName())));
    }

    public static boolean isMap(Class<?> cls) {
        return Map.class.isAssignableFrom(cls);
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
