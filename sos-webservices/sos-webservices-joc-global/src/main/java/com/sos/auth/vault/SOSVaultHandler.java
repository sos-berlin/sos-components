package com.sos.auth.vault;

import java.io.IOException;
import java.net.SocketException;
import java.net.URI;
import java.security.KeyStore;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.auth.vault.classes.SOSVaultAccountAccessToken;
import com.sos.auth.vault.classes.SOSVaultAccountCredentials;
import com.sos.auth.vault.classes.SOSVaultStoreUser;
import com.sos.auth.vault.classes.SOSVaultUpdatePolicies;
import com.sos.auth.vault.classes.SOSVaultWebserviceCredentials;
import com.sos.auth.vault.pojo.sys.auth.SOSVaultAuthenticationMethods;
import com.sos.auth.vault.pojo.sys.auth.SOSVaultCheckAccessTokenResponse;
import com.sos.commons.exception.SOSException;
import com.sos.commons.httpclient.SOSRestApiClient;
import com.sos.commons.httpclient.exception.SOSSSLException;
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

    public String storeAccountPassword(SOSVaultAccountCredentials sosVaultAccountCredentials, String password) throws SOSException,
            JsonParseException, JsonMappingException, IOException {

        SOSRestApiClient restApiClient = new SOSRestApiClient();
        restApiClient.addHeader("X-Vault-Token", webserviceCredentials.getApplicationToken());
        if ((keyStore != null) || (truststore != null)) {
            restApiClient.setSSLContext(keyStore, webserviceCredentials.getKeyPassword().toCharArray(), truststore);
        }
        URI requestUri = URI.create(webserviceCredentials.getServiceUrl() + "/v1/auth/userpass/users/" + sosVaultAccountCredentials.getUsername());
        SOSVaultStoreUser sosVaultStoreUser = new SOSVaultStoreUser();
        sosVaultStoreUser.setUsername(sosVaultAccountCredentials.getUsername());
        sosVaultStoreUser.setPassword(password);
        String body = Globals.objectMapper.writeValueAsString(sosVaultStoreUser);
        sosVaultAccountCredentials.setPassword("");
        String response = restApiClient.postRestService(requestUri, body);

        LOGGER.debug(response);

        restApiClient.closeHttpClient();
        return response;
    }

    public SOSVaultAccountAccessToken login(String password) throws SOSException, JsonParseException, JsonMappingException, IOException {

        SOSRestApiClient restApiClient = new SOSRestApiClient();
        restApiClient.addHeader("X-Vault-Token", webserviceCredentials.getApplicationToken());
        if ((keyStore != null) || (truststore != null)) {
            restApiClient.setSSLContext(keyStore, webserviceCredentials.getKeyPassword().toCharArray(), truststore);
        }

        SOSVaultAccountCredentials sosVaultAccountCredentials = new SOSVaultAccountCredentials();
        sosVaultAccountCredentials.setUsername(webserviceCredentials.getAccount());
        sosVaultAccountCredentials.setPassword(password);
        URI requestUri = URI.create(webserviceCredentials.getServiceUrl() + "/v1/auth/userpass/login/" + sosVaultAccountCredentials.getUsername());
        String body = Globals.objectMapper.writeValueAsString(sosVaultAccountCredentials);
        sosVaultAccountCredentials.setPassword(null);
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

    public String updateTokenPolicies(SOSVaultAccountCredentials sosVaultAccountCredentials) throws JsonProcessingException, SOSException {
        SOSRestApiClient restApiClient = new SOSRestApiClient();
        restApiClient.addHeader("X-Vault-Token", webserviceCredentials.getApplicationToken());
        if ((keyStore != null) || (truststore != null)) {
            restApiClient.setSSLContext(keyStore, webserviceCredentials.getKeyPassword().toCharArray(), truststore);
        }
        URI requestUri = URI.create(webserviceCredentials.getServiceUrl() + "/v1/auth/userpass/users/" + sosVaultAccountCredentials.getUsername());
        SOSVaultUpdatePolicies sosVaultUpdatePolicies = new SOSVaultUpdatePolicies();
        sosVaultUpdatePolicies.setToken_policies(sosVaultAccountCredentials.getTokenPolicies());
        String body = Globals.objectMapper.writeValueAsString(sosVaultUpdatePolicies);
        String response = restApiClient.postRestService(requestUri, body);

        LOGGER.debug(response);

        restApiClient.closeHttpClient();
        return response;
    }

    public String deleteAccount(String account) throws JsonProcessingException, SOSException, SocketException {
        SOSRestApiClient restApiClient = new SOSRestApiClient();
        restApiClient.addHeader("X-Vault-Token", webserviceCredentials.getApplicationToken());
        if ((keyStore != null) || (truststore != null)) {
            restApiClient.setSSLContext(keyStore, webserviceCredentials.getKeyPassword().toCharArray(), truststore);
        }
        URI requestUri = URI.create(webserviceCredentials.getServiceUrl() + "/v1/auth/userpass/users/" + account);
        String response = restApiClient.deleteRestService(requestUri);

        LOGGER.debug(response);

        restApiClient.closeHttpClient();
        return response;
    }

}
