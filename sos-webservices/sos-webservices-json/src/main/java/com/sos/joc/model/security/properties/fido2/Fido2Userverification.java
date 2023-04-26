
package com.sos.joc.model.security.properties.fido2;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Fido2Userverification {

    PREFERRED("PREFERRED"),
    DISCOURAGE("DISCOURAGE"),
    REQUIRED("REQUIRED");
    private final String value;
    private final static Map<String, Fido2Userverification> CONSTANTS = new HashMap<String, Fido2Userverification>();

    static {
        for (Fido2Userverification c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private Fido2Userverification(String value) {
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
    public static Fido2Userverification fromValue(String value) {
        Fido2Userverification constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
