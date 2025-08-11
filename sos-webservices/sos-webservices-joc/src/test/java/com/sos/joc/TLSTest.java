package com.sos.joc;

import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.junit.Ignore;
import org.junit.Test;

import com.sos.commons.httpclient.BaseHttpClient;
import com.sos.commons.httpclient.commons.HttpExecutionResult;
import com.sos.commons.sign.keys.keyStore.KeyStoreUtil;
import com.sos.commons.sign.keys.keyStore.KeystoreType;

import jakarta.ws.rs.core.UriBuilder;

public class TLSTest {
    
    private BaseHttpClient client;

    @Ignore
    @Test
    public void testClientCertificate() throws Exception {
        createClient();
        //httpClient.setClientCertificate("C:/Program Files/sos-berlin.com/jobscheduler/scheduler.2.0.1.oh/var/config/private/private-https.p12",
        //        "jobscheduler", null, "PKCS12", "jobscheduler");
        Map<String,String> headers = new HashMap<String, String>();
        headers.put("Accept", "application/json");
        HttpExecutionResult<String> result = client.executeGET(UriBuilder.fromPath("https://localhost:48420/agent/api").build(),
                headers, BodyHandlers.ofString(StandardCharsets.UTF_8));
        String response = result.response().body();
        System.out.println(response);
    }

    @Ignore
    @Test
    public void testSSLConnection() throws Exception {
        createClient();
        System.setProperty("javax.net.debug", "ssl");
        Map<String,String> headers = new HashMap<String, String>();
        headers.put("Accept", "application/json");
        HttpExecutionResult<String> result = client.executeGET(UriBuilder.fromPath("https://sp:11111/controller/api").build(),
                headers, BodyHandlers.ofString(StandardCharsets.UTF_8));
        String response = result.response().body();
        System.out.println(response);
    }

    private SSLContext createSSLContext () throws Exception {
        KeyManagerFactory keyFactory = null;
        TrustManagerFactory trustFactory = null;
        // C:\ProgramData\sos-berlin.com\js7\joc\270\jetty_base\resources\joc
        KeyStore keystore = KeyStoreUtil.readKeyStore(
                Paths.get("C:/ProgramData/sos-berlin.com/js7/joc/270/jetty_base/resources/joc/ec_keystore.p12"), KeystoreType.PKCS12, "jobscheduler");
        if (keystore != null) {
            keyFactory = KeyManagerFactory.getInstance("PKIX");
            keyFactory.init(keystore, "jobscheduler".toCharArray());
        }
        KeyStore truststore = KeyStoreUtil.readTrustStore(
                Paths.get("C:/ProgramData/sos-berlin.com/js7/joc/270/jetty_base/resources/joc/ec_truststore.p12"), KeystoreType.PKCS12, "");
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
    
    private BaseHttpClient createClient() throws Exception {
        if(client == null) {
            BaseHttpClient.Builder clientBuilder = BaseHttpClient.withBuilder().withConnectTimeout(Duration.ofSeconds(60L));
            SSLContext context = createSSLContext();
            if(context != null) {
                clientBuilder.withSSLContext(context);
            }
            client = clientBuilder.build();
        }
        return client;
    }
    
}
