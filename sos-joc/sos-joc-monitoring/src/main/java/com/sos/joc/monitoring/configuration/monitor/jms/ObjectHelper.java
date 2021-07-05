package com.sos.joc.monitoring.configuration.monitor.jms;

import java.util.Map;

public class ObjectHelper {

    public static Object newInstance(String className, Map<String, String> map) throws Exception {
        Class<?> clazz = ObjectHelper.parseType(className);
        if (map == null || map.size() == 0) {
            return clazz.newInstance();
        }

        Class<?>[] types = new Class<?>[map.size()];
        Object[] parameters = new Object[map.size()];
        int i = 0;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            Class<?> cz = parseType(entry.getKey());
            types[i] = cz;
            parameters[i] = castValue(cz, entry.getValue());
            i++;
        }

        Object o = null;
        try {
            o = clazz.getDeclaredConstructor(types).newInstance(parameters);
        } catch (Throwable e) {
            throw new Exception(String.format("can't create new instance of %s[%s]: %s", className, map, e.toString()), e);
        }
        return o;
    }

    public static Class<?> parseType(final String className) throws Exception {
        switch (className) {
        case "boolean":
            return boolean.class;
        case "double":
            return double.class;
        case "float":
            return float.class;
        case "int":
            return int.class;
        case "long":
            return long.class;
        case "short":
            return short.class;
        default:
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException ex) {
                throw new Exception(String.format("Class not found %s", className));
            }
        }
    }

    public static Object castValue(Class<?> clazz, String value) {
        switch (clazz.getSimpleName()) {
        case "String":
            return value;
        case "int":
            return Integer.parseInt(value);
        case "Integer":
            return Integer.getInteger(value);
        case "boolean":
            return Boolean.parseBoolean(value);
        case "Boolean":
            return Boolean.getBoolean(value);
        case "long":
            return Long.parseLong(value);
        case "Long":
            return Long.getLong(value);
        case "float":
            return Float.parseFloat(value);
        case "double":
            return Double.parseDouble(value);
        case "Double":
            return new Double(Double.parseDouble(value));
        case "Float":
            return new Float(Float.parseFloat(value));
        case "short":
            return Short.parseShort(value);
        case "Short":
            return new Short(Short.parseShort(value));
        default:
            return value;
        }
    }
}
