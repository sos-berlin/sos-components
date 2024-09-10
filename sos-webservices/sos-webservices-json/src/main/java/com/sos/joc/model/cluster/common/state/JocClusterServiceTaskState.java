
package com.sos.joc.model.cluster.common.state;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum JocClusterServiceTaskState {

    COMPLETED("COMPLETED"),
    UNCOMPLETED("UNCOMPLETED");
    private final String value;
    private final static Map<String, JocClusterServiceTaskState> CONSTANTS = new HashMap<String, JocClusterServiceTaskState>();

    static {
        for (JocClusterServiceTaskState c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private JocClusterServiceTaskState(String value) {
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
    public static JocClusterServiceTaskState fromValue(String value) {
        JocClusterServiceTaskState constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
