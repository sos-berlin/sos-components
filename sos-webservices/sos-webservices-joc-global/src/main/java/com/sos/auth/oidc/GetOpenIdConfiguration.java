package com.sos.auth.oidc;

import java.net.URI;
import java.security.KeyStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.classes.SOSAuthHelper;
import com.sos.commons.httpclient.deprecated.SOSRestApiClient;
import com.sos.commons.httpclient.exception.SOSSSLException;
import com.sos.joc.Globals;
import com.sos.joc.exceptions.ForcedClosingHttpClientException;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocInvalidResponseDataException;
import com.sos.joc.model.security.oidc.OpenIdConfiguration;
import com.sos.joc.model.security.properties.oidc.OidcProperties;

import jakarta.ws.rs.core.UriBuilder;

public class GetOpenIdConfiguration extends SOSRestApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetOpenIdConfiguration.class);
    private static final String API_PATH = "/.well-known/openid-configuration";
    private UriBuilder uriBuilder;
    private KeyStore truststore; 
    
    public GetOpenIdConfiguration(OidcProperties provider) throws Exception {
        setTrustStore(provider);
        setUriBuilder(provider.getIamOidcAuthenticationUrl());
    }
    
    public GetOpenIdConfiguration(OidcProperties provider, KeyStore truststore) throws SOSSSLException {
        this.truststore = truststore;
        setUriBuilder(provider.getIamOidcAuthenticationUrl());
    }
    
    private void setUriBuilder(String url) throws SOSSSLException {
        if (url == null) {
            throw new JocConfigurationException("The authentication URL of the OIDC provider is undefined.");
        }
        this.uriBuilder = UriBuilder.fromPath(url).path(API_PATH);
        setProperties(url);
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
        addHeader("Accept", "application/json");
        JocError jocError = new JocError();
        jocError.appendMetaInfo("URL: " + uri.toString());
        try {
            LOGGER.debug("REQUEST-URL:" + uri.toString());
            LOGGER.debug("REQUEST-HEADER:" + printHttpRequestHeaders());
            String response = getRestService(uri);
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

    private String getJsonStringFromResponse(String response, URI uri, JocError jocError) throws JocException {
        int httpReplyCode = statusCode();
        String contentType = getResponseHeader("Content-Type");
        if (response == null) {
            response = "";
        }
        LOGGER.debug("RESPONSE-HEADERS:" + printHttpResponseHeaders());
        LOGGER.debug("RESPONSE:" + response);
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
