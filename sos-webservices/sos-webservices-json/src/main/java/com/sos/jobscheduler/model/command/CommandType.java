
package com.sos.jobscheduler.model.command;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CommandType {

    TERMINATE("Terminate"),
    EMERGENCY_STOP("EmergencyStop"),
    READ_CONFIGURATION_DIRECTORY("ReadConfigurationDirectory");
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
