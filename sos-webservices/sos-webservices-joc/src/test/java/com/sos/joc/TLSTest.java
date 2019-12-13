package com.sos.joc;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

import org.junit.Ignore;
import org.junit.Test;

import com.sos.commons.exception.SOSException;
import com.sos.commons.httpclient.SOSRestApiClient;

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

}
