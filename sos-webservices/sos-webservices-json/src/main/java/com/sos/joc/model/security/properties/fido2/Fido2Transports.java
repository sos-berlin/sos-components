
package com.sos.joc.model.security.properties.fido2;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Fido2Transports {

    BLE("BLE"),
    HYBRID("HYBRID"),
    INTERNAL("INTERNAL"),
    NFC("NFC"),
    USB("USB");
    private final String value;
    private final static Map<String, Fido2Transports> CONSTANTS = new HashMap<String, Fido2Transports>();

    static {
        for (Fido2Transports c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private Fido2Transports(String value) {
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
    public static Fido2Transports fromValue(String value) {
        Fido2Transports constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
