
package com.sos.jobscheduler.model.cluster;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ClusterType {

    CLUSTER_EMPTY("ClusterEmpty"),
    CLUSTER_SOLE("ClusterSole"),
    CLUSTER_NODES_APPOINTED("ClusterNodesAppointed"),
    CLUSTER_PREPARED_TO_BE_COUPLED("ClusterPreparedToBeCoupled"),
    CLUSTER_COUPLED("ClusterCoupled"),
    CLUSTER_PASSIVE_LOST("ClusterPassiveLost"),
    CLUSTER_SWITCHED_OVER("ClusterSwitchedOver"),
    CLUSTER_FAILED_OVER("ClusterFailedOver");
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
