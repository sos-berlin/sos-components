package com.sos.jitl.jobs.common;


public class JobResourceValue {
    private final Object value;
    private final String resourceName;

    protected JobResourceValue(String resourceName, Object value) {
        this.resourceName = resourceName;
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public String getResourceName() {
        return resourceName;
    }
}
