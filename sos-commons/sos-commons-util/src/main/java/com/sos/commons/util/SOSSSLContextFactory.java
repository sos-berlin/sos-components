package com.sos.commons.util;

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
import com.sos.commons.util.SOSJavaKeyStoreReader.SOSJavaKeyStoreResult;
import com.sos.commons.util.arguments.impl.SSLArguments;
import com.sos.commons.util.loggers.base.ISOSLogger;

public class SOSSSLContextFactory {

    /** TLS (Transport Layer Security) is the successor to SSL (Secure Sockets Layer) and is now considered the secure standard.<br/>
     * - SSL is outdated and considered insecure (especially SSLv2 and SSLv3)<br/>
     * - TLS (starting from TLSv1.2 and TLSv1.3) is more secure and is used in modern systems <br/>
     */
    public static final String DEFAULT_PROTOCOL = "TLS";

    public static SSLContext create(ISOSLogger logger, SSLArguments args) throws Exception {
        if (args == null) {
            throw new SOSMissingDataException("SSLArguments");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("[SOSSSLContextFactory]%s", args);
        }
        // Standard TLS without enforcing a specific protocol
        // This can include TLSv1.0, TLSv1.1, TLSv1.2, TLSv1.3, depending on the supported version in the Java environment.
        SSLContext sslContext = SSLContext.getInstance(DEFAULT_PROTOCOL);
        if (args.getUntrustedSSL().isTrue()) {
            if (args.getTrustedSSL().isEnabled()) {
                logger.info("[SOSSSLContextFactory][%s=true][ignored]%s", args.getUntrustedSSL().getName(), args.getTrustedSSLFullInfo());
            }
            if (args.getUntrustedSSLVerifyCertificateHostname().isTrue()) {
                sslContext.init(null, getAcceptUntrustedCertificateTrustManagers(), new SecureRandom());
            } else {
                sslContext.init(null, getAcceptUntrustedCertificateAndHostnameTrustManagers(), new SecureRandom());
            }
        } else {
            if (!args.getUntrustedSSLVerifyCertificateHostname().isTrue()) {
                String name = args.getUntrustedSSLVerifyCertificateHostname().getName();
                Boolean val = args.getUntrustedSSLVerifyCertificateHostname().getValue();
                // e.g. YADE uses DisableCertificateHostnameVerification
                if (args.getUntrustedSSLVerifyCertificateHostnameOppositeName() != null) {
                    name = args.getUntrustedSSLVerifyCertificateHostnameOppositeName();
                    val = !val;
                }
                logger.info("[SOSSSLContextFactory][ignored]%s=%s", name, val);
                args.getUntrustedSSLVerifyCertificateHostname().setValue(true);
            }

            SOSJavaKeyStoreResult result = SOSJavaKeyStoreReader.read(logger, args.getTrustedSSL());
            if (result == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("[SOSSSLContextFactory]use defaultJVMTrustManagers");
                }
                sslContext.init(null, getDefaultJVMTrustManagers(), new SecureRandom());
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("[SOSSSLContextFactory][SOSJavaKeyStoreResult]%s", result);
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
                logger.debug("[SOSSSLContextFactory][setEnabledProtocols]%s", String.join(",", enabledProtocols));
            }
            sslContext.createSSLEngine().setEnabledProtocols(enabledProtocols);
        }
        return sslContext;
    }

    /** Removes "TLS","SSL", accepts only e.g. "TLSv1.1", "TLSv1.2", "TLSv1.3" ... */
    public static String[] getFilteredEnabledProtocols(SSLArguments args) {
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
