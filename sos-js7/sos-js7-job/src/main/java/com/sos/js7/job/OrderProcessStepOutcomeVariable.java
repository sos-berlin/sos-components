package com.sos.js7.job;

public class OrderProcessStepOutcomeVariable<T> {

    private final String name;
    private T value;

    public OrderProcessStepOutcomeVariable(String name) {
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
