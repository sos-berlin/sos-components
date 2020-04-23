
package com.sos.joc.model.common;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum JocSecurityLevel {

    LOW("LOW"),
    MEDIUM("MEDIUM"),
    HIGH("HIGH");
    private final String value;
    private final static Map<String, JocSecurityLevel> CONSTANTS = new HashMap<String, JocSecurityLevel>();

    static {
        for (JocSecurityLevel c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private JocSecurityLevel(String value) {
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
    public static JocSecurityLevel fromValue(String value) {
        JocSecurityLevel constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
