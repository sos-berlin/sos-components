
package com.sos.joc.model.agent;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AgentStateText {

    COUPLED("COUPLED"),
    DECOUPLED("DECOUPLED"),
    COUPLINGFAILED("COUPLINGFAILED"),
    UNKNOWN("UNKNOWN");
    private final String value;
    private final static Map<String, AgentStateText> CONSTANTS = new HashMap<String, AgentStateText>();

    static {
        for (AgentStateText c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private AgentStateText(String value) {
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
    public static AgentStateText fromValue(String value) {
        AgentStateText constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
