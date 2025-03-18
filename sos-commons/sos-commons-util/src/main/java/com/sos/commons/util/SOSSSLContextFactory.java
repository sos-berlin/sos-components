package com.sos.commons.util;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.util.SOSJavaKeyStoreReader.SOSJavaKeyStoreResult;
import com.sos.commons.util.arguments.impl.SSLArguments;

public class SOSSSLContextFactory {

    public static SSLContext create(SSLArguments args) throws Exception {
        if (args == null) {
            throw new SOSMissingDataException("SSLArguments");
        }
        // Standard TLS without enforcing a specific protocol
        SSLContext sslContext = SSLContext.getInstance("TLS");

        SOSJavaKeyStoreResult result = SOSJavaKeyStoreReader.read(args.getJavaKeyStore());
        if (result == null) {
            // TODO throw Exception ?
        } else {
            sslContext.init(getKeyManagers(result.getKeyStore(), result.getKeyStorePassword(), result.getKeyStoreType()), getTrustManagers(result
                    .getTrustStore(), result.getTrustStoreType(), args.getAcceptUntrustedCertificate().isTrue()), null);
        }
        if (!args.getProtocols().isEmpty()) {
            // Set explicitly supported protocols
            sslContext.createSSLEngine().setEnabledProtocols(args.getProtocols().getValue().stream().toArray(String[]::new));
        }
        return sslContext;
    }

    private static KeyManager[] getKeyManagers(final KeyStore keystore, final char[] keyPassword, String keyManagerFactoryAlgorithm)
            throws Exception {
        final KeyManagerFactory f = KeyManagerFactory.getInstance(keyManagerFactoryAlgorithm == null ? KeyManagerFactory.getDefaultAlgorithm()
                : keyManagerFactoryAlgorithm);
        f.init(keystore, keyPassword);
        return f.getKeyManagers();
    }

    private static TrustManager[] getTrustManagers(final KeyStore truststore, String trustManagerFactoryAlgorithm, boolean trustAll)
            throws NoSuchAlgorithmException, KeyStoreException {
        if (trustAll) {
            return new TrustManager[] { new X509TrustManager() {

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            } };
        }
        final TrustManagerFactory f = TrustManagerFactory.getInstance(trustManagerFactoryAlgorithm == null ? TrustManagerFactory.getDefaultAlgorithm()
                : trustManagerFactoryAlgorithm);
        f.init(truststore);
        return f.getTrustManagers();
    }

}
