package com.sos.jitl.jobs.common;

public class JobOutcomeVariable<T> {

    private final String name;
    private T value;

    public JobOutcomeVariable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setValue(T val) {
        value = val;
    }

    public T getValue() {
        return value;
    }
}
