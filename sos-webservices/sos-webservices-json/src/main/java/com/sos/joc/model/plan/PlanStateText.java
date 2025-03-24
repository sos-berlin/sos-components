
package com.sos.joc.model.plan;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PlanStateText {

    OPEN("OPEN"),
    CLOSED("CLOSED"),
    FINISHED("FINISHED"),
    DELETED("DELETED");
    private final String value;
    private final static Map<String, PlanStateText> CONSTANTS = new HashMap<String, PlanStateText>();

    static {
        for (PlanStateText c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private PlanStateText(String value) {
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
    public static PlanStateText fromValue(String value) {
        PlanStateText constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
