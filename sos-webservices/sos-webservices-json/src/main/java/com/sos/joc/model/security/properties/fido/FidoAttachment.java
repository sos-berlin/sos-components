
package com.sos.joc.model.security.properties.fido;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FidoAttachment {

    PLATFORM("PLATFORM"),
    ROAMING("ROAMING");
    private final String value;
    private final static Map<String, FidoAttachment> CONSTANTS = new HashMap<String, FidoAttachment>();

    static {
        for (FidoAttachment c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private FidoAttachment(String value) {
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
    public static FidoAttachment fromValue(String value) {
        FidoAttachment constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
