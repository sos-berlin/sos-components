
package com.sos.joc.model.security.properties.fido2;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Fido2ProtocolType {

    FIDO_2("FIDO2"),
    U_2_F("U2F"),
    PASSKEY("PASSKEY");
    private final String value;
    private final static Map<String, Fido2ProtocolType> CONSTANTS = new HashMap<String, Fido2ProtocolType>();

    static {
        for (Fido2ProtocolType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private Fido2ProtocolType(String value) {
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
    public static Fido2ProtocolType fromValue(String value) {
        Fido2ProtocolType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
