package com.sos.auth.vault;

import java.io.IOException;
import java.net.SocketException;
import java.net.URI;
import java.security.KeyStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.auth.vault.classes.SOSVaultAccountAccessToken;
import com.sos.auth.vault.classes.SOSVaultAccountCredentials;
import com.sos.auth.vault.classes.SOSVaultWebserviceCredentials;
import com.sos.auth.vault.pojo.sys.auth.SOSVaultAuthenticationMethods;
import com.sos.auth.vault.pojo.sys.auth.SOSVaultCheckAccessTokenResponse;
import com.sos.commons.exception.SOSException;
import com.sos.commons.httpclient.SOSRestApiClient;
import com.sos.joc.Globals;

public class SOSVaultHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSVaultHandler.class);
    private KeyStore keyStore = null;
    private KeyStore truststore = null;
    private SOSVaultWebserviceCredentials webserviceCredentials;

    public SOSVaultHandler(SOSVaultWebserviceCredentials webserviceCredentials, KeyStore keyStore, KeyStore trustStore) {
        this.keyStore = keyStore;
        this.truststore = trustStore;
        this.webserviceCredentials = webserviceCredentials;
    }

    public String getVaultStatus() throws SOSException, SocketException {
        SOSRestApiClient restApiClient = new SOSRestApiClient();
        restApiClient.addHeader("X-Vault-Token", webserviceCredentials.getApplicationToken());
        if ((keyStore != null) || (truststore != null)) {
            restApiClient.setSSLContext(keyStore, webserviceCredentials.getKeyPassword().toCharArray(), truststore);
        }
        URI requestUri = URI.create(webserviceCredentials + "/v1/sys/init");
        String response = restApiClient.getRestService(requestUri);

        restApiClient.closeHttpClient();
        return response;
    }

    public SOSVaultAuthenticationMethods getVaultAuthenticationMethods() throws SOSException, JsonParseException, JsonMappingException, IOException {

        SOSRestApiClient restApiClient = new SOSRestApiClient();
        restApiClient.addHeader("X-Vault-Token", webserviceCredentials.getApplicationToken());
        if ((keyStore != null) || (truststore != null)) {
            restApiClient.setSSLContext(keyStore, webserviceCredentials.getKeyPassword().toCharArray(), truststore);
        }
        URI requestUri = URI.create(webserviceCredentials.getServiceUrl() + "/v1/sys/auth");
        String response = restApiClient.getRestService(requestUri);
        LOGGER.debug(response);

        SOSVaultAuthenticationMethods sosVaultRoot = Globals.objectMapper.readValue(response, SOSVaultAuthenticationMethods.class);

        restApiClient.closeHttpClient();
        return sosVaultRoot;
    }

    public String storeAccountPassword(SOSVaultAccountCredentials sosVaultUserCredentials) throws SOSException, JsonParseException,
            JsonMappingException, IOException {

        SOSRestApiClient restApiClient = new SOSRestApiClient();
        restApiClient.addHeader("X-Vault-Token", webserviceCredentials.getApplicationToken());
        if ((keyStore != null) || (truststore != null)) {
            restApiClient.setSSLContext(keyStore, webserviceCredentials.getKeyPassword().toCharArray(), truststore);
        }
        URI requestUri = URI.create(webserviceCredentials.getServiceUrl() + "/v1/auth/userpass/users/" + sosVaultUserCredentials.getAccount());
        String body = Globals.objectMapper.writeValueAsString(sosVaultUserCredentials);
        String response = restApiClient.postRestService(requestUri, body);
        LOGGER.debug(response);

        restApiClient.closeHttpClient();
        return response;
    }

    public SOSVaultAccountAccessToken login() throws SOSException, JsonParseException, JsonMappingException, IOException {

        SOSRestApiClient restApiClient = new SOSRestApiClient();
        restApiClient.addHeader("X-Vault-Token", webserviceCredentials.getApplicationToken());
        if ((keyStore != null) || (truststore != null)) {
            restApiClient.setSSLContext(keyStore, webserviceCredentials.getKeyPassword().toCharArray(), truststore);
        }

        SOSVaultAccountCredentials sosVaultAccountCredentials = new SOSVaultAccountCredentials();
        sosVaultAccountCredentials.setAccount(webserviceCredentials.getAccount());
        sosVaultAccountCredentials.setPassword(webserviceCredentials.getPassword());
        URI requestUri = URI.create(webserviceCredentials.getServiceUrl() + "/v1/auth/userpass/login/" + sosVaultAccountCredentials.getAccount());
        String body = Globals.objectMapper.writeValueAsString(sosVaultAccountCredentials);
        String response = restApiClient.postRestService(requestUri, body);
        LOGGER.debug(response);

        SOSVaultAccountAccessToken sosVaultUserAccessToken = Globals.objectMapper.readValue(response, SOSVaultAccountAccessToken.class);

        restApiClient.closeHttpClient();
        return sosVaultUserAccessToken;
    }

    public boolean accountAccessTokenIsValid(SOSVaultAccountAccessToken sosVaultAccountAccessToken) throws SOSException, JsonParseException,
            JsonMappingException, IOException {

        SOSRestApiClient restApiClient = new SOSRestApiClient();
        restApiClient.addHeader("X-Vault-Token", webserviceCredentials.getApplicationToken());
        if ((keyStore != null) || (truststore != null)) {
            restApiClient.setSSLContext(keyStore, webserviceCredentials.getKeyPassword().toCharArray(), truststore);
        }
        URI requestUri = URI.create(webserviceCredentials.getServiceUrl() + "/v1/auth/token/lookup");
        String body = "{\"token\":\"" + sosVaultAccountAccessToken.getAuth().getClient_token() + "\"}";
        String response = restApiClient.postRestService(requestUri, body);
        LOGGER.debug(response);

        SOSVaultCheckAccessTokenResponse sosVaultCheckAccessTokenResponse = Globals.objectMapper.readValue(response,
                SOSVaultCheckAccessTokenResponse.class);

        restApiClient.closeHttpClient();
        return (sosVaultCheckAccessTokenResponse.errors == null || sosVaultCheckAccessTokenResponse.errors.size() == 0);
    }

    public void renewAccountAccess(SOSVaultAccountAccessToken sosVaultAccountAccessToken) throws SOSException, JsonParseException,
            JsonMappingException, IOException {
        if (sosVaultAccountAccessToken != null && sosVaultAccountAccessToken.getAuth().isRenewable()) {
            SOSRestApiClient restApiClient = new SOSRestApiClient();
            restApiClient.addHeader("X-Vault-Token", webserviceCredentials.getApplicationToken());
            if ((keyStore != null) || (truststore != null)) {
                restApiClient.setSSLContext(keyStore, webserviceCredentials.getKeyPassword().toCharArray(), truststore);
            }
            URI requestUri = URI.create(webserviceCredentials.getServiceUrl() + "/v1/auth/token/renew");
            String body = "{\"token\":\"" + sosVaultAccountAccessToken.getAuth().getClient_token() + "\"}";
            String response = restApiClient.postRestService(requestUri, body);
            LOGGER.debug(response);
            restApiClient.closeHttpClient();
        }
    }

}
