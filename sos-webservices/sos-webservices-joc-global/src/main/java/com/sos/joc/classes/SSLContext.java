package com.sos.joc.classes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Optional;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import js7.base.generic.SecretString;
import js7.base.io.https.KeyStoreRef;
import js7.base.io.https.TrustStoreRef;
import js7.data_for_java.auth.JHttpsConfig;

public class SSLContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(SSLContext.class);
    private static SSLContext sslContext;
    private volatile Path keystorePath;
    private volatile String keystoreType;
    private volatile String keystorePass;
    private volatile String keyPass;
    private volatile long keystoreModTime = 0;
    private volatile Path truststorePath;
    private volatile String truststoreType;
    private volatile String truststorePass;
    private volatile long truststoreModTime = 0;
    private volatile KeyStoreRef keyStoreRef;
    private volatile TrustStoreRef trustStoreRef;
    private volatile JocCockpitProperties sosJocProperties;
    private volatile KeyStore keystore;
    private volatile KeyStore truststore;
    private volatile char[] keyPassChars;
    private volatile javax.net.ssl.SSLContext netSSlContext;
    private volatile JHttpsConfig httpsConfig;

    private SSLContext() {
    }

    public static synchronized SSLContext getInstance() {
        if (sslContext == null) {
            sslContext = new SSLContext();
        }
        return sslContext;
    }

    public JHttpsConfig getHttpsConfig() {
        return httpsConfig;
    }

    public KeyStore getKeyStore() {
        return keystore;
    }

    public char[] getKeyStorePass() {
        return keyPassChars;
    }

    public KeyStore getTrustStore() {
        return truststore;
    }

    public javax.net.ssl.SSLContext getSSLContext() {
        return netSSlContext;
    }

    public synchronized JHttpsConfig loadHttpsConfig() {
        loadKeyStore();
        loadTrustStore();
        setHttpsConfig();
        return httpsConfig;
    }

    public synchronized void setJocProperties(JocCockpitProperties properties) {
        sosJocProperties = properties;
    }

    public synchronized void setSSLContext() {
        loadHttpsConfig();
        if (keystore != null || truststore != null) {
            try {
                SSLContextBuilder sslContextBuilder = SSLContexts.custom();
                sslContextBuilder.setKeyManagerFactoryAlgorithm(sosJocProperties.getProperty("ssl_keymanagerfactory_algorithm", KeyManagerFactory
                        .getDefaultAlgorithm()));
                sslContextBuilder.setTrustManagerFactoryAlgorithm(sosJocProperties.getProperty("ssl_trustmanagerfactory_algorithm",
                        TrustManagerFactory.getDefaultAlgorithm()));
                if (keystore != null) {
                    sslContextBuilder.loadKeyMaterial(keystore, keyPassChars);
                }
                if (truststore != null) {
                    sslContextBuilder.loadTrustMaterial(truststore, null);
                }
                netSSlContext = sslContextBuilder.build();
            } catch (GeneralSecurityException e) {
                if (e.getCause() != null) {
                    LOGGER.error("", e.getCause());
                } else {
                    LOGGER.error("", e);
                }
            }
        }
    }

    public synchronized void setSSLContext(JocCockpitProperties properties) {
        setJocProperties(properties);
        setSSLContext();
    }

    public synchronized KeyStoreRef loadKeyStore() {
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
                                keyStoreRef = KeyStoreRef.apply(p, SecretString(kPass), SecretString(kMPass));
                                keystore = readKeyStore();
                                keyPassChars = getKeyPass();
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

    public synchronized KeyStoreRef loadKeyStore(JocCockpitProperties properties) {
        setJocProperties(properties);
        return loadKeyStore();
    }

    public synchronized TrustStoreRef loadTrustStore() {
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
                                trustStoreRef = TrustStoreRef.apply(p, SecretString(tPass));
                                truststore = readTrustStore();
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

    public synchronized TrustStoreRef loadTrustStore(JocCockpitProperties properties) {
        setJocProperties(properties);
        return loadTrustStore();
    }

    public void setHttpsConfig() {
        getHttpsConfig(keyStoreRef, trustStoreRef);
    }

    public JHttpsConfig getHttpsConfig(KeyStoreRef keyStoreRef, TrustStoreRef trustStoreRef) {
        if (keyStoreRef == null && trustStoreRef == null) {
            httpsConfig = JHttpsConfig.empty();
        } else {
            Optional<KeyStoreRef> oKeyStoreRef = Optional.empty();
            if (keyStoreRef != null) {
                oKeyStoreRef = Optional.of(keyStoreRef);
            }
            httpsConfig = JHttpsConfig.apply(JHttpsConfig.of(oKeyStoreRef, Arrays.asList(trustStoreRef)));
        }
        return httpsConfig;
    }

    private boolean reloadKeyStore(Path path, String type, String pass, String mPass) throws IOException {
        return reloadKeyStore(path, type, pass, mPass, Files.getLastModifiedTime(path).toMillis());
    }

    private boolean reloadKeyStore(Path path, String type, String pass, String mPass, long modTime) {
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

    private boolean reloadTrustStore(Path path, String type, String pass) throws IOException {
        return reloadTrustStore(path, type, pass, Files.getLastModifiedTime(path).toMillis());
    }

    private boolean reloadTrustStore(Path path, String type, String pass, long modTime) {
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

    private void resetKeyStore() {
        keystorePath = null;
        keystoreType = null;
        keystorePass = null;
        keyPass = null;
        keystoreModTime = 0;
        keystore = null;
        keyPassChars = null;
        keyStoreRef = null;
    }

    private void resetTrustStore() {
        truststorePath = null;
        truststoreType = null;
        truststorePass = null;
        truststoreModTime = 0;
        truststore = null;
        trustStoreRef = null;
    }

    private KeyStore readKeyStore() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        KeyStore keyStore = KeyStore.getInstance(getKeystoreType());
        keyStore.load(Files.newInputStream(keystorePath), getKeystorePass());
        return keyStore;
    }

    private char[] getKeystorePass() {
        if (keystorePass != null) {
            return keystorePass.toCharArray();
        }
        return null;
    }

    private String getKeystoreType() {
        if (keystoreType == null) {
            return KeyStore.getDefaultType();
        }
        return keystoreType;
    }

    private char[] getKeyPass() {
        if (keyPass != null) {
            return keyPass.toCharArray();
        }
        return null;
    }

    private KeyStore readTrustStore() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        KeyStore keyStore = KeyStore.getInstance(getTruststoreType());
        keyStore.load(Files.newInputStream(truststorePath), getTruststorePass());
        return keyStore;
    }

    private char[] getTruststorePass() {
        if (truststorePass != null) {
            return truststorePass.toCharArray();
        }
        return null;
    }

    private String getTruststoreType() {
        if (truststoreType == null) {
            return KeyStore.getDefaultType();
        }
        return truststoreType;
    }

    private SecretString SecretString(String pass) {
        if (pass == null) {
            pass = "";
        }
        return SecretString.apply(pass);
    }
}
