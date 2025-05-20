package com.sos.commons.util.keystore;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;

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
    private String storeType;

    private KeyStoreReader() {
        this(null, null, null, null);
    }

    private KeyStoreReader(Type type, Path path, String password, String storeType) {
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
        if (args == null || !args.isEnabled()) {
            return null;
        }
        KeyStoreResult result = new KeyStoreReader().new KeyStoreResult();
        if (!args.getKeyStoreFile().isEmpty()) {
            result.setKeyStoreResult(read(Type.KEYSTORE, args.getKeyStoreFile().getValue(), args.getKeyStorePassword().getValue(), args
                    .getKeyStoreType().getValue().name()));
        }
        if (!args.getTrustStoreFile().isEmpty()) {
            result.setTrustStoreResult(read(Type.TRUSTSTORE, args.getTrustStoreFile().getValue(), args.getTrustStorePassword().getValue(), args
                    .getTrustStoreType().getValue().name()));
        }
        return result;
    }

    public static KeyStore load(Path path, String password, KeyStoreType storeType) throws Exception {
        return load(path, password, getType(storeType));
    }

    public static KeyStore load(Path path, String password, String storeType) throws Exception {
        if (path == null) {
            return null;
        }

        String type = getType(storeType);
        // char[] pass = password == null ? "".toCharArray() : password.toCharArray();
        char[] pass = password == null ? null : password.toCharArray();
        try (InputStream is = Files.newInputStream(path)) {
            KeyStore ks = KeyStore.getInstance(type);
            ks.load(is, pass);
            return ks;
        } catch (Throwable e) {
            throw new Exception(String.format("[%s.load][%s][%s]%s", KeyStoreReader.class.getSimpleName(), storeType, path, e), e);
        }
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

    private static KeyStoreObject read(Type type, Path path, String password, String storeType) throws Exception {
        KeyStoreReader reader = new KeyStoreReader(type, path, password, storeType);
        reader.resolvePathAndPassword();

        KeyStoreObject result = new KeyStoreReader().new KeyStoreObject();
        result.type = reader.storeType;
        result.keyStore = KeyStoreReader.load(reader.path, reader.password, reader.storeType);
        result.path = reader.path;
        result.password = reader.getPassword();
        return result;
    }

    public char[] getPassword() {
        // char[] pass = password == null ? "".toCharArray() : password.toCharArray();
        return password == null ? null : password.toCharArray();
    }

    public Path getPath() {
        return path;
    }

    public String getStoreType() {
        return storeType;
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

    private static String getType(KeyStoreType storeType) {
        return getType(storeType == null ? null : storeType.name());
    }

    private static String getType(String storeType) {
        return storeType == null ? KeyStore.getDefaultType() : storeType;
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

        private String keyStoreType;
        private KeyStore keyStore;
        private Path keyStorePath;
        private char[] keyStorePassword;

        private String trustStoreType;
        private KeyStore trustStore;
        private Path trustStorePath;
        private char[] trustStorePassword;

        public String getKeyStoreType() {
            return keyStoreType;
        }

        public KeyStore getKeyStore() {
            return keyStore;
        }

        public Path getKeyStorePath() {
            return keyStorePath;
        }

        public char[] getKeyStorePassword() {
            return keyStorePassword;
        }

        public String getTrustStoreType() {
            return trustStoreType;
        }

        public KeyStore getTrustStore() {
            return trustStore;
        }

        public Path getTrustStorePath() {
            return trustStorePath;
        }

        public char[] getTrustStorePassword() {
            return trustStorePassword;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (keyStorePath != null && trustStorePath != null) {
                if (keyStorePath.equals(trustStorePath)) {
                    sb.append("keystore/truststore ").append(keyStoreType);
                    sb.append(" ").append(keyStorePath);
                } else {
                    sb.append("keystore ").append(keyStoreType);
                    sb.append(" ").append(keyStorePath);
                    sb.append(",truststore ").append(trustStoreType);
                    sb.append(" ").append(trustStorePath);
                }
            } else if (keyStorePath != null) {
                sb.append("keystore ").append(keyStoreType);
                sb.append(" ").append(keyStorePath);
            } else if (trustStorePath != null) {
                sb.append("truststore ").append(trustStoreType);
                sb.append(" ").append(trustStorePath);
            }
            return sb.toString();
        }

        private void setKeyStoreResult(KeyStoreObject ks) {
            keyStoreType = ks.type;
            keyStorePath = ks.path;
            keyStorePassword = ks.password;
            keyStore = ks.keyStore;
        }

        private void setTrustStoreResult(KeyStoreObject ks) {
            trustStoreType = ks.type;
            trustStorePath = ks.path;
            trustStorePassword = ks.password;
            trustStore = ks.keyStore;
        }
    }

    private class KeyStoreObject {

        private String type;
        private KeyStore keyStore;
        private Path path;
        private char[] password;
    }

}
