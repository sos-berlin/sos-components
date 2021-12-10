
package com.sos.joc.model.agent;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AgentClusterSchedulingType {

    ROUND_ROBIN("ROUND_ROBIN"),
    FIXED_PRIORITY("FIXED_PRIORITY");
    private final String value;
    private final static Map<String, AgentClusterSchedulingType> CONSTANTS = new HashMap<String, AgentClusterSchedulingType>();

    static {
        for (AgentClusterSchedulingType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private AgentClusterSchedulingType(String value) {
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
    public static AgentClusterSchedulingType fromValue(String value) {
        AgentClusterSchedulingType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
