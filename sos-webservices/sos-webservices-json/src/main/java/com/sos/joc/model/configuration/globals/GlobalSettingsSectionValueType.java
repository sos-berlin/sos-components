
package com.sos.joc.model.configuration.globals;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum GlobalSettingsSectionValueType {

    ZONE("ZONE"),
    AGE("AGE"),
    WEEKDAYS("WEEKDAYS"),
    TIME("TIME"),
    POSITIVENUMBER("POSITIVENUMBER"),
    NONNEGATIVENUMBER("NONNEGATIVENUMBER"),
    STRING("STRING");
    private final String value;
    private final static Map<String, GlobalSettingsSectionValueType> CONSTANTS = new HashMap<String, GlobalSettingsSectionValueType>();

    static {
        for (GlobalSettingsSectionValueType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private GlobalSettingsSectionValueType(String value) {
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
    public static GlobalSettingsSectionValueType fromValue(String value) {
        GlobalSettingsSectionValueType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
