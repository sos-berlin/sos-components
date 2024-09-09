
package com.sos.joc.model.cluster.common.state;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum JocClusterServiceState {

    UNKNOWN("UNKNOWN"),
    BUSY("BUSY"),
    RELAX("RELAX");
    private final String value;
    private final static Map<String, JocClusterServiceState> CONSTANTS = new HashMap<String, JocClusterServiceState>();

    static {
        for (JocClusterServiceState c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private JocClusterServiceState(String value) {
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
    public static JocClusterServiceState fromValue(String value) {
        JocClusterServiceState constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
