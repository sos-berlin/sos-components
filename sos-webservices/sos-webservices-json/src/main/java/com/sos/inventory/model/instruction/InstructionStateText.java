
package com.sos.inventory.model.instruction;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum InstructionStateText {

    SKIPPED("SKIPPED"),
    STOPPED("STOPPED"),
    STOPPED_AND_SKIPPED("STOPPED_AND_SKIPPED");
    private final String value;
    private final static Map<String, InstructionStateText> CONSTANTS = new HashMap<String, InstructionStateText>();

    static {
        for (InstructionStateText c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private InstructionStateText(String value) {
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
    public static InstructionStateText fromValue(String value) {
        InstructionStateText constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
