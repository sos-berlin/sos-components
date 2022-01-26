
package com.sos.joc.model.security;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum IdentityServiceAuthenticationScheme {

    SINGLE_FACTOR("SINGLE-FACTOR"),
    TWO_FACTOR("TWO-FACTOR");
    private final String value;
    private final static Map<String, IdentityServiceAuthenticationScheme> CONSTANTS = new HashMap<String, IdentityServiceAuthenticationScheme>();

    static {
        for (IdentityServiceAuthenticationScheme c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private IdentityServiceAuthenticationScheme(String value) {
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
    public static IdentityServiceAuthenticationScheme fromValue(String value) {
        IdentityServiceAuthenticationScheme constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
