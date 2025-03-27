package com.sos.commons.util.arguments.impl;

import com.sos.commons.util.SOSString;

public enum JavaKeyStoreType {

    JKS, JCEKS, PKCS12, PKCS11, DKS;

    public static JavaKeyStoreType fromString(String type) {
        if (SOSString.isEmpty(type)) {
            return null;
        }
        return JavaKeyStoreType.valueOf(type.trim().toUpperCase());
    }
}
