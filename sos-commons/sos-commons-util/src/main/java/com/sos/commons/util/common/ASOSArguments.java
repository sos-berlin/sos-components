package com.sos.commons.util.common;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.sos.commons.util.SOSReflection;

public abstract class ASOSArguments {

    private List<Field> argumentFields;

    public List<Field> getArgumentFields() {
        if (argumentFields == null) {
            argumentFields = SOSReflection.getAllDeclaredFields(getClass()).stream().filter(f -> f.getType().equals(SOSArgument.class)).collect(
                    Collectors.toList());
        }
        return argumentFields;
    }

    public String getIdentifier() {
        return this.getClass().getName();
    }

    @SuppressWarnings("unchecked")
    // For UnitTest
    public <T> void applyDefaultOnNullValue() throws Exception {
        getArgumentFields();
        for (Field f : argumentFields) {
            f.setAccessible(true);
            SOSArgument<T> current = (SOSArgument<T>) f.get(this);
            if (current.getName() == null) {
                continue;
            }
            if (current.getDefaultValue() != null) {
                if (current.getValue() == null || current.getValue().equals(current.getDefaultValue())) {
                    current.setValue(current.getDefaultValue());
                    f.set(this, current);
                }
            }
        }
    }

    public void setArguments(List<SOSArgument<?>> args) throws Exception {
        getArgumentFields();
        for (Field f : argumentFields) {
            f.setAccessible(true);
            SOSArgument<?> current = (SOSArgument<?>) f.get(this);
            if (current.getName() == null) {// internal usage
                continue;
            }
            SOSArgument<?> extern = find(args, current.getName());
            if (extern != null) {
                current = extern;
            }
            f.set(this, current);
        }
    }

    public List<SOSArgument<?>> getArguments() throws Exception {
        getArgumentFields();
        List<SOSArgument<?>> l = new ArrayList<>();
        for (Field f : argumentFields) {
            f.setAccessible(true);
            SOSArgument<?> current = (SOSArgument<?>) f.get(this);
            if (current.getName() == null) {// internal usage
                continue;
            }
            l.add(current);
        }
        return l;
    }

    private SOSArgument<?> find(List<SOSArgument<?>> args, String name) {
        return args.stream().filter(a -> a.getName() != null && a.getName().equals(name)).findAny().orElse(null);
    }
}
