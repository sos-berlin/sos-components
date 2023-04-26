
package com.sos.joc.model.security.properties.fido2;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Fido2Attestation {

    DIRECT("DIRECT"),
    ENTERPRISE("ENTERPRISE"),
    INDIRECT("INDIRECT"),
    NONE("NONE");
    private final String value;
    private final static Map<String, Fido2Attestation> CONSTANTS = new HashMap<String, Fido2Attestation>();

    static {
        for (Fido2Attestation c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private Fido2Attestation(String value) {
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
    public static Fido2Attestation fromValue(String value) {
        Fido2Attestation constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
