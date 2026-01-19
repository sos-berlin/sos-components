package com.sos.commons.util.keystore;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.loggers.base.ISOSLogger;

public class KeyStoreReader {

    public enum Type {
        KEYSTORE, TRUSTSTORE, KEY_AND_TRUSTSTORE
    }

    private static final String SYSTEM_PROPERTY_KEYSTORE_PATH = "javax.net.ssl.keyStore";
    private static final String SYSTEM_PROPERTY_KEYSTORE_PASSWORD = "javax.net.ssl.keyStorePassword";

    private static final String SYSTEM_PROPERTY_TRUSTSTORE_PATH = "javax.net.ssl.trustStore";
    private static final String SYSTEM_PROPERTY_TRUSTSTORE_PASSWORD = "javax.net.ssl.trustStorePassword";

    private final Type type;
    private Path path;
    private String password;
    /** KeyStore.getInstance: JKS/PKC12... */
    private KeyStoreType storeType;

    private KeyStoreReader() {
        this(null, null, null, null);
    }

    private KeyStoreReader(Type type, Path path, String password, KeyStoreType storeType) {
        this.type = type;
        this.path = path;
        this.password = password;
        this.storeType = getType(storeType);
    }

    public KeyStore read() throws Exception {
        if (path == null) {
            return null;
        }
        resolvePathAndPassword();
        return load(path, password, storeType);
    }

    public static KeyStoreResult read(ISOSLogger logger, KeyStoreArguments args) throws Exception {
        if (args == null || !args.isCustomStoresEnabled()) {
            return null;
        }

        KeyStoreResult result = new KeyStoreReader().new KeyStoreResult();
        if (!args.getKeyStoreContainer().isEmpty()) {
            KeyStoreContainer c = args.getKeyStoreContainer().getValue();
            if (c.getKeyStore() == null) {
                result.setKeyStoreContainer(read(Type.KEYSTORE, c.getPath(), c.getPassword(), c.getType()));
            } else {
                result.setKeyStoreContainer(c);
            }
        }
        if (!SOSCollection.isEmpty(args.getTrustStoreContainers().getValue())) {
            for (KeyStoreContainer c : args.getTrustStoreContainers().getValue()) {
                if (c.getKeyStore() == null) {
                    result.addTrustStoreContainer(read(Type.TRUSTSTORE, c.getPath(), c.getPassword(), c.getType()));
                } else {
                    result.addTrustStoreContainer(c);
                }
            }
        }
        return result;
    }

    public static KeyStore load(Path path, String password, KeyStoreType storeType) throws Exception {
        if (path == null) {
            return null;
        }

        KeyStoreType type = getType(storeType);
        // char[] pass = password == null ? "".toCharArray() : password.toCharArray();
        char[] pass = password == null ? null : password.toCharArray();
        try (InputStream is = Files.newInputStream(path)) {
            KeyStore ks = KeyStore.getInstance(type.name());
            ks.load(is, pass);
            return ks;
        } catch (Exception e) {
            throw new Exception(String.format("[%s.load][%s][%s]%s", KeyStoreReader.class.getSimpleName(), storeType, path, e), e);
        }
    }

    public Path getPath() {
        return path;
    }

    public KeyStoreType getStoreType() {
        return storeType;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("type").append(type);
        if (storeType != null) {
            sb.append(",storeType=").append(storeType);
        }
        if (path != null) {
            sb.append(",path=").append(path);
        }
        return sb.toString();
    }

    private static KeyStoreContainer read(Type type, Path path, String password, KeyStoreType storeType) throws Exception {
        KeyStoreReader reader = new KeyStoreReader(type, path, password, storeType);
        reader.resolvePathAndPassword();

        KeyStoreContainer c = new KeyStoreContainer(reader.storeType, reader.path);
        c.setKeyStore(KeyStoreReader.load(reader.path, reader.password, reader.storeType));
        c.setPassword(reader.password);
        return c;
    }

    private void resolvePathAndPassword() {
        switch (type) {
        case KEYSTORE:
            setPathAndPassword(SYSTEM_PROPERTY_KEYSTORE_PATH, SYSTEM_PROPERTY_KEYSTORE_PASSWORD);
            break;
        case TRUSTSTORE:
            setPathAndPassword(SYSTEM_PROPERTY_TRUSTSTORE_PATH, SYSTEM_PROPERTY_TRUSTSTORE_PASSWORD);
            break;
        case KEY_AND_TRUSTSTORE:
            setPathAndPassword(SYSTEM_PROPERTY_KEYSTORE_PATH, SYSTEM_PROPERTY_KEYSTORE_PASSWORD);
            setPathAndPassword(SYSTEM_PROPERTY_TRUSTSTORE_PATH, SYSTEM_PROPERTY_TRUSTSTORE_PASSWORD);
            break;
        default:
            break;
        }
    }

    private static KeyStoreType getType(KeyStoreType storeType) {
        if (storeType != null) {
            return storeType;
        }
        try {
            return KeyStoreType.fromString(KeyStore.getDefaultType());
        } catch (Exception e) {
            return KeyStoreType.JKS;
        }
    }

    private void setPathAndPassword(String pathProperty, String passwordProperty) {
        if (path == null) {
            String val = System.getProperty(pathProperty);
            if (val != null) {
                path = Paths.get(val);
            }
        }
        if (password == null) {
            String val = System.getProperty(passwordProperty);
            if (val != null) {
                password = val;
            }
        }
    }

    public class KeyStoreResult {

        private KeyStoreContainer keyStoreContainer;
        private List<KeyStoreContainer> trustStoreContainers;

        public KeyStoreContainer getKeyStoreContainer() {
            return keyStoreContainer;
        }

        public List<KeyStoreContainer> getTrustStoreContainers() {
            return trustStoreContainers;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (keyStoreContainer != null && !SOSCollection.isEmpty(trustStoreContainers)) {
                KeyStoreContainer trustStoreSingleContainer = trustStoreContainers.size() == 1 ? trustStoreContainers.get(0) : null;
                if (trustStoreSingleContainer != null && trustStoreSingleContainer.getPath() != null && keyStoreContainer.getPath() != null
                        && trustStoreSingleContainer.getPath().equals(keyStoreContainer.getPath())) {
                    sb.append("keystore/truststore ").append(keyStoreContainer);
                } else {
                    sb.append(KeyStoreContainer.toString("keystore", keyStoreContainer));
                    sb.append(", ");
                    sb.append(KeyStoreContainer.toString("truststore", trustStoreContainers));
                }
            } else if (keyStoreContainer != null) {
                sb.append(KeyStoreContainer.toString("keystore", keyStoreContainer));
            } else if (!SOSCollection.isEmpty(trustStoreContainers)) {
                sb.append(KeyStoreContainer.toString("truststore", trustStoreContainers));
            }
            return sb.toString();
        }

        private void setKeyStoreContainer(KeyStoreContainer c) {
            keyStoreContainer = c;
        }

        private void addTrustStoreContainer(KeyStoreContainer c) {
            if (trustStoreContainers == null) {
                trustStoreContainers = new ArrayList<>();
            }
            trustStoreContainers.add(c);
        }
    }

}
