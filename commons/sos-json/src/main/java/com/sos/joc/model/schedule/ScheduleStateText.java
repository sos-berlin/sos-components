
package com.sos.joc.model.schedule;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ScheduleStateText {

    ACTIVE("ACTIVE"),
    INACTIVE("INACTIVE"),
    UNKNOWN("UNKNOWN");
    private final String value;
    private final static Map<String, ScheduleStateText> CONSTANTS = new HashMap<String, ScheduleStateText>();

    static {
        for (ScheduleStateText c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private ScheduleStateText(String value) {
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
    public static ScheduleStateText fromValue(String value) {
        ScheduleStateText constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
