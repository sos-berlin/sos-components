
package com.sos.joc.model.controller;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ComponentStateText {

    operational("operational"),
    limited("limited"),
    inoperable("inoperable"),
    unknown("unknown");
    private final String value;
    private final static Map<String, ComponentStateText> CONSTANTS = new HashMap<String, ComponentStateText>();

    static {
        for (ComponentStateText c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private ComponentStateText(String value) {
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
    public static ComponentStateText fromValue(String value) {
        ComponentStateText constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
