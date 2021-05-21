package com.sos.commons.util.common;

import java.lang.reflect.Field;
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

    @SuppressWarnings("rawtypes")
    public void setArguments(List<SOSArgument> args) throws IllegalArgumentException, IllegalAccessException {
        getArgumentFields();
        for (Field f : argumentFields) {
            f.setAccessible(true);
            SOSArgument current = (SOSArgument) f.get(this);
            if (current.getName() == null) {// internal usage
                continue;
            }
            SOSArgument extern = find(args, current.getName());
            if (extern != null) {
                current = extern;
            }
            f.set(this, current);
        }
    }

    @SuppressWarnings("rawtypes")
    private SOSArgument find(List<SOSArgument> args, String name) {
        return args.stream().filter(a -> a.getName() != null && a.getName().equals(name)).findAny().orElse(null);
    }
}
