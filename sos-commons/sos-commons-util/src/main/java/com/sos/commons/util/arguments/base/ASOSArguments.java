package com.sos.commons.util.arguments.base;

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
    public <T> void applyDefaultIfNull() throws Exception {
        getArgumentFields();
        for (Field f : argumentFields) {
            f.setAccessible(true);
            SOSArgument<T> current = (SOSArgument<T>) f.get(this);
            if (current.getName() == null) {
                continue;
            }
            if (current.getDefaultValue() != null && current.isEmpty()) {
                current.setValue(current.getDefaultValue());
                f.set(this, current);
            }
        }
    }

    public void applyDefaultIfNullQuietly() {
        try {
            applyDefaultIfNull();
        } catch (Exception e) {
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

    /** Included Argument classes:<br/>
     * If an Argument class has not only Argument fields, but fields that are themselves an instance of ASOSArguments<br/>
     * Examples: {@link SSLArguments#getJavaKeyStore()}
     * 
     * @return
     * @throws Exception */
    public List<ASOSArguments> getIncludedArgumentsIfNotNull() throws Exception {
        getArgumentFields();
        List<ASOSArguments> l = new ArrayList<>();

        List<Field> included = SOSReflection.getAllDeclaredFields(getClass()).stream().filter(f -> ASOSArguments.class.isAssignableFrom(f.getType()))
                .collect(Collectors.toList());
        for (Field f : included) {
            f.setAccessible(true);
            Object include = f.get(this);
            if (include != null) {
                l.add((ASOSArguments) include);
            }
        }
        return l;
    }

    private SOSArgument<?> find(List<SOSArgument<?>> args, String name) {
        return args.stream().filter(a -> a.getName() != null && a.getName().equals(name)).findAny().orElse(null);
    }
}
