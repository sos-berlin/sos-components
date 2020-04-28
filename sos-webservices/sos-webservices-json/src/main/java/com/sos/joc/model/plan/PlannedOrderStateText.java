
package com.sos.joc.model.plan;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PlannedOrderStateText {

    PLANNED("PLANNED"),
    SUCCESSFUL("SUCCESSFUL"),
    INCOMPLETE("INCOMPLETE"),
    FAILED("FAILED");
    private final String value;
    private final static Map<String, PlannedOrderStateText> CONSTANTS = new HashMap<String, PlannedOrderStateText>();

    static {
        for (PlannedOrderStateText c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private PlannedOrderStateText(String value) {
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
    public static PlannedOrderStateText fromValue(String value) {
        PlannedOrderStateText constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
