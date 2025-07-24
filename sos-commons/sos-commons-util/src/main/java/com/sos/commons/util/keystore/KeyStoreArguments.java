package com.sos.commons.util.keystore;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.arguments.base.ASOSArguments;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.commons.util.arguments.base.SOSArgument.DisplayMode;

public class KeyStoreArguments extends ASOSArguments {

    public static final String CLASS_KEY = "KEY_STORE";

    private static final String ALIASES_DELIMITER = ";";

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

    // internal usage - sets KeyStoreContainer - can be based on a Path or directly contain a KeyStore
    private SOSArgument<KeyStoreContainer> keyStoreContainer = new SOSArgument<>(null, false);
    // internal usage - support multiple TrustStores
    private SOSArgument<List<KeyStoreContainer>> trustStoreContainers = new SOSArgument<>(null, false);

    public boolean isCustomStoresEnabled() {
        return isCustomTrustStoreEnabled() || isCustomKeyStoreEnabled();
    }

    public boolean isCustomTrustStoreEnabled() {
        return !trustStoreFile.isEmpty() || !trustStoreContainers.isEmpty();
    }

    public boolean isCustomKeyStoreEnabled() {
        return !keyStoreFile.isEmpty() || !keyStoreContainer.isEmpty();
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

    public void setKeyStoreContainer(KeyStoreContainer c) {
        keyStoreContainer.setValue(c);
        mapContainerToSingleKeyStoreFileArgument(keyStoreContainer.getValue());
    }

    public SOSArgument<KeyStoreContainer> getKeyStoreContainer() {
        if (isCustomKeyStoreEnabled()) {
            if (keyStoreContainer.getValue() == null) {
                keyStoreContainer.setValue(mapSingleKeyStoreFileArgumentToContainer());
            }
        }
        return keyStoreContainer;
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

    public void setTrustStoreContainers(List<KeyStoreContainer> val) {
        trustStoreContainers.setValue(val);
    }

    public SOSArgument<List<KeyStoreContainer>> getTrustStoreContainers() {
        if (isCustomTrustStoreEnabled()) {
            if (trustStoreContainers.getValue() == null) {
                KeyStoreContainer c = mapSingleTrustStoreFileArgumentToContainer();
                if (c != null) {
                    trustStoreContainers.setValue(new ArrayList<>());
                    trustStoreContainers.getValue().add(c);
                }
            }
        }
        return trustStoreContainers;
    }

    public void addTrustStoreContainer(KeyStoreContainer c) {
        if (c == null) {
            return;
        }
        if (getTrustStoreContainers().getValue() == null) {
            trustStoreContainers.setValue(new ArrayList<>());
        }

        if (c.getPath() == null) {
            if (c.getKeyStore() == null) {
                return;
            }
            trustStoreContainers.getValue().add(c);
        } else {
            boolean exists = trustStoreContainers.getValue().stream().anyMatch(e -> c.getPath().equals(e.getPath()));
            if (!exists) {
                trustStoreContainers.getValue().add(c);
            }
        }
    }

    private void mapContainerToSingleKeyStoreFileArgument(KeyStoreContainer c) {
        if (c == null || c.getPath() == null) {
            keyStoreFile.setValue(null);
            return;
        }

        keyStoreFile.setValue(c.getPath());
        keyStoreType.setValue(c.getType());
        keyStorePassword.setValue(c.getPassword());
        keyStoreKeyPassword.setValue(c.getKeyPassword());
        keyStoreAlias.setValue(SOSCollection.isEmpty(c.getAliases()) ? null : String.join(ALIASES_DELIMITER, c.getAliases()));
    }

    private KeyStoreContainer mapSingleKeyStoreFileArgumentToContainer() {
        if (keyStoreFile.isEmpty()) {
            return null;
        }

        KeyStoreContainer c = new KeyStoreContainer(keyStoreType.getValue(), keyStoreFile.getValue());
        c.setPassword(keyStorePassword.getValue());
        c.setAliases(aliasesToList(keyStoreAlias.getValue()));
        return c;
    }

    private List<String> aliasesToList(String input) {
        if (SOSString.isEmpty(input)) {
            return null;
        }
        return Arrays.stream(input.split(ALIASES_DELIMITER)).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
    }

    private KeyStoreContainer mapSingleTrustStoreFileArgumentToContainer() {
        if (trustStoreFile.isEmpty()) {
            return null;
        }

        KeyStoreContainer c = new KeyStoreContainer(trustStoreType.getValue(), trustStoreFile.getValue());
        c.setPassword(trustStorePassword.getValue());
        return c;
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
