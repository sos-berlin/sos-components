
package com.sos.joc.model.security.properties.fido2;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Fido2ResidentKey {

    DISCOURAGED("DISCOURAGED"),
    PREFERRED("PREFERRED"),
    REQUIRED("REQUIRED");
    private final String value;
    private final static Map<String, Fido2ResidentKey> CONSTANTS = new HashMap<String, Fido2ResidentKey>();

    static {
        for (Fido2ResidentKey c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private Fido2ResidentKey(String value) {
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
    public static Fido2ResidentKey fromValue(String value) {
        Fido2ResidentKey constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
