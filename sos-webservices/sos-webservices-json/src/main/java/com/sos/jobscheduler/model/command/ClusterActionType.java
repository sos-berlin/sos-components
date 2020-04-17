
package com.sos.jobscheduler.model.command;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ClusterActionType {

    SWITCHOVER("Switchover"),
    FAILOVER("Failover");
    private final String value;
    private final static Map<String, ClusterActionType> CONSTANTS = new HashMap<String, ClusterActionType>();

    static {
        for (ClusterActionType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private ClusterActionType(String value) {
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
    public static ClusterActionType fromValue(String value) {
        ClusterActionType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
