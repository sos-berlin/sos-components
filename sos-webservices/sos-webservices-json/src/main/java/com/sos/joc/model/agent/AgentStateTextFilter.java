
package com.sos.joc.model.agent;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AgentStateTextFilter {

    COUPLED("COUPLED"),
    RESETTING("RESETTING"),
    INITIALISED("INITIALISED"),
    COUPLINGFAILED("COUPLINGFAILED"),
    SHUTDOWN("SHUTDOWN"),
    UNKNOWN("UNKNOWN");
    private final String value;
    private final static Map<String, AgentStateTextFilter> CONSTANTS = new HashMap<String, AgentStateTextFilter>();

    static {
        for (AgentStateTextFilter c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private AgentStateTextFilter(String value) {
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
    public static AgentStateTextFilter fromValue(String value) {
        return CONSTANTS.get(value);
//        AgentStateTextFilter constant = CONSTANTS.get(value);
//        if (constant == null) {
//            throw new IllegalArgumentException(value);
//        } else {
//            return constant;
//        }
    }

}
