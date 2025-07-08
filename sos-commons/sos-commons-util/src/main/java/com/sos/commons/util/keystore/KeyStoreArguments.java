package com.sos.commons.util.keystore;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.arguments.base.ASOSArguments;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.arguments.base.SOSArgument.DisplayMode;

public class KeyStoreArguments extends ASOSArguments {

    public static final String CLASS_KEY = "KEY_STORE";

    // Java KeyStore
    private SOSArgument<KeyStoreType> keyStoreType = new SOSArgument<>("keystore_type", false, KeyStoreType.JKS);
    private SOSArgument<Path> keyStoreFile = new SOSArgument<>("keystore_file", false);
    private SOSArgument<String> keyStorePassword = new SOSArgument<>("keystore_password", false, DisplayMode.MASKED);
    private SOSArgument<String> keyStoreKeyPassword = new SOSArgument<>("keystore_key_password", false, DisplayMode.MASKED);
    private SOSArgument<String> keyStoreAlias = new SOSArgument<>("keystore_alias", false);

    // Java TrustStore (default: 1 TrustStore)
    private SOSArgument<KeyStoreType> trustStoreType = new SOSArgument<>("truststore_type", false, KeyStoreType.JKS);
    private SOSArgument<Path> trustStoreFile = new SOSArgument<>("truststore_file", false);
    private SOSArgument<String> trustStorePassword = new SOSArgument<>("truststore_password", false, DisplayMode.MASKED);

    // internal usage - sets KeyStoreFile
    private SOSArgument<KeyStoreFile> keyStoreFileObject = new SOSArgument<>(null, false);
    // internal usage - support multiple TrustStores
    private SOSArgument<List<KeyStoreFile>> trustStoreFiles = new SOSArgument<>(null, false);

    public boolean isCustomStoresEnabled() {
        return isCustomTrustStoreEnabled() || isCustomKeyStoreEnabled();
    }

    public boolean isCustomTrustStoreEnabled() {
        return !trustStoreFile.isEmpty() || !trustStoreFiles.isEmpty();
    }

    public boolean isCustomKeyStoreEnabled() {
        return !keyStoreFile.isEmpty() || !keyStoreFileObject.isEmpty();
    }

    public SOSArgument<KeyStoreType> getKeyStoreType() {
        return keyStoreType;
    }

    public SOSArgument<Path> getKeyStoreFile() {
        return keyStoreFile;
    }

    public SOSArgument<String> getKeyStoreAlias() {
        return keyStoreAlias;
    }

    public SOSArgument<String> getKeyStorePassword() {
        return keyStorePassword;
    }

    public SOSArgument<String> getKeyStoreKeyPassword() {
        return keyStoreKeyPassword;
    }

    public void setKeyStoreFile(KeyStoreFile f) {
        keyStoreFileObject.setValue(f);
        mapToSingleKeyStoreProperties(keyStoreFileObject.getValue());
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

    public void setTrustStoreFiles(List<KeyStoreFile> val) {
        trustStoreFiles.setValue(val);
    }

    public SOSArgument<List<KeyStoreFile>> getTrustStoreFiles() {
        if (isCustomTrustStoreEnabled()) {
            if (trustStoreFiles.getValue() == null) {
                KeyStoreFile f = mapSingleTrustStoreProperties();
                if (f != null) {
                    trustStoreFiles.setValue(new ArrayList<>());
                    trustStoreFiles.getValue().add(f);
                }
            }
        }
        return trustStoreFiles;
    }

    public void addTrustStoreFile(KeyStoreFile f) {
        if (f == null || f.getPath() == null) {
            return;
        }
        if (getTrustStoreFiles().getValue() == null) {
            trustStoreFiles.setValue(new ArrayList<>());
        }

        boolean exists = trustStoreFiles.getValue().stream().anyMatch(e -> f.getPath().equals(e.getPath()));
        if (!exists) {
            trustStoreFiles.getValue().add(f);
        }
    }

    private void mapToSingleKeyStoreProperties(KeyStoreFile f) {
        if (f == null) {
            keyStoreFile.setValue(null);
            return;
        }

        keyStoreFile.setValue(f.getPath());
        keyStoreType.setValue(f.getType());
        keyStorePassword.setValue(f.getPassword());
        keyStoreKeyPassword.setValue(f.getKeyPassword());
        keyStoreAlias.setValue(SOSCollection.isEmpty(f.getAliases()) ? null : String.join(";", f.getAliases()));
    }

    private KeyStoreFile mapSingleTrustStoreProperties() {
        if (trustStoreFile.isEmpty()) {
            return null;
        }

        KeyStoreFile f = new KeyStoreFile();
        f.setType(trustStoreType.getValue());
        f.setPath(trustStoreFile.getValue());
        f.setPassword(trustStorePassword.getValue());
        return f;
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
