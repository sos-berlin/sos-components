
package com.sos.joc.model.security.properties.fido2;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Fido2Attachment {

    PLATFORM("PLATFORM"),
    ROAMING("ROAMING"); 
    private final String value;
    private final static Map<String, Fido2Attachment> CONSTANTS = new HashMap<String, Fido2Attachment>();

    static {
        for (Fido2Attachment c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private Fido2Attachment(String value) {
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
    public static Fido2Attachment fromValue(String value) {
        Fido2Attachment constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
