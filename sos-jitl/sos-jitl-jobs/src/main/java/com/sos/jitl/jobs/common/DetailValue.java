package com.sos.jitl.jobs.common;

public class DetailValue {

    private final Object value;
    private final String source;

    protected DetailValue(String source, Object value) {
        this.source = source;
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public String getSource() {
        return source;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("value=").append(value == null ? "" : value);
        sb.append(",source=").append(source == null ? "" : source);
        return sb.toString();
    }
}
