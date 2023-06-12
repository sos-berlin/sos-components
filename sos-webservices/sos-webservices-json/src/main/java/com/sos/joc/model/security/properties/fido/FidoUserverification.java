
package com.sos.joc.model.security.properties.fido;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FidoUserverification {

    DISCOURAGED("DISCOURAGED"),
    PREFERRED("PREFERRED"),
    REQUIRED("REQUIRED");
    private final String value;
    private final static Map<String, FidoUserverification> CONSTANTS = new HashMap<String, FidoUserverification>();

    static {
        for (FidoUserverification c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private FidoUserverification(String value) {
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
    public static FidoUserverification fromValue(String value) {
        FidoUserverification constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
