
package com.sos.joc.model.agent;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AgentClusterStateText {

    ALL_SUBAGENTS_ARE_COUPLED_AND_ENABLED("ALL_SUBAGENTS_ARE_COUPLED_AND_ENABLED"),
    ONLY_SOME_SUBAGENTS_ARE_COUPLED_AND_ENABLED("ONLY_SOME_SUBAGENTS_ARE_COUPLED_AND_ENABLED"),
    NO_SUBAGENTS_ARE_COUPLED_AND_ENABLED("NO_SUBAGENTS_ARE_COUPLED_AND_ENABLED"),
    UNKNOWN("UNKNOWN");
    private final String value;
    private final static Map<String, AgentClusterStateText> CONSTANTS = new HashMap<String, AgentClusterStateText>();

    static {
        for (AgentClusterStateText c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private AgentClusterStateText(String value) {
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
    public static AgentClusterStateText fromValue(String value) {
        AgentClusterStateText constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
