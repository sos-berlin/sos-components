
package com.sos.joc.model.cluster.common;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ClusterServices {

    cluster("cluster"),
    history("history"),
    dailyplan("dailyplan");
    private final String value;
    private final static Map<String, ClusterServices> CONSTANTS = new HashMap<String, ClusterServices>();

    static {
        for (ClusterServices c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private ClusterServices(String value) {
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
    public static ClusterServices fromValue(String value) {
        ClusterServices constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
