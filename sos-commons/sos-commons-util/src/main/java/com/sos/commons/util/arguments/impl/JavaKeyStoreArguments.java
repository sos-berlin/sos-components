package com.sos.commons.util.arguments.impl;

import java.nio.file.Path;

import com.sos.commons.util.arguments.base.ASOSArguments;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.arguments.base.SOSArgument.DisplayMode;

public class JavaKeyStoreArguments extends ASOSArguments {

    public static final String CLASS_KEY = "JAVA_KEY_STORE";

    // Java Keystore
    private SOSArgument<JavaKeyStoreType> keyStoreType = new SOSArgument<>("keystore_type", false, JavaKeyStoreType.JKS);
    private SOSArgument<Path> keyStoreFile = new SOSArgument<>("keystore_file", false);
    private SOSArgument<String> keyStorePassword = new SOSArgument<>("keystore_password", false, DisplayMode.MASKED);

    // Java Truststore
    private SOSArgument<JavaKeyStoreType> trustStoreType = new SOSArgument<>("truststore_type", false, JavaKeyStoreType.JKS);
    private SOSArgument<Path> trustStoreFile = new SOSArgument<>("truststore_file", false);
    private SOSArgument<String> trustStorePassword = new SOSArgument<>("truststore_password", false, DisplayMode.MASKED);

    public SOSArgument<JavaKeyStoreType> getKeyStoreType() {
        return keyStoreType;
    }

    public SOSArgument<Path> getKeyStoreFile() {
        return keyStoreFile;
    }

    public SOSArgument<String> getKeyStorePassword() {
        return keyStorePassword;
    }

    public SOSArgument<JavaKeyStoreType> getTrustStoreType() {
        return trustStoreType;
    }

    public SOSArgument<Path> getTrustStoreFile() {
        return trustStoreFile;
    }

    public SOSArgument<String> getTrustStorePassword() {
        return trustStorePassword;
    }
}
