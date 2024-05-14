package com.sos.commons.hibernate.transform;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.hibernate.query.TupleTransformer;

import com.sos.commons.util.SOSReflection;

public class SOSAliasToBeanResultTransformer<T> implements TupleTransformer<T> {

    private final boolean initialized;
    private final Class<T> resultClass;

    private Map<String, Method> setter;

    public SOSAliasToBeanResultTransformer(Class<T> resultClass) {
        this.initialized = false;
        this.resultClass = resultClass;
    }

    @Override
    public T transformTuple(Object[] tuple, String[] aliases) {
        if (tuple.length != aliases.length) {
            throw new IllegalArgumentException("Length of aliases array must match length of tuple array");
        }
        try {
            if (!initialized) {
                initialize();
            }

            T obj = resultClass.getDeclaredConstructor().newInstance();
            for (int i = 0; i < aliases.length; i++) {
                invokeSetter(obj, aliases[i], tuple[i]);
            }
            return obj;
        } catch (Throwable e) {
            throw new RuntimeException("Failed to instantiate result class", e);
        }
    }

    private void initialize() {
        setter = SOSReflection.getAllDeclaredMethodsAsMap(resultClass).entrySet().stream().filter(e -> e.getKey().startsWith("set") && e.getValue()
                .getParameterCount() == 1).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    private void invokeSetter(Object obj, String fieldName, Object value) throws Exception {
        Method m = setter.get(getSetterName(fieldName));
        if (m != null) {
            m.invoke(obj, value);
        }
    }

    private static String getSetterName(String fieldName) {
        return "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

}
