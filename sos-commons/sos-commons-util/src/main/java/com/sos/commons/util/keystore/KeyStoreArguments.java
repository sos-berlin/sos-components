package com.sos.commons.util.keystore;

import java.nio.file.Path;

import com.sos.commons.util.arguments.base.ASOSArguments;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.arguments.base.SOSArgument.DisplayMode;

public class KeyStoreArguments extends ASOSArguments {

    public static final String CLASS_KEY = "KEY_STORE";

    // Java Keystore
    private SOSArgument<KeyStoreType> keyStoreType = new SOSArgument<>("keystore_type", false, KeyStoreType.JKS);
    private SOSArgument<Path> keyStoreFile = new SOSArgument<>("keystore_file", false);
    private SOSArgument<String> keyStorePassword = new SOSArgument<>("keystore_password", false, DisplayMode.MASKED);

    // Java Truststore
    private SOSArgument<KeyStoreType> trustStoreType = new SOSArgument<>("truststore_type", false, KeyStoreType.JKS);
    private SOSArgument<Path> trustStoreFile = new SOSArgument<>("truststore_file", false);
    private SOSArgument<String> trustStorePassword = new SOSArgument<>("truststore_password", false, DisplayMode.MASKED);

    public boolean isEnabled() {
        return isTrustStoreEnabled() || isKeyStoreEnabled();
    }

    public boolean isTrustStoreEnabled() {
        return !trustStoreFile.isEmpty();
    }

    public boolean isKeyStoreEnabled() {
        return !keyStoreFile.isEmpty();
    }

    public SOSArgument<KeyStoreType> getKeyStoreType() {
        return keyStoreType;
    }

    public SOSArgument<Path> getKeyStoreFile() {
        return keyStoreFile;
    }

    public SOSArgument<String> getKeyStorePassword() {
        return keyStorePassword;
    }

    public SOSArgument<KeyStoreType> getTrustStoreType() {
        return trustStoreType;
    }

    public SOSArgument<Path> getTrustStoreFile() {
        return trustStoreFile;
    }

    public SOSArgument<String> getTrustStorePassword() {
        return trustStorePassword;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[keystore ").append(keyStoreType.getValue());
        sb.append(" file=").append(keyStoreFile.getValue()).append("]");
        sb.append("[truststore ").append(trustStoreType.getValue());
        sb.append(" file=").append(trustStoreFile.getValue()).append("]");
        return sb.toString();
    }
}
