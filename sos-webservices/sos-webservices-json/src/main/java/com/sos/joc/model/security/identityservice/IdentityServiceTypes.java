
package com.sos.joc.model.security.identityservice;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum IdentityServiceTypes {

    UNKNOWN("UNKNOWN"),
    KEYCLOAK("KEYCLOAK"),
    KEYCLOAK_JOC("KEYCLOAK-JOC"),
    LDAP("LDAP"),
    LDAP_JOC("LDAP-JOC"),
    OIDC("OIDC"),
    OIDC_JOC("OIDC-JOC"),
    FIDO("FIDO"),
    JOC("JOC"),
    CERTIFICATE("CERTIFICATE");
    private final String value;
    private final static Map<String, IdentityServiceTypes> CONSTANTS = new HashMap<String, IdentityServiceTypes>();

    static {
        for (IdentityServiceTypes c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private IdentityServiceTypes(String value) {
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
    public static IdentityServiceTypes fromValue(String value) {
        IdentityServiceTypes constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
