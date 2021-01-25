
package com.sos.controller.model.common;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OutcomeType {

    Succeeded("Succeeded"),
    Failed("Failed"),
    Disrupted("Disrupted"),
    Cancelled("Cancelled");
    private final String value;
    private final static Map<String, OutcomeType> CONSTANTS = new HashMap<String, OutcomeType>();

    static {
        for (OutcomeType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private OutcomeType(String value) {
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
    public static OutcomeType fromValue(String value) {
        OutcomeType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
