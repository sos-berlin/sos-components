
package com.sos.joc.model.agent;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AgentConnectionStateText {

    NODE_LOSS("NODE_LOSS"),
    NOT_DEDICATED("NOT_DEDICATED"),
    WITH_PERMANENT_ERROR("WITH_PERMANENT_ERROR"),
    WITH_TEMPORARY_ERROR("WITH_TEMPORARY_ERROR");
    private final String value;
    private final static Map<String, AgentConnectionStateText> CONSTANTS = new HashMap<String, AgentConnectionStateText>();

    static {
        for (AgentConnectionStateText c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private AgentConnectionStateText(String value) {
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
    public static AgentConnectionStateText fromValue(String value) {
        AgentConnectionStateText constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
