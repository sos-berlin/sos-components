package com.sos.commons.sign.keys.keyStore;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

public abstract class KeyStoreUtil {

	
    public static KeyStore readKeyStore(String keyStorePath, KeyStoreType keyStoreType) throws Exception {
    	return readKeyStore(Paths.get(keyStorePath), keyStoreType, null);
    }
    
    public static KeyStore readKeyStore(Path keyStorePath, KeyStoreType keyStoreType) throws Exception {
    	return readKeyStore(keyStorePath, keyStoreType, null);
    }
	
    public static KeyStore readKeyStore(String keyStorePath, KeyStoreType keyStoreType, String keyStorePassword) throws Exception {
    	return readKeyStore(Paths.get(keyStorePath), keyStoreType, keyStorePassword);
    }
    
    public static KeyStore readKeyStore(Path keyStorePath, KeyStoreType keyStoreType, String keyStorePassword) throws Exception {
        InputStream keyStoreStream = null;
        try {
        	keyStoreStream = Files.newInputStream(keyStorePath);
        	// for testing with keystore in classpath
        	// keyStoreStream = KeyStoreUtil.class.getResourceAsStream(keyStorePath);
            KeyStore keyStore = KeyStore.getInstance(keyStoreType.value()); // "PKCS12" or "JKS"
            if (keyStorePassword == null) {
            	keyStore.load(keyStoreStream, "".toCharArray());
            } else {
            	keyStore.load(keyStoreStream, keyStorePassword.toCharArray());
            }
            return keyStore;
        } finally {
            if (keyStoreStream != null) {
                keyStoreStream.close();
            }
        }
    }

    public static KeyStore readTrustStore(String trustStorePath, KeyStoreType trustStoreType) throws Exception {
    	return readTrustStore(Paths.get(trustStorePath), trustStoreType, null);
    }
    
    public static KeyStore readTrustStore(Path trustStorePath, KeyStoreType trustStoreType) throws Exception {
    	return readTrustStore(trustStorePath, trustStoreType, null);
    }
	
    public static KeyStore readTrustStore(String trustStorePath, KeyStoreType trustStoreType, String trustStorePassword) throws Exception {
    	return readTrustStore(Paths.get(trustStorePath), trustStoreType, trustStorePassword);
    }
    
    public static KeyStore readTrustStore(Path trustStorePath, KeyStoreType trustStoreType, String trustStorePassword) throws Exception {
        InputStream trustStoreStream = null;
        try {
        	trustStoreStream = Files.newInputStream(trustStorePath);
            KeyStore trustStore = KeyStore.getInstance(trustStoreType.value()); // "PKCS12" or "JKS"
            if (trustStorePassword == null || trustStorePassword.isEmpty()) {
            	trustStore.load(trustStoreStream, "".toCharArray());
            } else {
            	trustStore.load(trustStoreStream, trustStorePassword.toCharArray());
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

    public static X509Certificate getX509CertificateFromKeyStore (KeyStore keyStore, String alias) throws KeyStoreException {
		Certificate certificate = getCertificateFromKeyStore(keyStore, alias);
		if (certificate != null) {
			try {
				return (X509Certificate)certificate;
			} catch (ClassCastException e) {
				return null;
			}
		} else {
			return null;
		}
    }

    public static Certificate[] getX509CertificateChainFromKeyStore (KeyStore keyStore, String alias) throws KeyStoreException {
    	if (keyStore.containsAlias(alias)) {
    		Certificate[] certificateChain = keyStore.getCertificateChain(alias);
    		return certificateChain;
    	} else {
    		return null;
    	}
    }
}
