package com.sos.commons.hibernate.common;

public class SOSBatchObject {

    private final int index;
    private final String columnName;
    private final Object value;

    public SOSBatchObject(int index, String columnName, Object value) {
        this.index = index;
        this.columnName = columnName;
        this.value = value;
    }

    public int getIndex() {
        return index;
    }

    public String getColumnName() {
        return columnName;
    }

    public Object getValue() {
        return value;
    }

}
