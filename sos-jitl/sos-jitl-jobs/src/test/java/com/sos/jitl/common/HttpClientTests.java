package com.sos.jitl.common;

import static org.junit.Assert.assertNotNull;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.httpclient.BaseHttpClient;
import com.sos.commons.httpclient.commons.HttpExecutionResult;
import com.sos.commons.sign.keys.certificate.CertificateUtils;
import com.sos.commons.sign.keys.keyStore.KeyStoreUtil;
import com.sos.commons.sign.keys.keyStore.KeystoreType;

public class HttpClientTests {

    private static final String CERTSTORE_PATH = "C:/sp/devel/js7/LicenceCheck/icloud-certstore.p12";
    private static final String KEYSTORE_PATH = "C:/sp/devel/js7/keys/sp-keystore.p12";
    private static final String TRUSTORE_PATH = "C:/sp/devel/js7/keys/sp-truststore.p12";
    private static final String ALIAS_SP = "sp";
    private static final String ALIAS_ICLOUD = "icloud";
    private static final String ENV = "JS7_AGENT_CONFIG_DIR";

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientTests.class);
    private BaseHttpClient client;

    @Ignore
    @Test
    public void testReadKeyStore() throws Exception {
        KeyStore keyStore = KeyStoreUtil.readKeyStore(KEYSTORE_PATH, KeystoreType.PKCS12);
        assertNotNull(keyStore);
    }

    @Ignore
    @Test
    public void testReadCertificateFromKeyStore() {

        KeyStore keyStore = null;
        try {
            keyStore = KeyStoreUtil.readKeyStore(KEYSTORE_PATH, KeystoreType.PKCS12);
            X509Certificate cert = KeyStoreUtil.getX509CertificateFromKeyStore(keyStore, ALIAS_SP);
            assertNotNull(cert);
            CertificateUtils.logCertificateInfo(cert);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Ignore
    @Test
    public void testCheckLicenceValid() {
        KeyStore keyStore = null;
        try {
            keyStore = KeyStoreUtil.readKeyStore(CERTSTORE_PATH, KeystoreType.PKCS12);
            X509Certificate cert = KeyStoreUtil.getX509CertificateFromKeyStore(keyStore, ALIAS_ICLOUD);
            assertNotNull(cert);
            // not public API
            // assertTrue(CertificateUtils.checkLicence(cert));
            CertificateUtils.logCertificateInfo(cert);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Ignore
    @Test
    public void testCheckLicenceInvalid() {
        KeyStore keyStore = null;
        try {
            keyStore = KeyStoreUtil.readKeyStore(KEYSTORE_PATH, KeystoreType.PKCS12);
            X509Certificate cert = KeyStoreUtil.getX509CertificateFromKeyStore(keyStore, ALIAS_SP);
            assertNotNull(cert);
            // not public API
            // assertTrue(CertificateUtils.checkLicence(cert));
            CertificateUtils.logCertificateInfo(cert);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Ignore
    @Test
    public void testHttpClient() {
        try {
            createClient();
            HttpExecutionResult<String> result = client.executePOST(URI.create("http://localhost:4281/joc/api/security/login"));
            String response = result.response().body();
            String accessToken = result.response().headers().firstValue("X-Access-Token").orElse(null);
            LOGGER.info(accessToken);
            assertNotNull(response);
            if (accessToken != null) {
                result = client.executePOST(URI.create("http://localhost:4444/joc/api/security/logout"), Map.of("X-Access-Token",accessToken));
                response = result.response().body();
                LOGGER.info(response);
                assertNotNull(response);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Ignore
    @Test
    public void testReadCertificateChainFromKeyStore() {
        KeyStore keyStore = null;
        try {
            keyStore = KeyStoreUtil.readKeyStore(KEYSTORE_PATH, KeystoreType.PKCS12);
            Certificate[] certificateChain = keyStore.getCertificateChain(ALIAS_SP);
            if (certificateChain != null) {
                int count = 1;
                for (Certificate certificate : certificateChain) {
                    LOGGER.info("Certificate (" + count++ + ") CN: " + CertificateUtils.getCommonName((X509Certificate) certificate));
                    CertificateUtils.logCertificateInfo(((X509Certificate) certificate));
                }
            } else {
                LOGGER.info("No certificate chain present.");
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Ignore
    @Test
    public void testDecodeDisposition() throws Exception {
        String example1Disposition = "attachment; filename*=UTF-8''sos-%25232024-10-30%2523T24854243601-root-15286.order.log";
        String example2Disposition = "attachment; filename*=UTF-8''test-dpl-recursive2.zip";
        LOGGER.info(decodeDisposition(example1Disposition));
        LOGGER.info(decodeDisposition(example2Disposition));
    }

    @Ignore
    @Test
    public void readAgentConfigFolder() {
        LOGGER.info(System.getenv(ENV));
        LOGGER.info(System.getProperty(ENV));
    }

    private BaseHttpClient createClient() throws Exception {
        if(client == null) {
            BaseHttpClient.Builder clientBuilder = BaseHttpClient.withBuilder().withConnectTimeout(Duration.ofSeconds(60L)).withAuth("root", "root");
            SSLContext context = createSSLContext();
            if(context != null) {
                clientBuilder.withSSLContext(context);
            }
            client = clientBuilder.build();
        }
        return client;
    }
    
    private SSLContext createSSLContext () throws Exception {
        KeyManagerFactory keyFactory = null;
        TrustManagerFactory trustFactory = null;
        // C:\ProgramData\sos-berlin.com\js7\joc\270\jetty_base\resources\joc
        KeyStore keystore = KeyStoreUtil.readKeyStore(
                Paths.get(KEYSTORE_PATH), KeystoreType.PKCS12, "");
        if (keystore != null) {
            keyFactory = KeyManagerFactory.getInstance("PKIX");
            keyFactory.init(keystore, "".toCharArray());
        }
        KeyStore truststore = KeyStoreUtil.readTrustStore(
                Paths.get(TRUSTORE_PATH), KeystoreType.PKCS12, "");
        if (truststore != null) {
            trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustFactory.init(truststore);
        }
        if (keyFactory != null && trustFactory != null) {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyFactory.getKeyManagers(), trustFactory.getTrustManagers(), null);
            return sslContext;
        }
        return null;
    }
    
    public static String decodeDisposition(String disposition) throws UnsupportedEncodingException {
        String dispositionFilenameValue = disposition.replaceFirst("(?i)^.*filename(?:=\"?([^\"]+)\"?|\\*=([^;,]+)).*$", "$1$2");
        return decodeFromUriFormat(dispositionFilenameValue);
    }
    
    private static String decodeFromUriFormat(String parameter) throws UnsupportedEncodingException {
        final Pattern filenamePattern = Pattern.compile("(?<charset>[^']+)'(?<lang>[a-z]{2,8}(-[a-z0-9-]+)?)?'(?<filename>.+)",
                Pattern.CASE_INSENSITIVE);
        final Matcher matcher = filenamePattern.matcher(parameter);
        if (matcher.matches()) {
            final String filename = matcher.group("filename");
            final String charset = matcher.group("charset");
            return URLDecoder.decode(filename.replaceAll("%25", "%"), charset);
        } else {
            return parameter;
        }
    }
}
