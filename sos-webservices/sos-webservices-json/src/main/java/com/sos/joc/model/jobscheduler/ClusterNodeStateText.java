
package com.sos.joc.model.jobscheduler;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ClusterNodeStateText {

    active("active"),
    inactive("inactive"),
    unknown("unknown");
    private final String value;
    private final static Map<String, ClusterNodeStateText> CONSTANTS = new HashMap<String, ClusterNodeStateText>();

    static {
        for (ClusterNodeStateText c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private ClusterNodeStateText(String value) {
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
    public static ClusterNodeStateText fromValue(String value) {
        ClusterNodeStateText constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
