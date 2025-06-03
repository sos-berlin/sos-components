
package com.sos.joc.model.security.foureyes;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EmailPriority {

    HIGHEST("HIGHEST"),
    HIGH("HIGH"),
    NORMAL("NORMAL"),
    LOW("LOW"),
    LOWEST("LOWEST");
    private final String value;
    private final static Map<String, EmailPriority> CONSTANTS = new HashMap<String, EmailPriority>();

    static {
        for (EmailPriority c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private EmailPriority(String value) {
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
    public static EmailPriority fromValue(String value) {
        EmailPriority constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
