
package com.sos.joc.model.jobscheduler;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ClusterStateText {

    CLUSTER_EMPTY("ClusterEmpty"),
    CLUSTER_SOLE("ClusterSole"),
    CLUSTER_NODES_APPOINTED("ClusterNodesAppointed"),
    CLUSTER_PREPARED_TO_BE_COUPLED("ClusterPreparedToBeCoupled"),
    CLUSTER_COUPLED("ClusterCoupled"),
    CLUSTER_PASSIVE_LOST("ClusterPassiveLost"),
    CLUSTER_SWITCHED_OVER("ClusterSwitchedOver"),
    CLUSTER_FAILED_OVER("ClusterFailedOver"),
    CLUSTER_UNKNOWN("ClusterUnknown");
    private final String value;
    private final static Map<String, ClusterStateText> CONSTANTS = new HashMap<String, ClusterStateText>();

    static {
        for (ClusterStateText c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private ClusterStateText(String value) {
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
    public static ClusterStateText fromValue(String value) {
        ClusterStateText constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
