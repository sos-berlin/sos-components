package com.sos.commons.util.keystore;

import com.sos.commons.util.SOSString;

public enum KeyStoreType {

    JKS, JCEKS, PKCS12, PKCS11, DKS;

    public static KeyStoreType fromString(String type) {
        if (SOSString.isEmpty(type)) {
            return null;
        }
        return KeyStoreType.valueOf(type.trim().toUpperCase());
    }
}
