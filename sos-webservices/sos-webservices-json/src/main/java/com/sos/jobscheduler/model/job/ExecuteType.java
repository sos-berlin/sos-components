
package com.sos.jobscheduler.model.job;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ExecuteType {

    EXECUTABLE_SCRIPT("ExecutableScript"),
    EXECUTABLE_PATH("ExecutablePath");
    private final String value;
    private final static Map<String, ExecuteType> CONSTANTS = new HashMap<String, ExecuteType>();

    static {
        for (ExecuteType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private ExecuteType(String value) {
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
    public static ExecuteType fromValue(String value) {
        ExecuteType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
