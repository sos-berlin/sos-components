package com.sos.commons.util.arguments.impl;

import java.nio.file.Path;

import com.sos.commons.util.arguments.base.ASOSArguments;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.arguments.base.SOSArgument.DisplayMode;

public class JavaKeyStoreArguments extends ASOSArguments {

    public enum KeyStoreType {
        JKS, JCEKS, PKCS12, PKCS11, DKS
    }

    public static final String CLASS_KEY = "JAVA_KEY_STORE";

    // Java Keystore/Truststore
    private SOSArgument<KeyStoreType> type = new SOSArgument<>("keystore_type", false, KeyStoreType.JKS);
    private SOSArgument<Path> file = new SOSArgument<>("keystore_file", false);
    private SOSArgument<String> password = new SOSArgument<>("keystore_password", false, DisplayMode.MASKED);

    public SOSArgument<KeyStoreType> getType() {
        return type;
    }

    public SOSArgument<Path> getFile() {
        return file;
    }

    public SOSArgument<String> getPassword() {
        return password;
    }

}
