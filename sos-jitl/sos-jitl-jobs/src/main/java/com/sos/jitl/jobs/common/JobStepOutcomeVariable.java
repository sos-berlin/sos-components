package com.sos.jitl.jobs.common;

public class JobStepOutcomeVariable<T> {

    private final String name;
    private T value;

    public JobStepOutcomeVariable(String name) {
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
