package com.sos.commons.sign.keys.keyStore;

import java.util.HashMap;
import java.util.Map;

public enum KeystoreType {

	PKCS12("PKCS12"),
	JKS("JKS");
	
    private final String value;
    private final static Map<String, KeystoreType> CONSTANTS = new HashMap<String, KeystoreType>();

    static {
        for (KeystoreType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private KeystoreType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public String value() {
        return this.value;
    }

    public static KeystoreType fromValue(String value) {
    	KeystoreType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
