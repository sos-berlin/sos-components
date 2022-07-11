
package com.sos.joc.model.agent;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AgentStateReason {

    FRESH("FRESH"),
    RESTARTED("RESTARTED"),
    RESET("RESET");
    private final String value;
    private final static Map<String, AgentStateReason> CONSTANTS = new HashMap<String, AgentStateReason>();

    static {
        for (AgentStateReason c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private AgentStateReason(String value) {
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
    public static AgentStateReason fromValue(String value) {
        AgentStateReason constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
