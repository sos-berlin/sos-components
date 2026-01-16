package com.sos.auth.oidc;

import java.net.URI;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpTimeoutException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthHelper;
import com.sos.commons.httpclient.BaseHttpClient;
import com.sos.commons.httpclient.commons.HttpExecutionResult;
import com.sos.commons.httpclient.exception.SOSSSLException;
import com.sos.commons.util.http.HttpUtils;
import com.sos.commons.util.loggers.impl.SLF4JLogger;
import com.sos.joc.Globals;
import com.sos.joc.classes.SSLContext;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;
import com.sos.joc.exceptions.ControllerNoResponseException;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocInvalidResponseDataException;
import com.sos.joc.model.security.oidc.OpenIdConfiguration;
import com.sos.joc.model.security.properties.oidc.OidcProperties;

import jakarta.ws.rs.core.UriBuilder;

public class GetOpenIdConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetOpenIdConfiguration.class);
    private static final String API_PATH = "/.well-known/openid-configuration";
    private UriBuilder uriBuilder;
    private KeyStore truststore; 
    private BaseHttpClient client;
    private BaseHttpClient.Builder baseHttpClientBuilder;
    
    public GetOpenIdConfiguration(OidcProperties provider) throws Exception {
        setTrustStore(provider);
        setUriBuilder(provider.getIamOidcAuthenticationUrl());
    }
    
    public GetOpenIdConfiguration(OidcProperties provider, KeyStore truststore) throws SOSSSLException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
        this.truststore = truststore;
        setUriBuilder(provider.getIamOidcAuthenticationUrl());
    }
    
    private void setUriBuilder(String url) throws SOSSSLException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
        if (url == null) {
            throw new JocConfigurationException("The authentication URL of the OIDC provider is undefined.");
        }
        this.uriBuilder = UriBuilder.fromPath(url).path(API_PATH);
        init(url);
    }

    public OpenIdConfiguration getJsonObjectFromGet() throws JocException {
		return getJsonObject(getJsonStringFromGet());
	}
    
    public String getJsonStringFromGet() throws JocException {
        try {
            return getJsonStringFromGet(uriBuilder.build());
        } catch (JocException e) {
            throw e;
        } catch (Exception e) {
            throw new JocBadRequestException(e);
        }
    }

    private String getJsonStringFromGet(URI uri) throws JocException {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", "application/json");
        JocError jocError = new JocError();
        jocError.appendMetaInfo("URL: " + uri.toString());
        try {
            createClient();
            HttpExecutionResult<String> result = client.executeGET(uri, headers);
            return getJsonStringFromResponse(result, uri, jocError);
        } catch (HttpConnectTimeoutException e) {
            throw new ControllerConnectionRefusedException("", e);
        } catch (HttpTimeoutException e) {
            throw new ControllerNoResponseException("", e);
        } catch (JocException e) {
            throw e;
        } catch (Exception e) {
            throw new JocBadRequestException(jocError, e);
        }
    }
    
    private OpenIdConfiguration getJsonObject(String jsonStr) throws JocInvalidResponseDataException {
        try {
            if (jsonStr == null) {
                return null;
            }
            return Globals.objectMapper.readValue(jsonStr, OpenIdConfiguration.class);
        } catch (Exception e) {
            throw new JocInvalidResponseDataException(e);
        }
    }

    private String getJsonStringFromResponse(HttpExecutionResult<String> result, URI uri, JocError jocError) throws JocException {
        int httpReplyCode = result.response().statusCode();
        String contentType = result.response().headers().firstValue("Content-Type").orElse("");
        String response = result.response().body();
        if (response == null) {
            response = "";
        }
        try {
            switch (httpReplyCode) {
            case 200:
                if (contentType.startsWith("application/") && contentType.contains("json")) {
                    if (response.isEmpty()) {
                        throw new JocInvalidResponseDataException("Unexpected empty response");
                    }
                    return response;
                } else {
                    throw new JocInvalidResponseDataException(
                            String.format("Unexpected content type '%1$s'. Response: %2$s", contentType, response));
                }
            default:
                throw new JocBadRequestException(httpReplyCode + " " + HttpUtils.getReasonPhrase(httpReplyCode));
            }
        } catch (JocException e) {
            e.addErrorMetaInfo(jocError);
            throw e;
        }
    }
    
    private void setTrustStore(OidcProperties provider) throws Exception {
        truststore = SOSAuthHelper.getOIDCTrustStore(provider);
    }

    private void init(String url) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
        baseHttpClientBuilder = BaseHttpClient.withBuilder().withConnectTimeout(Duration.ofMillis(Globals.httpConnectionTimeout))
                .withLogger(new SLF4JLogger(LOGGER));
        if(truststore != null) {
            baseHttpClientBuilder.withSSLContext(SSLContext.createSslContext(truststore));
        }
        if (url.startsWith("https:") && truststore == null) {
            throw new ControllerConnectionRefusedException("Couldn't find required truststore");
        }
    }
    
    private BaseHttpClient createClient() throws Exception {
        if(client == null) {
            client = baseHttpClientBuilder.build();
        }
        return client;
    }
    
}
