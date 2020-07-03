package com.sos.joc.classes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import js7.base.generic.SecretString;
import js7.common.akkahttp.https.KeyStoreRef;
import js7.common.akkahttp.https.TrustStoreRef;

public class SSLContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(SSLContext.class);
    private static Path keystorePath;
    private static String keystoreType;
    private static String keystorePass;
    private static String keyPass;
    private static long keystoreModTime = 0;
    private static Path truststorePath;
    private static String truststoreType;
    private static String truststorePass;
    private static long truststoreModTime = 0;
    private static KeyStoreRef keyStoreRef;
    private static TrustStoreRef trustStoreRef;
    private static JocCockpitProperties sosJocProperties;
    public static javax.net.ssl.SSLContext keystore;
    public static javax.net.ssl.SSLContext truststore;
    
    public static void setJocProperties(JocCockpitProperties properties) {
        sosJocProperties = properties;
    }
    
    public static KeyStoreRef loadKeyStore() {
        if (sosJocProperties == null) {
            sosJocProperties = new JocCockpitProperties();
        }
        if (sosJocProperties != null) {
            String kPath = sosJocProperties.getProperty("keystore_path", System.getProperty("javax.net.ssl.keyStore"));
            String kType = sosJocProperties.getProperty("keystore_type", System.getProperty("javax.net.ssl.keyStoreType"));
            String kPass = sosJocProperties.getProperty("keystore_password", System.getProperty("javax.net.ssl.keyStorePassword"));
            String kMPass = sosJocProperties.getProperty("key_password", System.getProperty("javax.net.ssl.keyPassword"));
            if (kPath != null && !kPath.trim().isEmpty()) {
                Path p = sosJocProperties.resolvePath(kPath.trim());
                if (p != null) {
                    if (!Files.exists(p)) {
                        resetKeyStore();
                        LOGGER.error(String.format("keystore path (%1$s) is set but file (%2$s) not found.", kPath, p.toString()));
                    } else {
                        try {
                            if (reloadKeyStore(p, kType, kPass, kMPass)) {
                                keyStoreRef = KeyStoreRef.apply(p, SecretString.apply(kPass), SecretString.apply(kMPass));
                                keystore = SSLContexts.custom().loadKeyMaterial(readKeyStore(), getKeyPass()).build();
                            }
                        } catch (Exception e) {
                            resetKeyStore();
                            LOGGER.error("Client certificate not loaded", e);
                        }
                    }
                } else {
                    resetKeyStore();
                }
            } else {
                resetKeyStore();
            }
        }
        return keyStoreRef;
    }
    
    public static KeyStoreRef loadKeyStore(JocCockpitProperties properties) {
        setJocProperties(properties);
        return loadKeyStore();
    }
    
    public static TrustStoreRef loadTrustStore() {
        if (sosJocProperties == null) {
            sosJocProperties = new JocCockpitProperties();
        }
        if (sosJocProperties != null) {
            String tPath = sosJocProperties.getProperty("truststore_path", System.getProperty("javax.net.ssl.trustStore"));
            String tType = sosJocProperties.getProperty("truststore_type", System.getProperty("javax.net.ssl.trustStoreType"));
            String tPass = sosJocProperties.getProperty("truststore_password", System.getProperty("javax.net.ssl.trustStorePassword"));
            if (tPath != null && !tPath.trim().isEmpty()) {
                Path p = sosJocProperties.resolvePath(tPath.trim());
                if (p != null) {
                    if (!Files.exists(p)) {
                        resetTrustStore();
                        LOGGER.error(String.format("truststore path (%1$s) is set but file (%2$s) not found.", tPath, p.toString()));
                    } else {
                        try {
                            if (reloadTrustStore(p, tType, tPass)) {
                                trustStoreRef = TrustStoreRef.apply(p, SecretString.apply(tPass));
                                truststore = SSLContexts.custom().loadTrustMaterial(readTrustStore(), null).build();
                            }
                        } catch (Exception e) {
                            resetTrustStore();
                            LOGGER.error("Client certificate not loaded", e);
                        }
                    }
                } else {
                    resetTrustStore();
                }
            } else {
                resetTrustStore();
            }
        }
        return trustStoreRef;
    }
    
    public static TrustStoreRef loadTrustStore(JocCockpitProperties properties) {
        setJocProperties(properties);
        return loadTrustStore();
    }

    private static boolean reloadKeyStore(Path path, String type, String pass, String mPass) throws IOException {
        return reloadKeyStore(path, type, pass, mPass, Files.getLastModifiedTime(path).toMillis());
    }

    private static boolean reloadKeyStore(Path path, String type, String pass, String mPass, long modTime) {
        String kPath = keystorePath == null ? null : keystorePath.toString();
        if (!new EqualsBuilder().append(kPath, path.toString()).append(keystoreType, type).append(keystorePass, pass).append(keyPass, mPass).append(
                keystoreModTime, modTime).isEquals()) {
            keystorePath = path;
            keystoreType = type;
            keystorePass = pass;
            keyPass = mPass;
            keystoreModTime = modTime;
            return true;
        }
        return false;
    }
    
    private static boolean reloadTrustStore(Path path, String type, String pass) throws IOException {
        return reloadTrustStore(path, type, pass, Files.getLastModifiedTime(path).toMillis());
    }

    private static boolean reloadTrustStore(Path path, String type, String pass, long modTime) {
        String tPath = truststorePath == null ? null : truststorePath.toString();
        if (!new EqualsBuilder().append(tPath, path.toString()).append(truststoreType, type).append(truststorePass, pass).append(truststoreModTime,
                modTime).isEquals()) {
            truststorePath = path;
            truststoreType = type;
            truststorePass = pass;
            truststoreModTime = modTime;
            return true;
        }
        return false;
    }

    private static void resetKeyStore() {
        keystorePath = null;
        keystoreType = null;
        keystorePass = null;
        keyPass = null;
        keystoreModTime = 0;
        keystore = null;
        keyStoreRef = null;
    }
    
    private static void resetTrustStore() {
        truststorePath = null;
        truststoreType = null;
        truststorePass = null;
        truststoreModTime = 0;
        truststore = null;
        trustStoreRef = null;
    }

    private static KeyStore readKeyStore() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        KeyStore keyStore = KeyStore.getInstance(getKeystoreType());
        keyStore.load(Files.newInputStream(keystorePath), getKeystorePass());
        return keyStore;
    }

    private static char[] getKeystorePass() {
        if (keystorePass != null) {
            return keystorePass.toCharArray();
        }
        return null;
    }

    private static String getKeystoreType() {
        if (keystoreType == null) {
            return KeyStore.getDefaultType();
        }
        return keystoreType;
    }

    private static char[] getKeyPass() {
        if (keyPass != null) {
            return keyPass.toCharArray();
        }
        return null;
    }
    
    private static KeyStore readTrustStore() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        KeyStore keyStore = KeyStore.getInstance(getTruststoreType());
        keyStore.load(Files.newInputStream(truststorePath), getTruststorePass());
        return keyStore;
    }

    private static char[] getTruststorePass() {
        if (truststorePass != null) {
            return truststorePass.toCharArray();
        }
        return null;
    }

    private static String getTruststoreType() {
        if (truststoreType == null) {
            return KeyStore.getDefaultType();
        }
        return truststoreType;
    }
}
