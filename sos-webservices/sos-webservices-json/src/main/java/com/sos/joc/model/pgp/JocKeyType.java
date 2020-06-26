
package com.sos.joc.model.pgp;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum JocKeyType {

    PRIVATE("PRIVATE"),
    PUBLIC("PUBLIC");
    private final String value;
    private final static Map<String, JocKeyType> CONSTANTS = new HashMap<String, JocKeyType>();

    static {
        for (JocKeyType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private JocKeyType(String value) {
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
    public static JocKeyType fromValue(String value) {
        JocKeyType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
