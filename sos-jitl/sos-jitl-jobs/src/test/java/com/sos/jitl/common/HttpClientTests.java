package com.sos.jitl.common;

import static org.junit.Assert.assertNotNull;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.httpclient.deprecated.SOSRestApiClient;
import com.sos.commons.sign.keys.certificate.CertificateUtils;
import com.sos.commons.sign.keys.keyStore.KeyStoreUtil;
import com.sos.commons.sign.keys.keyStore.KeystoreType;
import com.sos.js7.job.jocapi.ApiExecutor;
import com.sos.js7.job.jocapi.ApiResponse;
import com.typesafe.config.Config;

public class HttpClientTests {

    private static final String CERTSTORE_PATH = "C:/sp/devel/js7/LicenceCheck/icloud-certstore.p12";
    private static final String KEYSTORE_PATH = "C:/sp/devel/js7/keys/sp-keystore.p12";
    private static final String TRUSTORE_PATH = "C:/sp/devel/js7/keys/sp-truststore.p12";
    private static final String ALIAS_SP = "sp";
    private static final String ALIAS_ICLOUD = "icloud";
    private static final String ENV = "JS7_AGENT_CONFIG_DIR";

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientTests.class);

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
        KeyStore keyStore = null;
        KeyStore truststore = null;
        try {
            keyStore = KeyStoreUtil.readKeyStore(KEYSTORE_PATH, KeystoreType.PKCS12);
            truststore = KeyStoreUtil.readTrustStore(TRUSTORE_PATH, KeystoreType.PKCS12);
            SOSRestApiClient restApiClient = new SOSRestApiClient();
            // restApiClient.setAutoCloseHttpClient(true);
            restApiClient.setSSLContext(keyStore, "".toCharArray(), truststore);
            String response = restApiClient.postRestService(URI.create("http://localhost:4444/joc/api/security/login"), null);

            String accessToken = restApiClient.getResponseHeader("X-Access-Token");
            LOGGER.info(accessToken);
            assertNotNull(response);
            if (accessToken != null) {
                restApiClient.addHeader("X-Access-Token", accessToken);
                response = restApiClient.postRestService(URI.create("http://localhost:4444/joc/api/security/logout"), null);
                LOGGER.info(response);
                assertNotNull(response);
            }
            restApiClient.closeHttpClient();
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
    public void readPrivateConf() throws SOSMissingDataException {
        Path privateConfPath = Paths.get(System.getProperty("user.dir")).resolve("src/test/resources");
        System.setProperty("js7.config-directory", privateConfPath.toString());
        System.setProperty("JS7_AGENT_CONFIG_DIR", privateConfPath.toString());
        ApiExecutor ex = new ApiExecutor(null);
        Config config = ex.readConfig();
        List<String> urls = config.getConfig("js7.api-server").getStringList("url");
        for (String uri : urls) {
            URI jocUri;
            try {
                jocUri = URI.create(uri);
                System.out.println(jocUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        assertNotNull(config);
    }

    @Ignore
    @Test
    public void testApiExecutorLogin() throws Exception {
        Path privateConfPath = Paths.get(System.getProperty("user.dir")).resolve("src/test/resources");
        System.setProperty("js7.config-directory", privateConfPath.toString());
        System.setProperty("JS7_AGENT_CONFIG_DIR", privateConfPath.toString());
        ApiExecutor ex = new ApiExecutor(null);
        String accessToken = ex.login().getAccessToken();
        ex.logout(accessToken);
    }

    @Ignore
    @Test
    public void testApiExecutorExport() throws Exception {
        Path privateConfPath = Paths.get(System.getProperty("user.dir")).resolve("src/test/resources");
        System.setProperty("js7.config-directory", privateConfPath.toString());
        System.setProperty("JS7_AGENT_CONFIG_DIR", privateConfPath.toString());
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("X-Outfile", Paths.get(System.getProperty("user.dir")).resolve("target/exported").resolve("export_calendars.zip").toString()
                .replace('\\', '/'));
        ApiExecutor ex = new ApiExecutor(null, headers);
        String accessToken = ex.login().getAccessToken();
        // String requestBody = "{\"useShortPath\": false, \"exportFile\": {\"filename\": \"export_calendars.zip\", \"format\": \"ZIP\"}, \"shallowCopy\":
        // {\"objectTypes\": [\"WORKINGDAYSCALENDAR\",\"NONWORKINGDAYSCALENDAR\"],\"folders\": [\"/Calendars\"],\"recursive\": true, \"onlyValidObjects\":
        // false, \"withoutDrafts\": false, \"withoutDeployed\": false, \"withoutReleased\": false}}";
        // ApiResponse response = ex.post(accessToken, "/inventory/export/folder", requestBody);
        LOGGER.info("File created!");
        ex.logout(accessToken);
    }

    @Ignore
    @Test
    public void testApiExecutorOrderLog() throws Exception {
        Path privateConfPath = Paths.get(System.getProperty("user.dir")).resolve("src/test/resources");
        System.setProperty("js7.config-directory", privateConfPath.toString());
        System.setProperty("JS7_AGENT_CONFIG_DIR", privateConfPath.toString());
        ApiExecutor ex = new ApiExecutor(null);
        String accessToken = ex.login().getAccessToken();
        String requestBody = "{\"controllerId\":\"controller_270\",\"historyId\":264}";
        ApiResponse response = ex.post(accessToken, "/order/log", requestBody);
        LOGGER.info("Order log:\n" + response.getResponseBody());
        ex.logout(accessToken);
    }

    @Ignore
    @Test
    public void testDecodeDisposition() throws Exception {
        String example1Disposition = "attachment; filename*=UTF-8''sos-%25232024-10-30%2523T24854243601-root-15286.order.log";
        String example2Disposition = "attachment; filename*=UTF-8''test-dpl-recursive2.zip";
        LOGGER.info(SOSRestApiClient.decodeDisposition(example1Disposition));
        LOGGER.info(SOSRestApiClient.decodeDisposition(example2Disposition));
    }

    @Ignore
    @Test
    public void readAgentConfigFolder() {
        LOGGER.info(System.getenv(ENV));
        LOGGER.info(System.getProperty(ENV));
    }

}
