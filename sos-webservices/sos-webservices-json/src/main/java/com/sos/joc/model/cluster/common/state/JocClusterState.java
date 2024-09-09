
package com.sos.joc.model.cluster.common.state;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum JocClusterState {

    STARTED("STARTED"),
    RUNNING("RUNNING"),
    STOPPED("STOPPED"),
    RESTARTED("RESTARTED"),
    ALREADY_STARTED("ALREADY_STARTED"),
    ALREADY_RUNNING("ALREADY_RUNNING"),
    ALREADY_STOPPED("ALREADY_STOPPED"),
    SWITCH("SWITCH"),
    MISSING_CONFIGURATION("MISSING_CONFIGURATION"),
    MISSING_HANDLERS("MISSING_HANDLERS"),
    MISSING_LICENSE("MISSING_LICENSE"),
    ERROR("ERROR"),
    COMPLETED("COMPLETED"),
    UNCOMPLETED("UNCOMPLETED");
    private final String value;
    private final static Map<String, JocClusterState> CONSTANTS = new HashMap<String, JocClusterState>();

    static {
        for (JocClusterState c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private JocClusterState(String value) {
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
    public static JocClusterState fromValue(String value) {
        JocClusterState constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
