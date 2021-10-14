package com.sos.joc;

import java.io.IOException;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.junit.Ignore;
import org.junit.Test;

import com.sos.commons.exception.SOSException;
import com.sos.commons.httpclient.SOSRestApiClient;
import com.sos.commons.sign.keys.keyStore.KeyStoreType;
import com.sos.commons.sign.keys.keyStore.KeyStoreUtil;

public class TLSTest {

    @Ignore
    @Test
    public void testClientCertificate() throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException,
            CertificateException, IOException, IllegalArgumentException, UriBuilderException, SOSException {
        SOSRestApiClient httpClient = new SOSRestApiClient();
        httpClient.setAllowAllHostnameVerifier(false);
        httpClient.setConnectionTimeout(5);
        httpClient.setSocketTimeout(20);
        httpClient.setClientCertificate("C:/Program Files/sos-berlin.com/jobscheduler/scheduler.2.0.1.oh/var/config/private/private-https.p12",
                "jobscheduler", "PKCS12", "jobscheduler");
        httpClient.addHeader("Accept", "application/json");
        String response = httpClient.getRestService(UriBuilder.fromPath("https://localhost:48420/agent/api").build());
        System.out.println(response);
    }

    @Ignore
    @Test
    public void testSSLConnection() throws Exception {
        SOSRestApiClient httpClient = new SOSRestApiClient();
        httpClient.setAllowAllHostnameVerifier(false);
        httpClient.setConnectionTimeout(60);
        httpClient.setSocketTimeout(20);
        System.setProperty("javax.net.debug", "ssl");
        httpClient.setSSLContext(createSSLContext());
        httpClient.addHeader("Accept", "application/json");
        String response = httpClient.getRestService(UriBuilder.fromPath("https://sp:11111/controller/api").build());
        System.out.println(response);
    }

    private javax.net.ssl.SSLContext createSSLContext () throws Exception {
        SSLContextBuilder sslContextBuilder = SSLContexts.custom();
        sslContextBuilder.setKeyManagerFactoryAlgorithm("PKIX");
        sslContextBuilder.setTrustManagerFactoryAlgorithm("PKIX");
        KeyStore keystore = KeyStoreUtil.readKeyStore(
                Paths.get("C:/Program Files/sos-berlin.com/js7/joc/jetty_base/resources/joc/https-keystore.p12"), KeyStoreType.PKCS12, "jobscheduler");
        if (keystore != null) {
            sslContextBuilder.loadKeyMaterial(keystore, "jobscheduler".toCharArray());
        }
        KeyStore truststore = KeyStoreUtil.readTrustStore(
                Paths.get("C:/Program Files/sos-berlin.com/js7/joc/jetty_base/resources/joc/https-truststore.p12"), KeyStoreType.PKCS12, "jobscheduler");
        if (truststore != null) {
            sslContextBuilder.loadTrustMaterial(truststore, null);
        }
        return sslContextBuilder.build();
    }
    
}
