
package com.sos.joc.model.history.order.moved;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MovedSkippedReason {

    Unknown("Unknown"),
    SkippedDueToWorkflowPathControl("SkippedDueToWorkflowPathControl"),
    NoAdmissionPeriodStart("NoAdmissionPeriodStart");
    private final String value;
    private final static Map<String, MovedSkippedReason> CONSTANTS = new HashMap<String, MovedSkippedReason>();

    static {
        for (MovedSkippedReason c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private MovedSkippedReason(String value) {
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
    public static MovedSkippedReason fromValue(String value) {
        MovedSkippedReason constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
