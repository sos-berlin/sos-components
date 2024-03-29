
package com.sos.joc.model.controller;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Role {

    STANDALONE("STANDALONE"),
    PRIMARY("PRIMARY"),
    BACKUP("BACKUP");
    private final String value;
    private final static Map<String, Role> CONSTANTS = new HashMap<String, Role>();

    static {
        for (Role c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private Role(String value) {
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
    public static Role fromValue(String value) {
        Role constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
