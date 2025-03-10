package com.sos.commons.util.arguments.base;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class ASOSArgument<T> {

    /** value type */
    private java.lang.reflect.Type clazzType;

    public ASOSArgument() {
        init();
    }

    // Argument can be initialized with wildcard SOSArgument<?>... - so the type cannot be evaluated
    private void init() {
        try {
            Type superClass = getClass().getGenericSuperclass();
            if (superClass instanceof ParameterizedType) {
                // SOSArgument<String> arg = new SOSArgument<>(...);
                this.clazzType = ((ParameterizedType) superClass).getActualTypeArguments()[0];
            }
        } catch (Exception e) {
        }
        // SOSArgument<?> arg = new SOSArgument<>(...);
        if (clazzType == null) {
            // this.clazzType = String.class;
        }
    }

    // redefine value type it it can't be evaluated in constructor
    public void setClazzType(Type val) {
        clazzType = val;
    }

    public void setClazzType(Object value) {
        if (value == null) {
            return;
        }
        clazzType = value.getClass();
        Type superClass = value.getClass().getGenericSuperclass();
        if (superClass instanceof ParameterizedType) {
            clazzType = superClass;
        }
    }

    public Type getClazzType() {
        return clazzType;
    }
}
