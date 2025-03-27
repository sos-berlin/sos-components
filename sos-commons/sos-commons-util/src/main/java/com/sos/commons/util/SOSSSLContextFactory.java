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

    /** TLS (Transport Layer Security) is the successor to SSL (Secure Sockets Layer) and is now considered the secure standard.<br/>
     * - SSL is outdated and considered insecure (especially SSLv2 and SSLv3)<br/>
     * - TLS (starting from TLSv1.2 and TLSv1.3) is more secure and is used in modern systems <br/>
     */
    public static final String DEFAULT_PROTOCOL = "TLS";

    public static SSLContext create(SSLArguments args) throws Exception {
        if (args == null) {
            throw new SOSMissingDataException("SSLArguments");
        }
        // Standard TLS without enforcing a specific protocol
        // This can include TLSv1.0, TLSv1.1, TLSv1.2, TLSv1.3, depending on the supported version in the Java environment.
        SSLContext sslContext = SSLContext.getInstance(DEFAULT_PROTOCOL);

        SOSJavaKeyStoreResult result = SOSJavaKeyStoreReader.read(args.getJavaKeyStore());
        if (result == null) {
            // TODO throw Exception ?
        } else {
            try {
                sslContext.init(getKeyManagers(result.getKeyStore(), result.getKeyStorePassword(), null), getTrustManagers(result.getTrustStore(),
                        null, args.getAcceptUntrustedCertificate().isTrue()), null);
            } catch (Exception e) {
                throw new Exception("[" + result.toString() + "][KeyManagerFactory.getDefaultAlgorithm()=" + KeyManagerFactory.getDefaultAlgorithm()
                        + "]" + e, e);
            }
        }
        if (!args.getProtocols().isEmpty()) {
            // Remove "TLS", accepts only e.g. "TLSv1.1", "TLSv1.2", "TLSv1.3" ...
            String[] filtered = args.getProtocols().getValue().stream().filter(p -> !DEFAULT_PROTOCOL.equalsIgnoreCase(p) && !"SSL".equalsIgnoreCase(
                    p)).toArray(String[]::new);
            if (filtered.length > 0) {
                // Set explicitly supported protocols: e.g. "TLSv1.2", "TLSv1.3"
                sslContext.createSSLEngine().setEnabledProtocols(filtered);
            }
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
