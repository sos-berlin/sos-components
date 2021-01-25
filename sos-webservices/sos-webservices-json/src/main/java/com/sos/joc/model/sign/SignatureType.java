
package com.sos.joc.model.sign;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SignatureType {

    PGP("PGP"),
    X_509("X509"),
    SILLY("Silly");
    private final String value;
    private final static Map<String, SignatureType> CONSTANTS = new HashMap<String, SignatureType>();

    static {
        for (SignatureType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private SignatureType(String value) {
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
    public static SignatureType fromValue(String value) {
        SignatureType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
