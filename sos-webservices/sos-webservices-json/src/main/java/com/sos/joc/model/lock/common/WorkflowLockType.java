
package com.sos.joc.model.lock.common;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum WorkflowLockType {

    EXCLUSIVE("EXCLUSIVE"),
    SHARED("SHARED"),
    UNKNOWN("UNKNOWN");
    private final String value;
    private final static Map<String, WorkflowLockType> CONSTANTS = new HashMap<String, WorkflowLockType>();

    static {
        for (WorkflowLockType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private WorkflowLockType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    @JsonValue
    public String value() {
        return this.value;
    }

    @JsonCreator
    public static WorkflowLockType fromValue(String value) {
        WorkflowLockType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
