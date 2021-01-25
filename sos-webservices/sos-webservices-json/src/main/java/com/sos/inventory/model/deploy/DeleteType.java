
package com.sos.inventory.model.deploy;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DeleteType {

    WORKFLOW_PATH("WorkflowPath"),
    LOCK_ID("LockId"),
    JOB_CLASS_PATH("JobClassPath"),
    JUNCTION_PATH("JunctionPath");
    private final String value;
    private final static Map<String, DeleteType> CONSTANTS = new HashMap<String, DeleteType>();

    static {
        for (DeleteType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private DeleteType(String value) {
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
    public static DeleteType fromValue(String value) {
        DeleteType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
