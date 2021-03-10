package com.sos.js7.joc.poc.jetty;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc2.poc.jetty.server.JettyServer;
import com.sos.js7.joc.poc.jetty.server.JettyTestServer;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestEmbeddedJetty {

    private JettyTestServer testServer;
    private JettyServer server;
    private static final Logger LOGGER = LoggerFactory.getLogger(TestEmbeddedJetty.class);
    private static final String SERVER_KEYSTORE_PATH = "src/test/resources/https-keystore.p12";
    private static final String SERVER_KEYSTORE_TYPE = "PKCS12";
    private static final String SERVER_KEYSTORE_PW = "jobscheduler";
    private static final String SERVER_KEYSTORE_KEYMAN_PW = "jobscheduler";
    private static final String SERVER_TRUSTSTORE_PATH = "src/test/resources/https-truststore.p12";
    private static final String SERVER_TRUSTSTORE_TYPE = "PKCS12";
    private static final String SERVER_TRUSTORE_PW = "jobscheduler";
    private static final String CLIENT_KEYSTORE_PATH = "src/test/resources/sp-https-keystore.p12";
    private static final String CLIENT_KEYSTORE_TYPE = "PKCS12";
    private static final String CLIENT_KEYSTORE_PW = "";
    private static final String CLIENT_KEYSTORE_KEYMAN_PW = "";
    private static final String CLIENT_TRUSTSTORE_PATH = "src/test/resources/sp-https-keystore.p12";
    private static final String CLIENT_TRUSTSTORE_TYPE = "PKCS12";
    private static final String CLIENT_TRUSTORE_PW = "jobscheduler";

    @Before
    public void setup() throws Exception {
        // start testServer for src/test/java tests only (01, 02)
        // start server for src/main/java tests (03)

        // testServer = new JettyTestServer();
        // testServer.start();

        server = new JettyServer();
        server.init(SERVER_KEYSTORE_PATH, SERVER_KEYSTORE_TYPE, SERVER_KEYSTORE_PW, SERVER_KEYSTORE_KEYMAN_PW, SERVER_TRUSTSTORE_PATH, SERVER_TRUSTSTORE_TYPE, SERVER_TRUSTORE_PW);
        server.start();
    }

    @After
    public void close() throws Exception {
        if (testServer != null) {
            testServer.stop();
        }
        if (server != null) {
            server.stop();
        }
    }

    // @Test
    public void test01JettyBlockingServlet() throws ClientProtocolException, IOException {
        // single threaded Servlet
        // will wait until request is finished
        // takes next request after previous is finished
        String url = "http://localhost:8010/status";
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);

        HttpResponse response = client.execute(request);
        LOGGER.debug(String.format("Response Status Code: %1$s", response.getStatusLine().getStatusCode()));
        LOGGER.debug(String.format("Response Content: %1$s", IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8.toString())));
        assertEquals(response.getStatusLine().getStatusCode(), 200);
    }

    // @Test
    public void test02JettyAsnycServlet() throws ClientProtocolException, IOException {
        // multi threaded Servlet
        // will respond after request is finished
        // takes requests parallel
        String url = "http://localhost:8010/heavy/async";
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);

        HttpResponse response = client.execute(request);
        assertEquals(response.getStatusLine().getStatusCode(), 200);

        String responseContent = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.ISO_8859_1.toString());
        assertEquals(responseContent, "This is some heavy resource that will be served in an async way");
    }

    @Test
    public void test03JettyServerHTTPTest() {
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet("http://sp.sos:8900/status80");
            HttpResponse response = client.execute(request);
            assertEquals(response.getStatusLine().getStatusCode(),200);
            String responseContent = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8.toString());
            LOGGER.info(responseContent);
            assertNotNull(responseContent);
        } catch (UnsupportedOperationException | IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Test
    @Ignore
    public void test04JettyServerWithSSLTest() {
        try {
            // use null as second param if you don't have a separate key password
            SSLContext sslContext = SSLContexts.custom()
                    .loadKeyMaterial(readKeyStore(), "sp".toCharArray())
                    .setKeyStoreType("PKCS12").loadTrustMaterial(readTrustStore(), null).build();
            HttpClient httpClient = HttpClients.custom().setSSLContext(sslContext).setRetryHandler(new DefaultHttpRequestRetryHandler(0, false)).build();
            HttpResponse response = httpClient.execute(new HttpGet("https://sp.sos:8910/status"));
            assertEquals(200, response.getStatusLine().getStatusCode());
            HttpEntity entity = response.getEntity();
            System.out.println("----------------------------------------");
            System.out.println(response.getStatusLine());
            EntityUtils.consume(entity);
            // HttpResponse response = client.execute(request);
            // assertEquals(response.getStatusLine().getStatusCode(),200);

            String responseContent = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.ISO_8859_1.toString());
            // assertEquals(responseContent,"This is some heavy resource that will be served in an async way");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private KeyStore readKeyStore() throws Exception {
        InputStream keyStoreStream = null;
        try {
            keyStoreStream = this.getClass().getResourceAsStream("/sp-keystore.p12");
            KeyStore keyStore = KeyStore.getInstance("PKCS12"); // or "JKS"
            keyStore.load(keyStoreStream, "".toCharArray());
            return keyStore;
        } finally {
            if (keyStoreStream != null) {
                keyStoreStream.close();
            }
        }
    }

    private KeyStore readTrustStore() throws Exception {
        InputStream trustStoreStream = null;
        try {
            trustStoreStream = this.getClass().getResourceAsStream("/https-truststore.p12");
            KeyStore trustStore = KeyStore.getInstance("PKCS12"); // or "JKS"
            trustStore.load(trustStoreStream, "jobscheduler".toCharArray());
            return trustStore;
        } finally {
            if (trustStoreStream != null) {
                trustStoreStream.close();
            }
        }
    }

}
