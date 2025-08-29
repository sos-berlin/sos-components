package com.sos.auth.oidc;

import java.net.URI;
import java.security.KeyStore;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthHelper;
import com.sos.auth.classes.SOSLockerHelper;
import com.sos.commons.httpclient.deprecated.SOSRestApiClient;
import com.sos.commons.httpclient.exception.SOSSSLException;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.common.Variables;
import com.sos.joc.Globals;
import com.sos.joc.exceptions.ForcedClosingHttpClientException;
import com.sos.joc.exceptions.JocAuthenticationException;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocInvalidResponseDataException;
import com.sos.joc.model.security.locker.Locker;
import com.sos.joc.model.security.oidc.OpenIdConfiguration;
import com.sos.joc.model.security.properties.oidc.OidcProperties;

import jakarta.ws.rs.core.UriBuilder;

public class EndSession extends SOSRestApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(EndSession.class);
    private UriBuilder uriBuilder;
    private Map<String, String> body = new HashMap<>();
    private KeyStore truststore;
    private String httpMethod = "POST";
    
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
            
            addHeader("Origin", origin);
            
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
                        byte[] authEncBytes = org.apache.commons.codec.binary.Base64.encodeBase64(s.getBytes());
                        addHeader("Authorization", "Basic " + new String(authEncBytes));
//                        LOGGER.info("Authorization: Basic " + new String(authEncBytes));
                        
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

    private void setUriBuilder(String url) throws SOSSSLException {
        if (url == null) {
            throw new JocAuthenticationException("The end session endpoint of the OIDC provider is undefined.");
        }
        this.uriBuilder = UriBuilder.fromPath(url);
        setProperties(url);
    }
    
    private void setUriBuilder(String url, String param) throws SOSSSLException {
        if (url == null) {
            throw new JocAuthenticationException("The end session endpoint of the OIDC provider is undefined.");
        }
        this.uriBuilder = UriBuilder.fromPath(url);
        uriBuilder.queryParam("post_logout_redirect_uri", param);
        setProperties(url);
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
    
//    private String getJsonStringFromGet(URI uri) throws JocException {
//        addHeader("Accept", "application/json, text/plain, */*");
//        JocError jocError = new JocError();
//        jocError.appendMetaInfo("URL: " + uri.toString());
//        try {
//            LOGGER.info("REQUEST-URL:" + uri.toString());
//            LOGGER.info("REQUEST-HEADER:" + printHttpRequestHeaders());
//            String response = getRestService(uri);
//            return getJsonStringFromResponse(response, uri, jocError);
//        } catch (JocException e) {
//            throw e;
//        } catch (Exception e) {
//            if (isForcedClosingHttpClient()) {
//                throw new ForcedClosingHttpClientException(uri.getScheme()+"://"+uri.getAuthority(), e);
//            } else {
//                throw new JocBadRequestException(jocError, e);
//            }
//        }
//    }

    private String getStringFromPost(URI uri) throws JocException {
        addHeader("Accept", "application/json, text/plain, */*");
        addHeader("Content-Type", "application/x-www-form-urlencoded");
        JocError jocError = new JocError();
        jocError.appendMetaInfo("URL: " + uri.toString());
        try {
            LOGGER.debug("REQUEST-URL:" + uri.toString());
            LOGGER.debug("REQUEST-HEADER:" + printHttpRequestHeaders());
            LOGGER.debug("REQUEST-BODY:" + body.toString());
            String response = postRestService(uri, body);
            return getJsonStringFromResponse(response, uri, jocError);
        } catch (JocException e) {
            throw e;
        } catch (Exception e) {
            if (isForcedClosingHttpClient()) {
                throw new ForcedClosingHttpClientException(uri.getScheme()+"://"+uri.getAuthority(), e);
            } else {
                throw new JocBadRequestException(jocError, e);
            }
        }
    }
    
    private void setProperties(String url) throws SOSSSLException {
        setAllowAllHostnameVerifier(!Globals.withHostnameVerification);
        setConnectionTimeout(Globals.httpConnectionTimeout);
        setSocketTimeout(Globals.httpSocketTimeout);
        setSSLContext(null, null, truststore);
        if (url.startsWith("https:") && truststore == null) {
            throw new JocConfigurationException("Couldn't find required truststore");
        }
    }
    
//	private GetTokenResponse getJsonObject(String jsonStr) throws JocInvalidResponseDataException {
//		try {
//			if (jsonStr == null) {
//				return null;
//			}
//			return Globals.objectMapper.readValue(jsonStr, GetTokenResponse.class);
//		} catch (Exception e) {
//			throw new JocInvalidResponseDataException(e);
//		}
//	}

    private String getJsonStringFromResponse(String response, URI uri, JocError jocError) throws JocException {
        int httpReplyCode = statusCode();
//        String contentType = getResponseHeader("Content-Type");
        if (response == null) {
            response = "";
        }
        LOGGER.debug("RESPONSE-HEADERS:" + printHttpResponseHeaders());
        LOGGER.debug("RESPONSE:" + response);
        try {
            switch (httpReplyCode) {
            case 200:
//                if (contentType.startsWith("application/") && contentType.contains("json")) {
                    if (response.isEmpty()) {
                        throw new JocInvalidResponseDataException("Unexpected empty response");
                    }
                    return response;
//                } else {
//                    throw new JocInvalidResponseDataException(String.format("Unexpected content type '%1$s'. Response: %2$s", contentType,
//                            response));
//                }
            default:
                throw new JocBadRequestException(httpReplyCode + " " + getHttpResponse().getStatusLine().getReasonPhrase());
            }
        } catch (JocException e) {
            e.addErrorMetaInfo(jocError);
            throw e;
        }
    }
    
    private void setTrustStore(OidcProperties provider) throws Exception {
        truststore = SOSAuthHelper.getOIDCTrustStore(provider);
    }
}
