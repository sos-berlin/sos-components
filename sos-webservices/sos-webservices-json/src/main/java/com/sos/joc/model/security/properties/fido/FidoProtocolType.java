
package com.sos.joc.model.security.properties.fido;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FidoProtocolType {

    FIDO_2("FIDO2"),
    PASSKEY("PASSKEY");
    private final String value;
    private final static Map<String, FidoProtocolType> CONSTANTS = new HashMap<String, FidoProtocolType>();

    static {
        for (FidoProtocolType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private FidoProtocolType(String value) {
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
    public static FidoProtocolType fromValue(String value) {
        FidoProtocolType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
