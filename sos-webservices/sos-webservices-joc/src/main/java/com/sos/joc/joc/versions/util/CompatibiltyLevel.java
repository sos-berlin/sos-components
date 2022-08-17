package com.sos.joc.joc.versions.util;

import java.util.HashMap;
import java.util.Map;

public enum CompatibiltyLevel {

    COMPATIBLE("COMPATIBLE"),
    PARTIALLY_COMPATIBLE("PARTIALLY_COMPATIBLE"),
    NOT_COMPATIBLE("NOT_COMPATIBLE");
    private final String value;
    private final static Map<String, CompatibiltyLevel> CONSTANTS = new HashMap<String, CompatibiltyLevel>();

    static {
        for (CompatibiltyLevel c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private CompatibiltyLevel(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }

    public static CompatibiltyLevel fromValue(String value) {
        CompatibiltyLevel constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException((value +""));
        } else {
            return constant;
        }
    }

}
