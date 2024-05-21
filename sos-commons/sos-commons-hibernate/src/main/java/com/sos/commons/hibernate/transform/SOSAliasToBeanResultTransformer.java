package com.sos.commons.hibernate.transform;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.hibernate.query.TupleTransformer;

import com.sos.commons.util.SOSReflection;

public class SOSAliasToBeanResultTransformer<T> implements TupleTransformer<T> {

    private final Class<T> resultClass;

    private Map<String, Method> setter = null;

    public SOSAliasToBeanResultTransformer(Class<T> resultClass) {
        this.resultClass = resultClass;
    }

    @Override
    public T transformTuple(Object[] tuple, String[] aliases) {
        if (tuple.length != aliases.length) {
            throw new IllegalArgumentException("length of aliases array must match length of tuple array");
        }

        if (setter == null) {
            initialize();
        }

        T obj = newResultClassInstance();
        for (int i = 0; i < aliases.length; i++) {
            invokeSetter(obj, aliases[i], tuple[i]);
        }
        return obj;
    }

    private void initialize() {
        try {
            setter = SOSReflection.getAllDeclaredMethodsAsMap(resultClass).entrySet().stream().filter(e -> e.getKey().startsWith("set") && e
                    .getValue().getParameterCount() == 1).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        } catch (Throwable e) {
            throw new RuntimeException("[" + resultClass + "]failed to initialize setter methods", e);
        }
    }

    private T newResultClassInstance() {
        T obj = null;
        try {
            obj = resultClass.getDeclaredConstructor().newInstance();
        } catch (Throwable e) {
            throw new RuntimeException("[" + resultClass + "]failed to instantiate result class", e);
        }
        return obj;
    }

    private void invokeSetter(Object obj, String alias, Object value) {
        String setterName = getSetterName(alias);
        Method m = setter.get(setterName);
        if (m == null) {
            throw new RuntimeException("[" + resultClass + "][alias=" + alias + ",setter=" + setterName + "]setter not found");
        }
        try {
            m.invoke(obj, value);
        } catch (Throwable e) {
            throw new RuntimeException("[" + resultClass + "][alias=" + alias + ",setter=" + setterName + "]failed to invoke setter method", e);
        }
    }

    private static String getSetterName(String alias) {
        return "set" + alias.substring(0, 1).toUpperCase() + alias.substring(1);
    }

}
