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
import com.sos.auth.classes.SOSAuthHelper;
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

    private static final String X_VAULT_TOKEN = "X-Vault-Token";
    private static final boolean POST = true;
    private static final boolean GET = false;
    private static final Logger LOGGER = LoggerFactory.getLogger(SOSVaultHandler.class);
    private KeyStore truststore = null;
    private SOSVaultWebserviceCredentials webserviceCredentials;

    public SOSVaultHandler(SOSVaultWebserviceCredentials webserviceCredentials, KeyStore trustStore) {
        this.truststore = trustStore;
        this.webserviceCredentials = webserviceCredentials;
    }

    private String getVaultErrorResponse(String response) {
        try {
            SOSVaultCheckAccessTokenResponse sosVaultCheckAccessTokenResponse = Globals.objectMapper.readValue(response,
                    SOSVaultCheckAccessTokenResponse.class);
            if (sosVaultCheckAccessTokenResponse.getErrors() != null && sosVaultCheckAccessTokenResponse.getErrors().size() > 0) {
                return sosVaultCheckAccessTokenResponse.getErrors().get(0);
            } else {
                return "";
            }
        } catch (IOException e) {
            LOGGER.warn(e.getMessage());
            return response;
        }

    }

    private String getResponse(boolean post, String api, String body, String xVaultAccessToken) throws SOSException, SocketException {
        SOSRestApiClient restApiClient = new SOSRestApiClient();
        restApiClient.setConnectionTimeout(SOSAuthHelper.RESTAPI_CONNECTION_TIMEOUT);


        if (!(xVaultAccessToken == null || xVaultAccessToken.isEmpty())) {
            restApiClient.addHeader(X_VAULT_TOKEN, xVaultAccessToken);
        }
        if (truststore != null) {
            restApiClient.setSSLContext(null, null, truststore);
        }
        URI requestUri = URI.create(webserviceCredentials.getServiceUrl() + api);

        String response;
        if (post) {
            response = restApiClient.postRestService(requestUri, body);
        } else {
            response = restApiClient.getRestService(requestUri);
        }

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
            jocError.setMessage(getVaultErrorResponse(response));
            throw new JocException(jocError);
        case 403:
            jocError.setMessage(getVaultErrorResponse(response)
                    + ":Forbidden, your authentication details are either incorrect, you don't have access to this feature, or - if CORS is enabled - you made a cross-origin request from an origin that is not allowed to make such requests.");
            throw new JocException(jocError);
        case 404:
            jocError.setMessage(getVaultErrorResponse(response)
                    + ":Invalid path. This can both mean that the path truly doesn't exist or that you don't have permission to view a specific path. Vault uses 404 in some cases to avoid state leakage.");
            throw new JocException(jocError);
        case 405:
            jocError.setMessage(getVaultErrorResponse(response)
                    + ":Unsupported operation. You tried to use a method inappropriate to the request path, e.g. a POST on an endpoint that only accepts GETs.");
            throw new JocException(jocError);
        case 412:
            jocError.setMessage(getVaultErrorResponse(response)
                    + ":Precondition failed. Returned on Enterprise when a request can't be processed yet due to some missing eventually consistent data. Should be retried, perhaps with a little backoff. See Vault Eventual Consistency.");
            throw new JocException(jocError);
        case 429:
            jocError.setMessage(getVaultErrorResponse(response)
                    + ": Default return code for health status of standby nodes. This will likely change in the future.");
            throw new JocException(jocError);
        case 473:
            jocError.setMessage(getVaultErrorResponse(response) + ":Default return code for health status of performance standby nodes.");
            throw new JocException(jocError);
        case 500:
            jocError.setMessage(getVaultErrorResponse(response) + ":Internal server error. An internal error has occurred, try again later");
            throw new JocException(jocError);
        case 502:
            jocError.setMessage(getVaultErrorResponse(response)
                    + ":A request to Vault required Vault making a request to a third party; the third party responded with an error of some kind.");
            throw new JocException(jocError);
        case 503:
            jocError.setMessage(getVaultErrorResponse(response) + ":Vault is down for maintenance or is currently sealed. Try again later.");
            throw new JocException(jocError);
        default:
            jocError.setMessage(httpReplyCode + " " + restApiClient.getHttpResponse().getStatusLine().getReasonPhrase());
            throw new JocException(jocError);
        }

    }

    public String storeAccountPassword(SOSVaultAccountCredentials sosVaultAccountCredentials, String password) throws SOSException,
            JsonProcessingException, SocketException {

        SOSVaultStoreUser sosVaultStoreUser = new SOSVaultStoreUser();
        sosVaultStoreUser.setUsername(sosVaultAccountCredentials.getUsername());
        sosVaultStoreUser.setPassword(password);
        String body = Globals.objectMapper.writeValueAsString(sosVaultStoreUser);
        sosVaultAccountCredentials.setPassword(password);
        String response = getResponse(POST, "/v1/auth/" + webserviceCredentials.getAuthenticationMethodPath() + "/users/" + sosVaultAccountCredentials
                .getUsername(), body, webserviceCredentials.getApplicationToken());

        LOGGER.debug(response);

        return response;
    }

    public SOSVaultAccountAccessToken login(String password) throws SOSException, JsonParseException, JsonMappingException, IOException {

        SOSVaultAccountCredentials sosVaultAccountCredentials = new SOSVaultAccountCredentials();
        sosVaultAccountCredentials.setUsername(webserviceCredentials.getAccount());
        sosVaultAccountCredentials.setPassword(password);
        String body = Globals.objectMapper.writeValueAsString(sosVaultAccountCredentials);
        sosVaultAccountCredentials.setPassword(null);

        String response = getResponse(POST, "/v1/auth/" + webserviceCredentials.getAuthenticationMethodPath() + "/login/" + sosVaultAccountCredentials
                .getUsername(), body, null);

        LOGGER.debug(response);

        SOSVaultAccountAccessToken sosVaultUserAccessToken = Globals.objectMapper.readValue(response, SOSVaultAccountAccessToken.class);

        return sosVaultUserAccessToken;
    }

    public boolean accountAccessTokenIsValid(SOSVaultAccountAccessToken sosVaultAccountAccessToken) throws JsonParseException, JsonMappingException,
            IOException, SOSException {

        String response = getResponse(GET, "/v1/auth/token/lookup-self", "", sosVaultAccountAccessToken.getAuth().getClient_token());

        LOGGER.debug(response);
        SOSVaultCheckAccessTokenResponse sosVaultCheckAccessTokenResponse = Globals.objectMapper.readValue(response,
                SOSVaultCheckAccessTokenResponse.class);

        return (sosVaultCheckAccessTokenResponse.getErrors() == null || sosVaultCheckAccessTokenResponse.getErrors().size() == 0);
    }

    public void renewAccountAccess(SOSVaultAccountAccessToken sosVaultAccountAccessToken) throws SOSException, SocketException {
        if (sosVaultAccountAccessToken != null && sosVaultAccountAccessToken.getAuth().isRenewable()) {
            String response = getResponse(POST, "/v1/auth/token/renew-self", "", sosVaultAccountAccessToken.getAuth().getClient_token());
            LOGGER.debug(response);
        }
    }

    public String updateTokenPolicies(SOSVaultAccountCredentials sosVaultAccountCredentials) throws JsonProcessingException, SOSException,
            SocketException {
        SOSVaultUpdatePolicies sosVaultUpdatePolicies = new SOSVaultUpdatePolicies();
        sosVaultUpdatePolicies.setToken_policies(sosVaultAccountCredentials.getTokenPolicies());
        String body = Globals.objectMapper.writeValueAsString(sosVaultUpdatePolicies);
        sosVaultAccountCredentials.setPassword(null);

        String response = getResponse(POST, "/v1/auth/" + webserviceCredentials.getAuthenticationMethodPath() + "/users/" + sosVaultAccountCredentials
                .getUsername(), body, webserviceCredentials.getApplicationToken());

        LOGGER.debug(response);

        return response;
    }

    public String deleteAccount(String account) throws JsonProcessingException, SOSException, SocketException {
        SOSRestApiClient restApiClient = new SOSRestApiClient();
        restApiClient.addHeader(X_VAULT_TOKEN, webserviceCredentials.getApplicationToken());
        if ((truststore != null)) {
            restApiClient.setSSLContext(null, null, truststore);
        }
        URI requestUri = URI.create(webserviceCredentials.getServiceUrl() + "/v1/auth/" + webserviceCredentials.getAuthenticationMethodPath()
                + "/users/" + account);
        String response = restApiClient.deleteRestService(requestUri);

        LOGGER.debug(response);

        restApiClient.closeHttpClient();
        return response;
    }

}
