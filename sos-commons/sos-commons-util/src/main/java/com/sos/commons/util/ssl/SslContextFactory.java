package com.sos.commons.util.ssl;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;

import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.util.keystore.KeyStoreReader;
import com.sos.commons.util.keystore.KeyStoreReader.KeyStoreResult;
import com.sos.commons.util.loggers.base.ISOSLogger;

/** Named SslContextFactory (not SSLContextFactory) to follow modern Java naming conventions,<br/>
 * where acronyms use only the first letter capitalized (e.g., HttpClient, XmlParser). */
public class SslContextFactory {

    /** TLS (Transport Layer Security) is the successor to SSL (Secure Sockets Layer) and is now considered the secure standard.<br/>
     * - SSL is outdated and considered insecure (especially SSLv2 and SSLv3)<br/>
     * - TLS (starting from TLSv1.2 and TLSv1.3) is more secure and is used in modern systems <br/>
     */
    public static final String DEFAULT_PROTOCOL = "TLS";

    public static SSLContext create(ISOSLogger logger, SslArguments args) throws Exception {
        if (args == null) {
            throw new SOSMissingDataException("SSLArguments");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("[SslContextFactory]%s", args);
        }
        // Standard TLS without enforcing a specific protocol
        // This can include TLSv1.0, TLSv1.1, TLSv1.2, TLSv1.3, depending on the supported version in the Java environment.
        SSLContext sslContext = SSLContext.getInstance(DEFAULT_PROTOCOL);
        if (args.getUntrustedSsl().isTrue()) {
            if (args.getTrustedSsl().isEnabled()) {
                logger.info("[SslContextFactory][%s=true][ignored]%s", args.getUntrustedSsl().getName(), args.getTrustedSslFullInfo());
            }
            if (args.getUntrustedSslVerifyCertificateHostname().isTrue()) {
                sslContext.init(null, getAcceptUntrustedCertificateTrustManagers(), new SecureRandom());
            } else {
                sslContext.init(null, getAcceptUntrustedCertificateAndHostnameTrustManagers(), new SecureRandom());
            }
        } else {
            if (!args.getUntrustedSslVerifyCertificateHostname().isTrue()) {
                String name = args.getUntrustedSslVerifyCertificateHostname().getName();
                Boolean val = args.getUntrustedSslVerifyCertificateHostname().getValue();
                // e.g. YADE uses DisableCertificateHostnameVerification
                if (args.getUntrustedSslVerifyCertificateHostnameOppositeName() != null) {
                    name = args.getUntrustedSslVerifyCertificateHostnameOppositeName();
                    val = !val;
                }
                logger.info("[SslContextFactory][ignored]%s=%s", name, val);
                args.getUntrustedSslVerifyCertificateHostname().setValue(true);
            }

            KeyStoreResult result = KeyStoreReader.read(logger, args.getTrustedSsl());
            if (result == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("[SslContextFactory]use defaultJVMTrustManagers");
                }
                sslContext.init(null, getDefaultJVMTrustManagers(), new SecureRandom());
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("[SslContextFactory][KeyStoreResult]%s", result);
                }
                try {
                    sslContext.init(getKeyManagers(result.getKeyStore(), result.getKeyStorePassword()), getTrustManagers(result.getTrustStore()),
                            new SecureRandom());
                } catch (Exception e) {
                    throw new Exception("[" + result.toString() + "][KeyManagerFactory.getDefaultAlgorithm()=" + KeyManagerFactory
                            .getDefaultAlgorithm() + "]" + e, e);
                }
            }
        }
        String[] enabledProtocols = getFilteredEnabledProtocols(args);
        if (enabledProtocols.length > 0) {
            // Set explicitly supported protocols: e.g. "TLSv1.2", "TLSv1.3"
            if (logger.isDebugEnabled()) {
                logger.debug("[SslContextFactory][setEnabledProtocols]%s", String.join(",", enabledProtocols));
            }
            sslContext.createSSLEngine().setEnabledProtocols(enabledProtocols);
        }
        return sslContext;
    }

    /** Removes "TLS","SSL", accepts only e.g. "TLSv1.1", "TLSv1.2", "TLSv1.3" ... */
    public static String[] getFilteredEnabledProtocols(SslArguments args) {
        if (args == null || args.getEnabledProtocols().isEmpty()) {
            return new String[0];
        }
        return Arrays.stream(args.getEnabledProtocols().getValue().split(",")).map(String::trim).filter(s -> !s.isEmpty()).filter(s -> !s
                .equalsIgnoreCase(DEFAULT_PROTOCOL) && !s.equalsIgnoreCase("SSL")).toArray(String[]::new);
    }

    private static KeyManager[] getKeyManagers(final KeyStore keystore, final char[] keyPassword) throws Exception {
        final KeyManagerFactory f = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        f.init(keystore, keyPassword);
        return f.getKeyManagers();
    }

    private static TrustManager[] getDefaultJVMTrustManagers() throws Exception {
        TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        // null = use the default KeyStore (from JVM Truststore)
        factory.init((KeyStore) null);
        return factory.getTrustManagers();
    }

    private static TrustManager[] getTrustManagers(final KeyStore truststore) throws NoSuchAlgorithmException, KeyStoreException {
        final TrustManagerFactory f = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        f.init(truststore);
        return f.getTrustManagers();
    }

    /** Accepts all certificates - not includes disabling hostname verification
     * 
     * @return
     * @throws Exception */
    private static TrustManager[] getAcceptUntrustedCertificateTrustManagers() throws Exception {
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

    /** Accepts all certificates - includes disabling hostname verification
     * 
     * @return
     * @throws Exception */
    private static TrustManager[] getAcceptUntrustedCertificateAndHostnameTrustManagers() throws Exception {
        return new TrustManager[] { new X509ExtendedTrustManager() {

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType, java.net.Socket socket) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType, java.net.Socket socket) {
            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType, javax.net.ssl.SSLEngine engine) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType, javax.net.ssl.SSLEngine engine) {
            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        } };
    }

}
