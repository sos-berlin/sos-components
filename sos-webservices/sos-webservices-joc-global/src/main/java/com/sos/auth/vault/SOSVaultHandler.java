package com.sos.auth.vault;

import java.io.IOException;
import java.net.SocketException;
import java.net.URI;
import java.security.KeyStore;

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
import com.sos.auth.vault.pojo.sys.auth.SOSVaultCheckAccessTokenResponse;
import com.sos.commons.exception.SOSException;
import com.sos.commons.httpclient.SOSRestApiClient;
import com.sos.joc.Globals;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;

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

    private String getResponse(String api, String body) throws SOSException {
        SOSRestApiClient restApiClient = new SOSRestApiClient();

        restApiClient.addHeader("X-Vault-Token", webserviceCredentials.getApplicationToken());
        if ((keyStore != null) || (truststore != null)) {
            restApiClient.setSSLContext(keyStore, webserviceCredentials.getKeyPassword().toCharArray(), truststore);
        }
        URI requestUri = URI.create(webserviceCredentials.getServiceUrl() + api);

        String response = restApiClient.postRestService(requestUri, body);
        if (response == null) {
            response = "";
        }

        int httpReplyCode = restApiClient.statusCode();
        String contentType = restApiClient.getResponseHeader("Content-Type");
        restApiClient.closeHttpClient();

        JocError jocError = new JocError();
        switch (httpReplyCode) {
        case 200:
            if (contentType.contains("application/json")) {
                if (response.isEmpty()) {
                    jocError.setMessage("Unexpected empty response");
                    throw new JocException(jocError);
                }
                return response;
            } else {
                jocError.setMessage(String.format("Unexpected content type '%1$s'. Response: %2$s", contentType, response));
                throw new JocException(jocError);
            }
        case 201:
        case 204:
            return response;
        case 400:
            jocError.setMessage("Invalid request, missing or invalid data.");
            throw new JocException(jocError);
        case 403:
            jocError.setMessage(
                    "Forbidden, your authentication details are either incorrect, you don't have access to this feature, or - if CORS is enabled - you made a cross-origin request from an origin that is not allowed to make such requests.");
            throw new JocException(jocError);
        case 404:
            jocError.setMessage(
                    "Invalid path. This can both mean that the path truly doesn't exist or that you don't have permission to view a specific path. Vault uses 404 in some cases to avoid state leakage.");
            throw new JocException(jocError);
        case 405:
            jocError.setMessage(
                    "Unsupported operation. You tried to use a method inappropriate to the request path, e.g. a POST on an endpoint that only accepts GETs.");
            throw new JocException(jocError);
        case 412:
            jocError.setMessage(
                    "Precondition failed. Returned on Enterprise when a request can't be processed yet due to some missing eventually consistent data. Should be retried, perhaps with a little backoff. See Vault Eventual Consistency.");
            throw new JocException(jocError);
        case 429:
            jocError.setMessage("Default return code for health status of standby nodes. This will likely change in the future.");
            throw new JocException(jocError);
        case 473:
            jocError.setMessage("Default return code for health status of performance standby nodes.");
            throw new JocException(jocError);
        case 500:
            jocError.setMessage("Internal server error. An internal error has occurred, try again later");
            throw new JocException(jocError);
        case 502:
            jocError.setMessage(
                    "A request to Vault required Vault making a request to a third party; the third party responded with an error of some kind.");
            throw new JocException(jocError);
        case 503:
            jocError.setMessage("Vault is down for maintenance or is currently sealed. Try again later.");
            throw new JocException(jocError);
        default:
            jocError.setMessage(httpReplyCode + " " + restApiClient.getHttpResponse().getStatusLine().getReasonPhrase());
            throw new JocException(jocError);
        }

    }

    public String storeAccountPassword(SOSVaultAccountCredentials sosVaultAccountCredentials, String password) throws SOSException,
            JsonProcessingException {

        SOSVaultStoreUser sosVaultStoreUser = new SOSVaultStoreUser();
        sosVaultStoreUser.setUsername(sosVaultAccountCredentials.getUsername());
        sosVaultStoreUser.setPassword(password);
        String body = Globals.objectMapper.writeValueAsString(sosVaultStoreUser);
        sosVaultAccountCredentials.setPassword("");
        String response = getResponse("/v1/auth/userpass/users/" + sosVaultAccountCredentials.getUsername(), body);

        LOGGER.debug(response);

        return response;
    }

    public SOSVaultAccountAccessToken login(String password) throws SOSException, JsonParseException, JsonMappingException, IOException {

        SOSVaultAccountCredentials sosVaultAccountCredentials = new SOSVaultAccountCredentials();
        sosVaultAccountCredentials.setUsername(webserviceCredentials.getAccount());
        sosVaultAccountCredentials.setPassword(password);
        String body = Globals.objectMapper.writeValueAsString(sosVaultAccountCredentials);
        sosVaultAccountCredentials.setPassword(null);

        String response = getResponse("/v1/auth/userpass/login/" + sosVaultAccountCredentials.getUsername(), body);

        LOGGER.debug(response);

        SOSVaultAccountAccessToken sosVaultUserAccessToken = Globals.objectMapper.readValue(response, SOSVaultAccountAccessToken.class);

        return sosVaultUserAccessToken;
    }

    public boolean accountAccessTokenIsValid(SOSVaultAccountAccessToken sosVaultAccountAccessToken) throws JsonParseException, JsonMappingException,
            IOException, SOSException {

        String body = "{\"token\":\"" + sosVaultAccountAccessToken.getAuth().getClient_token() + "\"}";
        String response = getResponse("/v1/auth/token/lookup", body);

        LOGGER.debug(response);
        SOSVaultCheckAccessTokenResponse sosVaultCheckAccessTokenResponse = Globals.objectMapper.readValue(response,
                SOSVaultCheckAccessTokenResponse.class);

        return (sosVaultCheckAccessTokenResponse.getErrors() == null || sosVaultCheckAccessTokenResponse.getErrors().size() == 0);
    }

    public void renewAccountAccess(SOSVaultAccountAccessToken sosVaultAccountAccessToken) throws SOSException {
        if (sosVaultAccountAccessToken != null && sosVaultAccountAccessToken.getAuth().isRenewable()) {
            String body = "{\"token\":\"" + sosVaultAccountAccessToken.getAuth().getClient_token() + "\"}";
            String response = getResponse("/v1/auth/token/renew", body);
            LOGGER.debug(response);
        }
    }

    public String updateTokenPolicies(SOSVaultAccountCredentials sosVaultAccountCredentials) throws JsonProcessingException, SOSException {
        SOSVaultUpdatePolicies sosVaultUpdatePolicies = new SOSVaultUpdatePolicies();
        sosVaultUpdatePolicies.setToken_policies(sosVaultAccountCredentials.getTokenPolicies());
        String body = Globals.objectMapper.writeValueAsString(sosVaultUpdatePolicies);
        sosVaultAccountCredentials.setPassword(null);

        String response = getResponse("/v1/auth/userpass/users/" + sosVaultAccountCredentials.getUsername(), body);

        LOGGER.debug(response);

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
