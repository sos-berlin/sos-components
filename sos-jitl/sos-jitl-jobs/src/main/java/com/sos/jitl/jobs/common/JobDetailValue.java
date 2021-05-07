package com.sos.jitl.jobs.common;

public class JobDetailValue {

    private final Object value;
    private final String source;

    protected JobDetailValue(String source, Object value) {
        this.source = source;
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public String getSource() {
        return source;
    }
}
