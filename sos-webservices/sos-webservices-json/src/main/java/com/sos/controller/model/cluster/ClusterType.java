
package com.sos.controller.model.cluster;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ClusterType {

    EMPTY("Empty"),
    NODES_APPOINTED("NodesAppointed"),
    PREPARED_TO_BE_COUPLED("PreparedToBeCoupled"),
    COUPLED("Coupled"),
    PASSIVE_LOST("PassiveLost"),
    FAILED_OVER("FailedOver"),
    SWITCHED_OVER("SwitchedOver"),
    ACTIVE_NODE_IS_NOT_READY("ActiveNodeIsNotReady"),
    ACTIVE_SHUT_DOWN("ActiveShutDown");
    private final String value;
    private final static Map<String, ClusterType> CONSTANTS = new HashMap<String, ClusterType>();

    static {
        for (ClusterType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private ClusterType(String value) {
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
    public static ClusterType fromValue(String value) {
        ClusterType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
