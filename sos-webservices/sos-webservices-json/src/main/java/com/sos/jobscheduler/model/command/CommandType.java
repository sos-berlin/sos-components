
package com.sos.jobscheduler.model.command;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CommandType {

    BATCH("Batch"),
    CANCEL_ORDER("CancelOrder"),
    TERMINATE("Terminate"),
    EMERGENCY_STOP("EmergencyStop"),
    REPLACE_REPO("ReplaceRepo"),
    UPDATE_REPO("UpdateRepo");
    private final String value;
    private final static Map<String, CommandType> CONSTANTS = new HashMap<String, CommandType>();

    static {
        for (CommandType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private CommandType(String value) {
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
    public static CommandType fromValue(String value) {
        CommandType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
