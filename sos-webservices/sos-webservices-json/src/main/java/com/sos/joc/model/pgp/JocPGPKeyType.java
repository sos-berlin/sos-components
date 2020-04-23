
package com.sos.joc.model.pgp;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum JocPGPKeyType {

    DEFAULT("DEFAULT"),
    PRIVATE("PRIVATE"),
    PUBLIC("PUBLIC");
    private final String value;
    private final static Map<String, JocPGPKeyType> CONSTANTS = new HashMap<String, JocPGPKeyType>();

    static {
        for (JocPGPKeyType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private JocPGPKeyType(String value) {
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
    public static JocPGPKeyType fromValue(String value) {
        JocPGPKeyType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
