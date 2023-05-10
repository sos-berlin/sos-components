
package com.sos.joc.model.security.fido2;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CipherTypes {

    RSA_ECB_OAEP_WITH_SHA_1_AND_MGF_1_PADDING("RSA/ECB/OAEPWithSHA-1AndMGF1Padding"),
    RSA_ECB_PKCS_1_PADDING("RSA/ECB/PKCS1Padding"),
    RSA("RSA");
    private final String value;
    private final static Map<String, CipherTypes> CONSTANTS = new HashMap<String, CipherTypes>();

    static {
        for (CipherTypes c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private CipherTypes(String value) {
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
    public static CipherTypes fromValue(String value) {
        CipherTypes constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
