
package com.sos.controller.model.workflow;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum WorkflowStateText {

    IN_SYNC("IN_SYNC"),
    NOT_IN_SYNC("NOT_IN_SYNC"),
    UNKNOWN("UNKNOWN");
    private final String value;
    private final static Map<String, WorkflowStateText> CONSTANTS = new HashMap<String, WorkflowStateText>();

    static {
        for (WorkflowStateText c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private WorkflowStateText(String value) {
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
    public static WorkflowStateText fromValue(String value) {
        WorkflowStateText constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
