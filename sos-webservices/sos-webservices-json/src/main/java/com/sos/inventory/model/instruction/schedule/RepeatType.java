
package com.sos.inventory.model.instruction.schedule;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RepeatType {

    TICKING("Ticking"),
    CONTINUOUS("Continuous"),
    PERIODIC("Periodic");
    private final String value;
    private final static Map<String, RepeatType> CONSTANTS = new HashMap<String, RepeatType>();

    static {
        for (RepeatType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private RepeatType(String value) {
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
    public static RepeatType fromValue(String value) {
        RepeatType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
