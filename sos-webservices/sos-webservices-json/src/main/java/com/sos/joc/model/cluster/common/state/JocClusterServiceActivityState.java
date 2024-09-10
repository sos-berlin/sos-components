
package com.sos.joc.model.cluster.common.state;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum JocClusterServiceActivityState {

    BUSY("BUSY"),
    RELAX("RELAX");
    private final String value;
    private final static Map<String, JocClusterServiceActivityState> CONSTANTS = new HashMap<String, JocClusterServiceActivityState>();

    static {
        for (JocClusterServiceActivityState c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private JocClusterServiceActivityState(String value) {
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
    public static JocClusterServiceActivityState fromValue(String value) {
        JocClusterServiceActivityState constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
