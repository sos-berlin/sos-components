package com.sos.commons.hibernate.transform;

import java.lang.reflect.Field;

import org.hibernate.query.TupleTransformer;

public class SOSAliasToBeanResultTransformer<T> implements TupleTransformer<T> {

    private final Class<T> resultClass;

    public SOSAliasToBeanResultTransformer(Class<T> resultClass) {
        this.resultClass = resultClass;
    }

    @Override
    public T transformTuple(Object[] tuple, String[] aliases) {
        if (tuple.length != aliases.length) {
            throw new IllegalArgumentException("Length of aliases array must match length of tuple array");
        }
        try {
            T obj = resultClass.getDeclaredConstructor().newInstance();
            for (int i = 0; i < aliases.length; i++) {
                Field field = getFieldByName(resultClass, aliases[i]);
                if (field != null) {
                    field.setAccessible(true);
                    field.set(obj, tuple[i]);
                }
            }
            return obj;
        } catch (Throwable e) {
            throw new RuntimeException("Failed to instantiate result class", e);
        }
    }

    private Field getFieldByName(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            if (clazz.getSuperclass() != null) {
                return getFieldByName(clazz.getSuperclass(), fieldName);
            }
            return null;
        }
    }

}
