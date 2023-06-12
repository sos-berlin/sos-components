
package com.sos.joc.model.security.properties.fido;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FidoResidentKey {

    DISCOURAGED("DISCOURAGED"),
    PREFERRED("PREFERRED"),
    REQUIRED("REQUIRED");
    private final String value;
    private final static Map<String, FidoResidentKey> CONSTANTS = new HashMap<String, FidoResidentKey>();

    static {
        for (FidoResidentKey c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private FidoResidentKey(String value) {
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
    public static FidoResidentKey fromValue(String value) {
        FidoResidentKey constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
