
package com.sos.joc.model.security;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum LdapProtocolItem {

    PLAIN("PLAIN"),
    STARTTLS("STARTTLS"),
    SSL("SSL");
    private final String value;
    private final static Map<String, LdapProtocolItem> CONSTANTS = new HashMap<String, LdapProtocolItem>();

    static {
        for (LdapProtocolItem c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private LdapProtocolItem(String value) {
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
    public static LdapProtocolItem fromValue(String value) {
        LdapProtocolItem constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
