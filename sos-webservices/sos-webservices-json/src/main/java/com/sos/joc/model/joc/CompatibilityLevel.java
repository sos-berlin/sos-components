
package com.sos.joc.model.joc;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CompatibilityLevel {

    COMPATIBLE("COMPATIBLE"),
    PARTIALLY_COMPATIBLE("PARTIALLY_COMPATIBLE"),
    NOT_COMPATIBLE("NOT_COMPATIBLE");
    private final String value;
    private final static Map<String, CompatibilityLevel> CONSTANTS = new HashMap<String, CompatibilityLevel>();

    static {
        for (CompatibilityLevel c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private CompatibilityLevel(String value) {
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
    public static CompatibilityLevel fromValue(String value) {
        CompatibilityLevel constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
