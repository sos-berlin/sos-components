
package com.sos.joc.model.security.oidc;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TokenEndpointMethods {

    CLIENT_SECRET_POST("client_secret_post"),
    CLIENT_SECRET_BASIC("client_secret_basic"),
    CLIENT_SECRET_JWT("client_secret_jwt"),
    PRIVATE_KEY_JWT("private_key_jwt"),
    NONE("none");
    private final String value;
    private final static Map<String, TokenEndpointMethods> CONSTANTS = new HashMap<String, TokenEndpointMethods>();

    static {
        for (TokenEndpointMethods c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private TokenEndpointMethods(String value) {
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
    public static TokenEndpointMethods fromValue(String value) {
        TokenEndpointMethods constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
