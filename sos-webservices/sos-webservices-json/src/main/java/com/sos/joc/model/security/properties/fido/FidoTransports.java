
package com.sos.joc.model.security.properties.fido;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FidoTransports {

    BLE("BLE"),
    HYBRID("HYBRID"),
    INTERNAL("INTERNAL"),
    NFC("NFC"),
    USB("USB");
    private final String value;
    private final static Map<String, FidoTransports> CONSTANTS = new HashMap<String, FidoTransports>();

    static {
        for (FidoTransports c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private FidoTransports(String value) {
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
    public static FidoTransports fromValue(String value) {
        FidoTransports constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
