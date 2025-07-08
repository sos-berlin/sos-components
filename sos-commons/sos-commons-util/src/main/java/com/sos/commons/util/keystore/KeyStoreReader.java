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
        if (!args.getKeyStoreFile().isEmpty()) {
            result.setKeyStoreFile(read(Type.KEYSTORE, args.getKeyStoreFile().getValue(), args.getKeyStorePassword().getValue(), args
                    .getKeyStoreType().getValue()));
        }
        if (!SOSCollection.isEmpty(args.getTrustStoreFiles().getValue())) {
            for (KeyStoreFile f : args.getTrustStoreFiles().getValue()) {
                result.addTrustStoreFile(read(Type.TRUSTSTORE, f.getPath(), f.getPassword(), f.getType()));
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
        } catch (Throwable e) {
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

    private static KeyStoreFile read(Type type, Path path, String password, KeyStoreType storeType) throws Exception {
        KeyStoreReader reader = new KeyStoreReader(type, path, password, storeType);
        reader.resolvePathAndPassword();

        KeyStoreFile f = new KeyStoreFile();
        f.setType(reader.storeType);
        f.setKeyStore(KeyStoreReader.load(reader.path, reader.password, reader.storeType));
        f.setPath(reader.path);
        f.setPassword(reader.password);
        return f;
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

        private KeyStoreFile keyStoreFile;
        private List<KeyStoreFile> trustStoreFiles;

        public KeyStoreFile getKeyStoreFile() {
            return keyStoreFile;
        }

        public List<KeyStoreFile> getTrustStoreFiles() {
            return trustStoreFiles;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (keyStoreFile != null && !SOSCollection.isEmpty(trustStoreFiles)) {
                KeyStoreFile trustStoreSingleFile = trustStoreFiles.size() == 1 ? trustStoreFiles.get(0) : null;
                if (trustStoreSingleFile != null && trustStoreSingleFile.getPath().equals(keyStoreFile.getPath())) {
                    sb.append("keystore/truststore ").append(keyStoreFile.getType());
                    sb.append(" ").append(keyStoreFile.getPath());
                } else {
                    sb.append(KeyStoreFile.toString("keystore", keyStoreFile));
                    sb.append(", ");
                    sb.append(KeyStoreFile.toString("truststore", trustStoreFiles));
                }
            } else if (keyStoreFile != null) {
                sb.append(KeyStoreFile.toString("keystore", keyStoreFile));
            } else if (!SOSCollection.isEmpty(trustStoreFiles)) {
                sb.append(KeyStoreFile.toString("truststore", trustStoreFiles));
            }
            return sb.toString();
        }

        private void setKeyStoreFile(KeyStoreFile f) {
            keyStoreFile = f;
        }

        private void addTrustStoreFile(KeyStoreFile f) {
            if (trustStoreFiles == null) {
                trustStoreFiles = new ArrayList<>();
            }
            trustStoreFiles.add(f);
        }
    }

}
