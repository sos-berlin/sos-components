package com.sos.auth.oidc;

import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthHelper;
import com.sos.auth.classes.SOSLockerHelper;
import com.sos.commons.httpclient.BaseHttpClient;
import com.sos.commons.httpclient.commons.HttpExecutionResult;
import com.sos.commons.httpclient.exception.SOSSSLException;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.http.HttpUtils;
import com.sos.commons.util.loggers.impl.SLF4JLogger;
import com.sos.inventory.model.common.Variables;
import com.sos.joc.Globals;
import com.sos.joc.classes.SSLContext;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;
import com.sos.joc.exceptions.JocAuthenticationException;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocInvalidResponseDataException;
import com.sos.joc.model.security.locker.Locker;
import com.sos.joc.model.security.oidc.OpenIdConfiguration;
import com.sos.joc.model.security.properties.oidc.OidcProperties;

import jakarta.ws.rs.core.UriBuilder;

public class EndSession {

    private static final Logger LOGGER = LoggerFactory.getLogger(EndSession.class);
    private UriBuilder uriBuilder;
    private Map<String, String> body = new HashMap<>();
    private KeyStore truststore;
    private String httpMethod = "POST";
    private BaseHttpClient client;
    private BaseHttpClient.Builder baseHttpClientBuilder;
    private Map<String, String> additionalHeaders;
    
    public EndSession(OidcProperties props, OpenIdConfiguration openIdConfigurationResponse, String lockerKey, String origin, String referrer)
            throws Exception {
        setTrustStore(props);
        setUriBuilder(props, openIdConfigurationResponse, lockerKey, origin, referrer);
    }

    public EndSession(OidcProperties props, OpenIdConfiguration openIdConfigurationResponse, String lockerKey, String origin, String referrer,
            KeyStore truststore) throws SOSSSLException {
        this.truststore = truststore;
        setUriBuilder(props, openIdConfigurationResponse, lockerKey, origin, referrer);
    }

    private void setUriBuilder(OidcProperties props, OpenIdConfiguration openIdConfigurationResponse, String lockerKey, String origin, String referrer)
            throws SOSSSLException {
        try {
            Locker locker = SOSLockerHelper.lockerGet(lockerKey);
            Map<String, Object> loginProps = Optional.ofNullable(locker).map(Locker::getContent).map(Variables::getAdditionalProperties).orElse(
                    Collections.emptyMap());
            String token = (String) loginProps.get("token");
            String clientId = (String) loginProps.get("clientId");
            String clientSecret = (String) loginProps.get("clientSecret");
            String endSessionEndPoint = (String) loginProps.get("endSessionEndPoint");
            if(additionalHeaders == null) {
                additionalHeaders = new HashMap<String, String>(3);
            }
            additionalHeaders.put("Origin", origin);
            
            if (endSessionEndPoint.contains("login.windows.net") || endSessionEndPoint.contains("login.microsoftonline.com")) {
                httpMethod = "GET";
                setUriBuilder(endSessionEndPoint, referrer);
            } else {
                httpMethod = "POST";
                if (token == null || clientId == null) {
                    throw new JocAuthenticationException("Incomplete data to close session at OIDC identity service: " + props.getIamOidcName());
                }
                if (!SOSString.isEmpty(clientSecret)) {
                    List<String> supportedMethods = openIdConfigurationResponse.getToken_endpoint_auth_methods_supported();
                    if (supportedMethods.contains("client_secret_basic")) {
                        String s = clientId + ":" + clientSecret;
                        String authEncBytes = Base64.getEncoder().encodeToString(s.getBytes());
                        additionalHeaders.put("Authorization", "Basic " + authEncBytes);
                        additionalHeaders.put(lockerKey, referrer);
                        createBody(token, referrer);
                        
                    } else { //if (supportedMethods.contains("client_secret_post")) {
                        createBody(clientId, clientSecret, token, referrer);
                    }
                } else {
                    createBody(clientId, clientSecret, token, referrer);
                }
                setUriBuilder(endSessionEndPoint);
            }
        } catch (JocAuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new JocAuthenticationException("Incomplete data to close session at OIDC identity service: " + props.getIamOidcName(), e);
        } 
    }
    
    private void createBody(String token, String referrer) {
        body.put("token_type_hint", "access_token");
        body.put("token", token);
        body.put("redirect_uri", referrer);
    }
    
    private void createBody(String clientId, String clientSecret, String token, String referrer) {
        createBody(token, referrer);
        body.put("client_id", clientId);
        if (!SOSString.isEmpty(clientSecret)) {
            body.put("client_secret", clientSecret);
        }
    }

    private void setUriBuilder(String url) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
        if (url == null) {
            throw new JocAuthenticationException("The end session endpoint of the OIDC provider is undefined.");
        }
        this.uriBuilder = UriBuilder.fromPath(url);
        init(url);
    }
    
    private void setUriBuilder(String url, String param) throws SOSSSLException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
        if (url == null) {
            throw new JocAuthenticationException("The end session endpoint of the OIDC provider is undefined.");
        }
        this.uriBuilder = UriBuilder.fromPath(url);
        uriBuilder.queryParam("post_logout_redirect_uri", param);
        init(url);
    }

    public String getStringResponse() throws JocException {
        try {
            if (httpMethod.equals("POST")) {
                return getStringFromPost(uriBuilder.build()); 
            } else {
                // Do nothing, doesn't work
                return "";
                //return getJsonStringFromGet(uriBuilder.build());
            }
        } catch (JocException e) {
            throw e;
        } catch (Exception e) {
            throw new JocBadRequestException(e);
        }
    }
    
    private String getStringFromPost(URI uri) throws JocException {
        Map<String,String> headers = new HashMap<String, String>();
        headers.put("Accept", "application/json, text/plain, */*");
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.putAll(additionalHeaders);
        JocError jocError = new JocError();
        jocError.appendMetaInfo("URL: " + uri.toString());
        try {
            createClient();
            HttpExecutionResult<String> result = client.executePOST(uri, headers, HttpUtils.createUrlEncodedBodyfromMap(body));
            return getJsonStringFromResponse(result, uri, jocError);
        } catch (JocException e) {
            throw e;
        } catch (Exception e) {
            throw new JocBadRequestException(jocError, e);
        }
    }
    
    private String getJsonStringFromResponse(HttpExecutionResult<String> result, URI uri, JocError jocError) throws JocException {
        int httpReplyCode = result.response().statusCode();
        String response = result.response().body();
        if (response == null) {
            response = "";
        }
        try {
            switch (httpReplyCode) {
            case 200:
                if (response.isEmpty()) {
                    throw new JocInvalidResponseDataException("Unexpected empty response");
                }
                return response;
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
