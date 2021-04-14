package com.sos.commons.sign.keys.keyStore;

import java.util.HashMap;
import java.util.Map;

public enum KeyStoreType {

	PKCS12("PKCS12"),
	JKS("JKS");
	
    private final String value;
    private final static Map<String, KeyStoreType> CONSTANTS = new HashMap<String, KeyStoreType>();

    static {
        for (KeyStoreType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private KeyStoreType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public String value() {
        return this.value;
    }

    public static KeyStoreType fromValue(String value) {
    	KeyStoreType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
