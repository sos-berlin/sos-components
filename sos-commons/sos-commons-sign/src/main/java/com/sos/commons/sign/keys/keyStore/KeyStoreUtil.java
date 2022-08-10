package com.sos.commons.sign.keys.keyStore;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class KeyStoreUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyStoreUtil.class);

    public static KeyStore readKeyStore(String keyStorePath, KeystoreType keyStoreType) throws KeyStoreException, NoSuchAlgorithmException,
            CertificateException, IOException {
        return readKeyStore(Paths.get(keyStorePath), keyStoreType, null);
    }

    public static KeyStore readKeyStore(Path keyStorePath, KeystoreType keyStoreType) throws Exception {
        return readKeyStore(keyStorePath, keyStoreType, null);
    }

    public static KeyStore readKeyStore(String keyStorePath, KeystoreType keyStoreType, String keyStorePassword) throws KeyStoreException,
            NoSuchAlgorithmException, CertificateException, IOException {
        return readKeyStore(Paths.get(keyStorePath), keyStoreType, keyStorePassword);
    }

    public static KeyStore readKeyStore(Path keyStorePath, KeystoreType keyStoreType, String keyStorePassword) throws IOException, KeyStoreException,
            NoSuchAlgorithmException, CertificateException {
        InputStream keyStoreStream = null;
        try {
            boolean fileAlreadyExist = Files.exists(keyStorePath);
            if (!fileAlreadyExist) {
                LOGGER.warn(String.format("KeyStore with path: %1$s does not exist!", keyStorePath.toAbsolutePath()));
            }
            keyStoreStream = Files.newInputStream(keyStorePath);
            // for testing with keystore in classpath
            // keyStoreStream = KeyStoreUtil.class.getResourceAsStream(keyStorePath);
            KeyStore keyStore = KeyStore.getInstance(keyStoreType.value()); // "PKCS12" or "JKS"
            if (fileAlreadyExist) {
                if (keyStorePassword == null) {
                    keyStore.load(keyStoreStream, "".toCharArray());
                } else {
                    keyStore.load(keyStoreStream, keyStorePassword.toCharArray());
                }
            } else {
                keyStore.load(null, null);
            }
            return keyStore;
        } finally {
            if (keyStoreStream != null) {
                keyStoreStream.close();
            }
        }
    }

    public static KeyStore readTrustStore(String trustStorePath, KeystoreType trustStoreType) throws KeyStoreException, NoSuchAlgorithmException,
            CertificateException, IOException {
        return readTrustStore(Paths.get(trustStorePath), trustStoreType, null);
    }

    public static KeyStore readTrustStore(Path trustStorePath, KeystoreType trustStoreType) throws Exception {
        return readTrustStore(trustStorePath, trustStoreType, null);
    }

    public static KeyStore readTrustStore(String trustStorePath, KeystoreType trustStoreType, String trustStorePassword) throws Exception {
        return readTrustStore(Paths.get(trustStorePath), trustStoreType, trustStorePassword);
    }

    public static KeyStore readTrustStore(Path trustStorePath, KeystoreType trustStoreType, String trustStorePassword) throws IOException,
            KeyStoreException, NoSuchAlgorithmException, CertificateException {
        InputStream trustStoreStream = null;
        try {
            boolean fileAlreadyExist = Files.exists(trustStorePath);
            if (!fileAlreadyExist) {
                LOGGER.warn(String.format("TrustStore with path: %1$s does not exist!", trustStorePath.toAbsolutePath()));
            }
            trustStoreStream = Files.newInputStream(trustStorePath);
            KeyStore trustStore = KeyStore.getInstance(trustStoreType.value()); // "PKCS12" or "JKS"
            if (fileAlreadyExist) {
                if (trustStorePassword == null || trustStorePassword.isEmpty()) {
                    trustStore.load(trustStoreStream, "".toCharArray());
                } else {
                    trustStore.load(trustStoreStream, trustStorePassword.toCharArray());
                }
            } else {
                trustStore.load(null, null);
            }
            return trustStore;
        } finally {
            if (trustStoreStream != null) {
                trustStoreStream.close();
            }
        }
    }

    public static Certificate getCertificateFromKeyStore(KeyStore keyStore, String alias) throws KeyStoreException {
        if (keyStore.containsAlias(alias)) {
            Certificate certificate = keyStore.getCertificate(alias);
            return certificate;
        } else {
            return null;
        }
    }

    public static X509Certificate getX509CertificateFromKeyStore(KeyStore keyStore, String alias) throws KeyStoreException {
        Certificate certificate = getCertificateFromKeyStore(keyStore, alias);
        if (certificate != null) {
            try {
                return (X509Certificate) certificate;
            } catch (ClassCastException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    public static Certificate[] getX509CertificateChainFromKeyStore(KeyStore keyStore, String alias) throws KeyStoreException {
        if (keyStore.containsAlias(alias)) {
            Certificate[] certificateChain = keyStore.getCertificateChain(alias);
            return certificateChain;
        } else {
            return null;
        }
    }
}
