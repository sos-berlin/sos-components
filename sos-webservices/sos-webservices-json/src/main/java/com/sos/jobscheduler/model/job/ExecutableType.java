
package com.sos.jobscheduler.model.job;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ExecutableType {

    EXECUTABLE_SCRIPT("ExecutableScript"),
    EXECUTABLE_PATH("ExecutablePath");
    private final String value;
    private final static Map<String, ExecutableType> CONSTANTS = new HashMap<String, ExecutableType>();

    static {
        for (ExecutableType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private ExecutableType(String value) {
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
    public static ExecutableType fromValue(String value) {
        ExecutableType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
