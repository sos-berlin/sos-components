
package com.sos.joc.model.cluster.common;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ClusterHandlerIdentifier {

    cluster("cluster"),
    history("history"),
    dailyplan("dailyplan");
    private final String value;
    private final static Map<String, ClusterHandlerIdentifier> CONSTANTS = new HashMap<String, ClusterHandlerIdentifier>();

    static {
        for (ClusterHandlerIdentifier c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private ClusterHandlerIdentifier(String value) {
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
    public static ClusterHandlerIdentifier fromValue(String value) {
        ClusterHandlerIdentifier constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
