
package com.sos.joc.model.security.identityservice;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum IdentityServiceTypes {

    VAULT("VAULT"),
    VAULT_JOC("VAULT-JOC"),
    VAULT_JOC_ACTIVE("VAULT-JOC-ACTIVE"),
    LDAP("LDAP"),
    LDAP_JOC("LDAP-JOC"),
    JOC("JOC");
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
