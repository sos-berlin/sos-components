package com.sos.commons.util;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;

import com.sos.commons.util.arguments.impl.JavaKeyStoreArguments;
import com.sos.commons.util.arguments.impl.JavaKeyStoreArguments.KeyStoreType;

public class SOSJavaKeyStoreReader {

    public enum Type {
        KEYSTORE, TRUSTSTORE, KEY_AND_TRUSTSTORE
    }

    private static final String SYSTEM_PROPERTY_KEYSTORE_PATH = "javax.net.ssl.keyStore";
    private static final String SYSTEM_PROPERTY_TRUSTSTORE_PATH = "javax.net.ssl.trustStore";

    private static final String SYSTEM_PROPERTY_KEYSTORE_PASSWORD = "javax.net.ssl.keyStorePassword";
    private static final String SYSTEM_PROPERTY_TRUSTSTORE_PASSWORD = "javax.net.ssl.trustStorePassword";

    private final Type type;
    private Path path;
    private String password;
    /** KeyStore.getInstance: JKS/PKC12... */
    private String storeType;

    public SOSJavaKeyStoreReader(Type type) {
        this(type, null, null, null);
    }

    public SOSJavaKeyStoreReader(Type type, JavaKeyStoreArguments args) {
        this(type, args.getFile().getValue(), args.getPassword().getValue(), getType(args.getType().getValue()));
    }

    public SOSJavaKeyStoreReader(Type type, Path path) {
        this(type, path, null, null);
    }

    public SOSJavaKeyStoreReader(Type type, Path path, String password) {
        this(type, path, password, null);
    }

    public SOSJavaKeyStoreReader(Type type, Path path, String password, String storeType) {
        this.type = type;
        this.path = path;
        this.password = password;
        this.storeType = getType(storeType);
    }

    public KeyStore read() throws Exception {
        resolvePathAndPassword();
        if (path == null) {
            return null;
        }
        return load(path, password, storeType);
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
            throw new Exception(String.format("[%s][%s]%s", path, storeType, e), e);
        }
    }

    public char[] getPassword() {
        // char[] pass = password == null ? "".toCharArray() : password.toCharArray();
        return password == null ? null : password.toCharArray();
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

    public Path getPath() {
        return path;
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
}
