
package com.sos.joc.model.configuration.clusterSettings;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ClusterSettingsSectionValueType {

    ZONE("ZONE"),
    AGE("AGE"),
    WEEKDAYS("WEEKDAYS"),
    TIME("TIME"),
    POSITIVENUMBER("POSITIVENUMBER"),
    NONNEGATIVENUMBER("NONNEGATIVENUMBER"),
    STRING("STRING");
    private final String value;
    private final static Map<String, ClusterSettingsSectionValueType> CONSTANTS = new HashMap<String, ClusterSettingsSectionValueType>();

    static {
        for (ClusterSettingsSectionValueType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private ClusterSettingsSectionValueType(String value) {
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
    public static ClusterSettingsSectionValueType fromValue(String value) {
        ClusterSettingsSectionValueType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
