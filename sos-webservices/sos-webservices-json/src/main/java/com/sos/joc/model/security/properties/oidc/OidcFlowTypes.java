
package com.sos.joc.model.security.properties.oidc;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OidcFlowTypes {

    AUTHENTICATION("AUTHENTICATION"),
    IMPLICITE("IMPLICITE"),
    CLIENT_CREDENTIAL("CLIENT-CREDENTIAL");
    private final String value;
    private final static Map<String, OidcFlowTypes> CONSTANTS = new HashMap<String, OidcFlowTypes>();

    static {
        for (OidcFlowTypes c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private OidcFlowTypes(String value) {
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
    public static OidcFlowTypes fromValue(String value) {
        OidcFlowTypes constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
