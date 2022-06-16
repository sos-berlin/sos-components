package com.sos.auth.keycloak;

import java.io.IOException;
import java.net.SocketException;
import java.net.URI;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.auth.classes.SOSIdentityService;
import com.sos.auth.keycloak.classes.SOSKeycloakAccountAccessToken;
import com.sos.auth.keycloak.classes.SOSKeycloakAccountCredentials;
import com.sos.auth.keycloak.classes.SOSKeycloakClientRepresentation;
import com.sos.auth.keycloak.classes.SOSKeycloakGroupRepresentation;
import com.sos.auth.keycloak.classes.SOSKeycloakRoleRepresentation;
import com.sos.auth.keycloak.classes.SOSKeycloakStoreUser;
import com.sos.auth.keycloak.classes.SOSKeycloakIntrospectRepresentation;
import com.sos.auth.keycloak.classes.SOSKeycloakUpdatePolicies;
import com.sos.auth.keycloak.classes.SOSKeycloakUserRepresentation;
import com.sos.auth.keycloak.classes.SOSKeycloakWebserviceCredentials;
import com.sos.commons.exception.SOSException;
import com.sos.commons.httpclient.SOSRestApiClient;
import com.sos.joc.Globals;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;

public class SOSKeycloakHandler {

    private static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
    private static final boolean POST = true;
    private static final boolean GET = false;
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String BEARER = "Bearer";
    private static final String AUTHORIZATION = "Authorization";

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSKeycloakHandler.class);
    private KeyStore truststore = null;
    private SOSKeycloakWebserviceCredentials webserviceCredentials;
    private SOSIdentityService identityService;

    public SOSKeycloakHandler(SOSKeycloakWebserviceCredentials webserviceCredentials, KeyStore trustStore, SOSIdentityService identityService) {
        this.truststore = trustStore;
        this.webserviceCredentials = webserviceCredentials;
        this.identityService = identityService;
    }

    private String getKeycloakErrorResponse(String response) {
        return "";

    }

    private String getResponse(boolean post, String api, String body, String xKeycloakAccessToken) throws SOSException, SocketException {
        SOSRestApiClient restApiClient = new SOSRestApiClient();

        if (!(xKeycloakAccessToken == null || xKeycloakAccessToken.isEmpty())) {
            restApiClient.addHeader(AUTHORIZATION, BEARER + " " + xKeycloakAccessToken);
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
        String contentType = restApiClient.getResponseHeader(CONTENT_TYPE);
        restApiClient.closeHttpClient();

        JocError jocError = new JocError();
        switch (httpReplyCode) {
        case 201:
        case 204:
            return response;
        case 400:
        case 403:
        case 404:
        case 405:
        case 412:
        case 429:
        case 473:
        case 500:
        case 502:
        case 503:
            jocError.setMessage(getKeycloakErrorResponse(response));
            throw new JocException(jocError);
        default:
            jocError.setMessage(httpReplyCode + " " + restApiClient.getHttpResponse().getStatusLine().getReasonPhrase());
            throw new JocException(jocError);
        }

    }

    private String getFormResponse(Boolean post, String api, Map<String, String> body, String xKeycloakAccessToken) throws SOSException,
            SocketException {
        SOSRestApiClient restApiClient = new SOSRestApiClient();
        restApiClient.addHeader(CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED);
        if (!(xKeycloakAccessToken == null || xKeycloakAccessToken.isEmpty())) {
            restApiClient.addHeader(AUTHORIZATION, BEARER + " " + xKeycloakAccessToken);
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
        String contentType = restApiClient.getResponseHeader(CONTENT_TYPE);
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
        case 403:
        case 404:
        case 405:
        case 412:
        case 429:
        case 473:
        case 500:
        case 502:
        case 503:
            jocError.setMessage(getKeycloakErrorResponse(response));
            throw new JocException(jocError);
        default:
            jocError.setMessage(httpReplyCode + " " + restApiClient.getHttpResponse().getStatusLine().getReasonPhrase());
            throw new JocException(jocError);
        }

    }

    public String storeAccountPassword(SOSKeycloakAccountCredentials sosKeycloakAccountCredentials, String password) throws SOSException,
            JsonProcessingException, SocketException {

        SOSKeycloakStoreUser sosKeycloakStoreUser = new SOSKeycloakStoreUser();
        sosKeycloakStoreUser.setUsername(sosKeycloakAccountCredentials.getUsername());
        sosKeycloakStoreUser.setPassword(password);
        String body = Globals.objectMapper.writeValueAsString(sosKeycloakStoreUser);
        sosKeycloakAccountCredentials.setPassword(password);
        String response = "";
        // String response = getResponse(POST, "/v1/auth/" + webserviceCredentials.getAuthenticationMethodPath() + "/users/" + sosKeycloakAccountCredentials
        // .getUsername(), body, webserviceCredentials.getApplicationToken());

        LOGGER.debug(response);

        return response;
    }

    public SOSKeycloakAccountAccessToken login(String password) throws SOSException, JsonParseException, JsonMappingException, IOException {

        Map<String, String> body = new HashMap<String, String>();

        body.put("username", webserviceCredentials.getAccount());
        body.put("password", password);
        body.put("client_id", webserviceCredentials.getClientId());
        body.put("grant_type", "password");
        body.put("client_secret", webserviceCredentials.getClientSecret());

        String response = getFormResponse(POST, "/auth/realms/" + webserviceCredentials.getRealm() + "/protocol/openid-connect/token", body, null);
        LOGGER.debug(response);

        SOSKeycloakAccountAccessToken sosKeycloakUserAccessToken = Globals.objectMapper.readValue(response, SOSKeycloakAccountAccessToken.class);

        return sosKeycloakUserAccessToken;
    }

    public boolean accountAccessTokenIsValid(SOSKeycloakAccountAccessToken sosKeycloakAccountAccessToken) throws JsonParseException,
            JsonMappingException, IOException, SOSException {
        Map<String, String> body = new HashMap<String, String>();
        body.put("client_id", webserviceCredentials.getClientId());
        body.put("token", sosKeycloakAccountAccessToken.getAccess_token());
        body.put("client_secret", webserviceCredentials.getClientSecret());

        String response = getFormResponse(POST, "/auth/realms/" + webserviceCredentials.getRealm() + "/protocol/openid-connect/token/introspect",
                body, null);
        LOGGER.debug(response);

        SOSKeycloakIntrospectRepresentation sosKeycloakUserAccessToken = Globals.objectMapper.readValue(response,
                SOSKeycloakIntrospectRepresentation.class);

        return sosKeycloakUserAccessToken.getActive();
    }

    public void renewAccountAccess(SOSKeycloakAccountAccessToken sosKeycloakAccountAccessToken) throws SOSException, SocketException {
        if (sosKeycloakAccountAccessToken != null) {

            Map<String, String> body = new HashMap<String, String>();
            body.put("client_id", webserviceCredentials.getClientId());
            body.put("grant_type", "refresh_token");
            body.put("refresh_token", sosKeycloakAccountAccessToken.getRefresh_token());

            String response = getFormResponse(POST, "/auth/realms/" + webserviceCredentials.getRealm() + "/protocol/openid-connect/token", body,
                    null);
            LOGGER.debug(response);
        }
    }

    public String updateTokenPolicies(SOSKeycloakAccountCredentials sosKeycloakAccountCredentials) throws JsonProcessingException, SOSException,
            SocketException {
        SOSKeycloakUpdatePolicies sosKeycloakUpdatePolicies = new SOSKeycloakUpdatePolicies();
        sosKeycloakUpdatePolicies.setToken_policies(sosKeycloakAccountCredentials.getKeycloackRoles());
        String body = Globals.objectMapper.writeValueAsString(sosKeycloakUpdatePolicies);
        sosKeycloakAccountCredentials.setPassword(null);
        String response = "";

        // String response = getResponse(POST, "/v1/auth/" + webserviceCredentials.getAuthenticationMethodPath() + "/users/" + sosKeycloakAccountCredentials
        // .getUsername(), body, webserviceCredentials.getApplicationToken());

        LOGGER.debug(response);

        return response;
    }

    private SOSKeycloakAccountAccessToken getAdminAccessToken() throws SocketException, SOSException, JsonMappingException, JsonProcessingException {

        Map<String, String> body = new HashMap<String, String>();

        body.put("username", webserviceCredentials.getAdminAccount());
        body.put("password", webserviceCredentials.getAdminPassword());
        body.put("client_id", webserviceCredentials.getClientId());
        body.put("grant_type", "password");
        body.put("client_secret", webserviceCredentials.getClientSecret());

        String response = getFormResponse(POST, "/auth/realms/" + webserviceCredentials.getRealm() + "/protocol/openid-connect/token", body, null);

        LOGGER.debug(response);

        SOSKeycloakAccountAccessToken sosKeycloakUserAccessToken = Globals.objectMapper.readValue(response, SOSKeycloakAccountAccessToken.class);

        return sosKeycloakUserAccessToken;
    }

    private SOSKeycloakAccountAccessToken getUserRealmRoles() throws SocketException, SOSException, JsonMappingException, JsonProcessingException {

        Map<String, String> body = new HashMap<String, String>();

        body.put("username", webserviceCredentials.getAdminAccount());
        body.put("password", webserviceCredentials.getAdminPassword());
        body.put("client_id", webserviceCredentials.getClientId());
        body.put("grant_type", "password");
        body.put("client_secret", webserviceCredentials.getClientSecret());

        String response = getFormResponse(POST, "/auth/realms/" + webserviceCredentials.getRealm() + "master/protocol/openid-connect/token", body,
                null);

        LOGGER.debug(response);

        SOSKeycloakAccountAccessToken sosKeycloakUserAccessToken = Globals.objectMapper.readValue(response, SOSKeycloakAccountAccessToken.class);

        return sosKeycloakUserAccessToken;
    }

    private SOSKeycloakUserRepresentation getUserId(SOSKeycloakAccountAccessToken adminAccessToken) throws SocketException, SOSException,
            JsonMappingException, JsonProcessingException {

        String response = getFormResponse(GET, "/auth/admin/realms/" + webserviceCredentials.getRealm() + "/users/?username=" + webserviceCredentials
                .getAccount(), null, adminAccessToken.getAccess_token());
        LOGGER.debug(response);
        SOSKeycloakUserRepresentation[] sosKeycloakUserRepresentation = Globals.objectMapper.readValue(response,
                SOSKeycloakUserRepresentation[].class);
        if (sosKeycloakUserRepresentation.length > 0) {
            return sosKeycloakUserRepresentation[0];
        } else {
            return null;
        }
    }

    private SOSKeycloakClientRepresentation getClientId(SOSKeycloakAccountAccessToken adminAccessToken) throws SocketException, SOSException,
            JsonMappingException, JsonProcessingException {

        String response = getFormResponse(GET, "/auth/admin/realms/" + webserviceCredentials.getRealm() + "/clients/?clientId="
                + webserviceCredentials.getClientId(), null, adminAccessToken.getAccess_token());
        LOGGER.debug(response);
        // response = response.substring(1, response.length() - 1);
        SOSKeycloakClientRepresentation[] sosKeycloakClientRepresentation = Globals.objectMapper.readValue(response,
                SOSKeycloakClientRepresentation[].class);
        if (sosKeycloakClientRepresentation.length > 0) {
            return sosKeycloakClientRepresentation[0];
        } else {
            return null;
        }
    }

    private SOSKeycloakRoleRepresentation[] getAccountRealmRoles(SOSKeycloakAccountAccessToken adminAccessToken,
            SOSKeycloakUserRepresentation userRepresentation) throws SocketException, SOSException, JsonMappingException, JsonProcessingException {
        String response = getFormResponse(GET, "/auth/admin/realms/" + webserviceCredentials.getRealm() + "/users/" + userRepresentation.getId()
                + "/role-mappings/realm", null, adminAccessToken.getAccess_token());
        LOGGER.debug(response);
        SOSKeycloakRoleRepresentation[] sosKeycloakRoleRepresentation = Globals.objectMapper.readValue(response,
                SOSKeycloakRoleRepresentation[].class);
        return sosKeycloakRoleRepresentation;
    }

    private SOSKeycloakRoleRepresentation[] getGroupRealmRoles(SOSKeycloakAccountAccessToken adminAccessToken,
            SOSKeycloakGroupRepresentation sosKeycloakGroupRepresentation) throws SocketException, SOSException, JsonMappingException,
            JsonProcessingException {

        String response = getFormResponse(GET, "/auth/admin/realms/" + webserviceCredentials.getRealm() + "/groups/" + sosKeycloakGroupRepresentation
                .getId() + "/role-mappings/realm", null, adminAccessToken.getAccess_token());
        LOGGER.debug(response);
        SOSKeycloakRoleRepresentation[] sosKeycloakRoleRepresentation = Globals.objectMapper.readValue(response,
                SOSKeycloakRoleRepresentation[].class);
        return sosKeycloakRoleRepresentation;
    }

    private SOSKeycloakRoleRepresentation[] getGroupClientRoles(SOSKeycloakAccountAccessToken adminAccessToken,
            SOSKeycloakGroupRepresentation sosKeycloakGroupRepresentation, SOSKeycloakClientRepresentation clientRepresentation)
            throws SocketException, SOSException, JsonMappingException, JsonProcessingException {
        String response = getFormResponse(GET, "/auth/admin/realms/" + webserviceCredentials.getRealm() + "/groups/" + sosKeycloakGroupRepresentation
                .getId() + "/role-mappings/clients/" + clientRepresentation.getId(), null, adminAccessToken.getAccess_token());
        LOGGER.debug(response);
        SOSKeycloakRoleRepresentation[] sosKeycloakRoleRepresentation = Globals.objectMapper.readValue(response,
                SOSKeycloakRoleRepresentation[].class);
        return sosKeycloakRoleRepresentation;
    }

    private SOSKeycloakRoleRepresentation[] getAccountClientRoles(SOSKeycloakAccountAccessToken adminAccessToken,
            SOSKeycloakUserRepresentation userRepresentation, SOSKeycloakClientRepresentation clientRepresentation) throws SocketException,
            SOSException, JsonMappingException, JsonProcessingException {

        String response = getFormResponse(GET, "/auth/admin/realms/" + webserviceCredentials.getRealm() + "/users/" + userRepresentation.getId()
                + "/role-mappings/clients/" + clientRepresentation.getId(), null, adminAccessToken.getAccess_token());
        LOGGER.debug(response);
        SOSKeycloakRoleRepresentation[] sosKeycloakRoleRepresentation = Globals.objectMapper.readValue(response,
                SOSKeycloakRoleRepresentation[].class);
        return sosKeycloakRoleRepresentation;
    }

    private SOSKeycloakGroupRepresentation[] getAccountGroups(SOSKeycloakAccountAccessToken adminAccessToken,
            SOSKeycloakUserRepresentation userRepresentation) throws SocketException, SOSException, JsonMappingException, JsonProcessingException {

        String response = getFormResponse(GET, "/auth/admin/realms/" + webserviceCredentials.getRealm() + "/users/" + userRepresentation.getId()
                + "/groups", null, adminAccessToken.getAccess_token());
        LOGGER.debug(response);
        SOSKeycloakGroupRepresentation[] sosKeycloakGroupRepresentation = Globals.objectMapper.readValue(response,
                SOSKeycloakGroupRepresentation[].class);
        return sosKeycloakGroupRepresentation;
    }

    public Set<String> getTokenRoles() throws SocketException, SOSException, JsonMappingException, JsonProcessingException {
        Set<String> tokenRoles = new HashSet<String>();
        SOSKeycloakRoleRepresentation[] sosKeycloakRoleRepresentations;
        // 1. get a admin access-token
        SOSKeycloakAccountAccessToken adminAccessToken = this.getAdminAccessToken();
        // 2. get user-id
        SOSKeycloakUserRepresentation userRepresentation = this.getUserId(adminAccessToken);
        // 2. get client-id
        SOSKeycloakClientRepresentation clientRepresentation = this.getClientId(adminAccessToken);

        // 3. get account realm roles
        sosKeycloakRoleRepresentations = this.getAccountRealmRoles(adminAccessToken, userRepresentation);
        for (int i = 0; i < sosKeycloakRoleRepresentations.length; i++) {
            tokenRoles.add(sosKeycloakRoleRepresentations[i].getName());
        }
        // 4. get account client roles
        sosKeycloakRoleRepresentations = this.getAccountClientRoles(adminAccessToken, userRepresentation, clientRepresentation);
        for (int i = 0; i < sosKeycloakRoleRepresentations.length; i++) {
            tokenRoles.add(sosKeycloakRoleRepresentations[i].getName());
        }
        // 5. get account groups
        SOSKeycloakGroupRepresentation[] sosKeycloakGroupRepresentations = this.getAccountGroups(adminAccessToken, userRepresentation);
        // 5.1. foreach get group id
        for (int i = 0; i < sosKeycloakGroupRepresentations.length; i++) {
            // 5.11 get account group realm roles
            sosKeycloakRoleRepresentations = this.getGroupRealmRoles(adminAccessToken, sosKeycloakGroupRepresentations[i]);
            for (int ii = 0; ii < sosKeycloakRoleRepresentations.length; ii++) {
                tokenRoles.add(sosKeycloakRoleRepresentations[ii].getName());
            }
            // 5.11 get account group client roles
            sosKeycloakRoleRepresentations = this.getGroupClientRoles(adminAccessToken, sosKeycloakGroupRepresentations[i], clientRepresentation);
            for (int ii = 0; ii < sosKeycloakRoleRepresentations.length; ii++) {
                tokenRoles.add(sosKeycloakRoleRepresentations[ii].getName());
            }

        }

        return tokenRoles;
    }

}
