package com.sos.auth.oidc;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthHelper;
import com.sos.commons.httpclient.BaseHttpClient;
import com.sos.commons.httpclient.commons.HttpExecutionResult;
//import com.sos.commons.httpclient.deprecated.SOSRestApiClient;
import com.sos.commons.httpclient.exception.SOSSSLException;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.http.HttpUtils;
import com.sos.commons.util.loggers.impl.SLF4JLogger;
import com.sos.joc.Globals;
import com.sos.joc.classes.SSLContext;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocInvalidResponseDataException;
import com.sos.joc.model.security.oidc.GetTokenRequest;
import com.sos.joc.model.security.oidc.GetTokenResponse;
import com.sos.joc.model.security.oidc.OpenIdConfiguration;
import com.sos.joc.model.security.properties.oidc.OidcProperties;

import jakarta.ws.rs.core.UriBuilder;

public class GetToken {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetToken.class);
    private UriBuilder uriBuilder;
    private Map<String, String> body = new HashMap<>();
    private KeyStore truststore;
    private BaseHttpClient client;
    private BaseHttpClient.Builder baseHttpClientBuilder;
    
    public GetToken(OidcProperties props, OpenIdConfiguration openIdConfigurationResponse, GetTokenRequest requestBody, String origin)
            throws Exception {
        setTrustStore(props);
        setUriBuilder(props, openIdConfigurationResponse, requestBody, origin);
    }
    
    public GetToken(OidcProperties props, OpenIdConfiguration openIdConfigurationResponse, GetTokenRequest requestBody, String origin,
            KeyStore truststore) throws SOSSSLException {
        this.truststore = truststore;
        setUriBuilder(props, openIdConfigurationResponse, requestBody, origin);
    }

    private void setUriBuilder(OidcProperties props, OpenIdConfiguration openIdConfigurationResponse, GetTokenRequest requestBody, String origin)
            throws SOSSSLException {
        if (!SOSString.isEmpty(props.getIamOidcClientSecret())) {
            List<String> supportedMethods = openIdConfigurationResponse.getToken_endpoint_auth_methods_supported();
            if (supportedMethods.contains("client_secret_basic")) {
           baseHttpClientBuilder.withAuth(props.getIamOidcClientId(), props.getIamOidcClientSecret());
                createBody(requestBody);
            } else { //if (supportedMethods.contains("client_secret_post")) {
                createBody(props, requestBody);
            }
        } else {
            createBody(props, requestBody);
        }
        setUriBuilder(openIdConfigurationResponse.getToken_endpoint(), origin);
    }
    
    private void createBody(GetTokenRequest requestBody) {
        body.put("grant_type", "authorization_code");
        body.put("code", requestBody.getCode());
        body.put("redirect_uri", requestBody.getRedirect_uri());
        body.put("code_verifier", requestBody.getCode_verifier());
    }
    
    private void createBody(OidcProperties props, GetTokenRequest requestBody) {
        createBody(requestBody);
        body.put("client_id", props.getIamOidcClientId());
        if (!SOSString.isEmpty(props.getIamOidcClientSecret())) {
            body.put("client_secret", props.getIamOidcClientSecret());
        }
    }

    private void setUriBuilder(String url, String origin) throws SOSSSLException {
        if (url == null) {
            throw new JocConfigurationException("The token endpoint of the OIDC provider is undefined.");
        }
        this.uriBuilder = UriBuilder.fromPath(url);
        init(origin, url);
    }

    public GetTokenResponse getJsonObjectFromPost() throws JocException {
		return getJsonObject(getJsonStringFromPost());
	}
    
    public String getJsonStringFromPost() throws JocException {
        try {
            return getJsonStringFromPost(uriBuilder.build());
        } catch (JocException e) {
            throw e;
        } catch (Exception e) {
            throw new JocBadRequestException(e);
        }
    }

    private String getJsonStringFromPost(URI uri) throws JocException {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Accept", "application/json");
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        JocError jocError = new JocError();
        jocError.appendMetaInfo("URL: " + uri.toString());
        try {
            createClient();
            HttpExecutionResult<String> result = client.executePOST(uri, headers, asBodyString(body));
            return getJsonStringFromResponse(result, uri, jocError);
        } catch (JocException e) {
            throw e;
        } catch (Exception e) {
            throw new JocBadRequestException(jocError, e);
        }
    }
    
    private GetTokenResponse getJsonObject(String jsonStr) throws JocInvalidResponseDataException {
        try {
            if (jsonStr == null) {
                return null;
            }
            return Globals.objectMapper.readValue(jsonStr, GetTokenResponse.class);
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
                    throw new JocInvalidResponseDataException(String.format("Unexpected content type '%1$s'. Response: %2$s", contentType,
                            response));
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

    private BaseHttpClient createClient() throws Exception {
        if(client == null) {
            client = baseHttpClientBuilder.build();
        }
        return client;
    }
    
    private void init(String origin, String url) throws ControllerConnectionRefusedException {
        if (url.startsWith("https:") && truststore == null) {
            throw new JocConfigurationException("Couldn't find required truststore");
        }
        baseHttpClientBuilder = BaseHttpClient.withBuilder().withConnectTimeout(Duration.ofMillis(Globals.httpConnectionTimeout))
                .withLogger(new SLF4JLogger(LOGGER)).withHeader("Origin", origin);
        if(truststore != null) {
            try {
                baseHttpClientBuilder.withSSLContext(SSLContext.createSslContext(truststore));
            } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
                throw new JocConfigurationException(e);
            }
        }
    }
    
    private String asBodyString(Map<String, String> params) {
        return params.entrySet().stream().map(entry -> {
            try {
                return URLEncoder.encode(entry.getKey(), "UTF-8") + "=" + URLEncoder.encode(entry.getValue(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new JocException(e);
            }
        }).collect(Collectors.joining("&"));
    }
    
}
