
package com.sos.joc.model.configuration.clusterSettings;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ClusterSettingValueType {

    ZONE("ZONE"),
    AGE("AGE"),
    WEEKDAYS("WEEKDAYS"),
    TIME("TIME"),
    POSITIVENUMBER("POSITIVENUMBER"),
    NONNEGATIVENUMBER("NONNEGATIVENUMBER");
    private final String value;
    private final static Map<String, ClusterSettingValueType> CONSTANTS = new HashMap<String, ClusterSettingValueType>();

    static {
        for (ClusterSettingValueType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private ClusterSettingValueType(String value) {
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
    public static ClusterSettingValueType fromValue(String value) {
        ClusterSettingValueType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
