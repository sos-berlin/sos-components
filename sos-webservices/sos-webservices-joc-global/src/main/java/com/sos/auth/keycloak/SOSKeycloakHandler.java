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
import com.sos.auth.classes.SOSAuthAccessTokenHandler;
import com.sos.auth.classes.SOSAuthHelper;
import com.sos.auth.keycloak.classes.SOSKeycloakAccountAccessToken;
import com.sos.auth.keycloak.classes.SOSKeycloakClientRepresentation;
import com.sos.auth.keycloak.classes.SOSKeycloakGroupRepresentation;
import com.sos.auth.keycloak.classes.SOSKeycloakIntrospectRepresentation;
import com.sos.auth.keycloak.classes.SOSKeycloakRoleRepresentation;
import com.sos.auth.keycloak.classes.SOSKeycloakUserRepresentation;
import com.sos.auth.keycloak.classes.SOSKeycloakWebserviceCredentials;
import com.sos.commons.exception.SOSException;
import com.sos.commons.httpclient.SOSRestApiClient;
import com.sos.commons.util.SOSString;
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

    public SOSKeycloakHandler(SOSKeycloakWebserviceCredentials webserviceCredentials, KeyStore trustStore) {
        this.truststore = trustStore;
        this.webserviceCredentials = webserviceCredentials;
    }

    private String getKeycloakErrorResponse(String response) {
        return response;

    }

    private String getFormResponse(Boolean post, String api, Map<String, String> body, String xKeycloakAccessToken) throws SOSException,
            SocketException {
        SOSRestApiClient restApiClient = new SOSRestApiClient();
        restApiClient.setConnectionTimeout(SOSAuthHelper.RESTAPI_CONNECTION_TIMEOUT);
        restApiClient.addHeader(CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED);
        if (!(xKeycloakAccessToken == null || xKeycloakAccessToken.isEmpty())) {
            restApiClient.addHeader(AUTHORIZATION, BEARER + " " + xKeycloakAccessToken);
        }
        if (truststore != null) {
            restApiClient.setSSLContext(null, null, truststore);
        }
        URI requestUri = URI.create(webserviceCredentials.getServiceUrl() + api);
        if (post && body != null) {
            for (java.util.Map.Entry<String, String> e : body.entrySet()) {
                if (e.getKey().contains("password") || e.getKey().contains("client_secret")) {
                    LOGGER.debug(e.getKey() + "= ********");
                } else {
                    LOGGER.debug(e.getKey() + "=" + e.getValue());
                }
            }
        }
        LOGGER.debug(requestUri.toString());

        String response;
        if (post) {
            response = restApiClient.postRestService(requestUri, body);
        } else {
            response = restApiClient.getRestService(requestUri);
        }
        if (response == null) {
            response = "";
        }
        LOGGER.debug(response);

        int httpReplyCode = restApiClient.statusCode();
        String contentType = restApiClient.getResponseHeader(CONTENT_TYPE);
        restApiClient.closeHttpClient();
        LOGGER.debug("httpReplyCode ===>" + httpReplyCode);

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
        case 401:
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

    public SOSKeycloakAccountAccessToken login(String password) throws SOSException, JsonParseException, JsonMappingException, IOException {

        Map<String, String> body = new HashMap<String, String>();

        body.put("username", webserviceCredentials.getAccount());
        body.put("password", password);
        body.put("client_id", webserviceCredentials.getClientId());
        body.put("grant_type", "password");
        body.put("client_secret", webserviceCredentials.getClientSecret());

        String response = getFormResponse(POST, "/auth/realms/" + webserviceCredentials.getRealm() + "/protocol/openid-connect/token", body, null);

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
        LOGGER.debug("accountTokenIsValid");

        SOSKeycloakIntrospectRepresentation sosKeycloakUserAccessToken = Globals.objectMapper.readValue(response,
                SOSKeycloakIntrospectRepresentation.class);

        LOGGER.debug(SOSString.toString(sosKeycloakAccountAccessToken));
        boolean valid = (sosKeycloakUserAccessToken.getActive() && sosKeycloakAccountAccessToken.getExpires_in()
                - SOSAuthAccessTokenHandler.TIME_GAP_SECONDS > 0);
        return valid;
    }

    public SOSKeycloakAccountAccessToken renewAccountAccess(SOSKeycloakAccountAccessToken sosKeycloakAccountAccessToken) throws SOSException,
            SocketException, JsonMappingException, JsonProcessingException {
        if (sosKeycloakAccountAccessToken != null) {

            Map<String, String> body = new HashMap<String, String>();
            body.put("client_id", webserviceCredentials.getClientId());
            body.put("grant_type", "refresh_token");
            body.put("client_secret", webserviceCredentials.getClientSecret());
            body.put("refresh_token", sosKeycloakAccountAccessToken.getRefresh_token());

            String response = getFormResponse(POST, "/auth/realms/" + webserviceCredentials.getRealm() + "/protocol/openid-connect/token", body,
                    null);

            SOSKeycloakAccountAccessToken newKeycloakUserAccessToken = Globals.objectMapper.readValue(response, SOSKeycloakAccountAccessToken.class);

            LOGGER.debug(SOSString.toString(newKeycloakUserAccessToken));
            return newKeycloakUserAccessToken;
        }
        return sosKeycloakAccountAccessToken;
    }

    private SOSKeycloakAccountAccessToken getAdminAccessToken() throws SocketException, SOSException, JsonMappingException, JsonProcessingException {

        Map<String, String> body = new HashMap<String, String>();

        body.put("username", webserviceCredentials.getAdminAccount());
        body.put("password", webserviceCredentials.getAdminPassword());
        body.put("client_id", webserviceCredentials.getClientId());
        body.put("grant_type", "password");
        body.put("client_secret", webserviceCredentials.getClientSecret());

        try {
            String response = getFormResponse(POST, "/auth/realms/" + webserviceCredentials.getRealm() + "/protocol/openid-connect/token", body,
                    null);
            SOSKeycloakAccountAccessToken sosKeycloakUserAccessToken = Globals.objectMapper.readValue(response, SOSKeycloakAccountAccessToken.class);

            return sosKeycloakUserAccessToken;
        } catch (JocException e) {
            LOGGER.info("Error accessing the admin account");
            throw e;
        }
    }

    private SOSKeycloakUserRepresentation getUserId(SOSKeycloakAccountAccessToken adminAccessToken) throws SocketException, SOSException,
            JsonMappingException, JsonProcessingException {

        String response = getFormResponse(GET, "/auth/admin/realms/" + webserviceCredentials.getRealm() + "/users/?username=" + webserviceCredentials
                .getAccount(), null, adminAccessToken.getAccess_token());
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
        SOSKeycloakRoleRepresentation[] sosKeycloakRoleRepresentation = Globals.objectMapper.readValue(response,
                SOSKeycloakRoleRepresentation[].class);
        return sosKeycloakRoleRepresentation;
    }

    private SOSKeycloakRoleRepresentation[] getGroupRealmRoles(SOSKeycloakAccountAccessToken adminAccessToken,
            SOSKeycloakGroupRepresentation sosKeycloakGroupRepresentation) throws SocketException, SOSException, JsonMappingException,
            JsonProcessingException {

        String response = getFormResponse(GET, "/auth/admin/realms/" + webserviceCredentials.getRealm() + "/groups/" + sosKeycloakGroupRepresentation
                .getId() + "/role-mappings/realm", null, adminAccessToken.getAccess_token());
        SOSKeycloakRoleRepresentation[] sosKeycloakRoleRepresentation = Globals.objectMapper.readValue(response,
                SOSKeycloakRoleRepresentation[].class);
        return sosKeycloakRoleRepresentation;
    }

    private SOSKeycloakRoleRepresentation[] getGroupClientRoles(SOSKeycloakAccountAccessToken adminAccessToken,
            SOSKeycloakGroupRepresentation sosKeycloakGroupRepresentation, SOSKeycloakClientRepresentation clientRepresentation)
            throws SocketException, SOSException, JsonMappingException, JsonProcessingException {
        String response = getFormResponse(GET, "/auth/admin/realms/" + webserviceCredentials.getRealm() + "/groups/" + sosKeycloakGroupRepresentation
                .getId() + "/role-mappings/clients/" + clientRepresentation.getId(), null, adminAccessToken.getAccess_token());
        SOSKeycloakRoleRepresentation[] sosKeycloakRoleRepresentation = Globals.objectMapper.readValue(response,
                SOSKeycloakRoleRepresentation[].class);
        return sosKeycloakRoleRepresentation;
    }

    private SOSKeycloakRoleRepresentation[] getAccountClientRoles(SOSKeycloakAccountAccessToken adminAccessToken,
            SOSKeycloakUserRepresentation userRepresentation, SOSKeycloakClientRepresentation clientRepresentation) throws SocketException,
            SOSException, JsonMappingException, JsonProcessingException {

        String response = getFormResponse(GET, "/auth/admin/realms/" + webserviceCredentials.getRealm() + "/users/" + userRepresentation.getId()
                + "/role-mappings/clients/" + clientRepresentation.getId(), null, adminAccessToken.getAccess_token());
        SOSKeycloakRoleRepresentation[] sosKeycloakRoleRepresentation = Globals.objectMapper.readValue(response,
                SOSKeycloakRoleRepresentation[].class);
        return sosKeycloakRoleRepresentation;
    }

    private SOSKeycloakGroupRepresentation[] getAccountGroups(SOSKeycloakAccountAccessToken adminAccessToken,
            SOSKeycloakUserRepresentation userRepresentation) throws SocketException, SOSException, JsonMappingException, JsonProcessingException {

        String response = getFormResponse(GET, "/auth/admin/realms/" + webserviceCredentials.getRealm() + "/users/" + userRepresentation.getId()
                + "/groups", null, adminAccessToken.getAccess_token());
        SOSKeycloakGroupRepresentation[] sosKeycloakGroupRepresentation = Globals.objectMapper.readValue(response,
                SOSKeycloakGroupRepresentation[].class);
        return sosKeycloakGroupRepresentation;
    }

    public Set<String> getTokenRoles() throws SocketException, SOSException, JsonMappingException, JsonProcessingException {
        Set<String> tokenRoles = new HashSet<String>();
        try {
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
        } catch (JocException e) {
            LOGGER.info("KEYCLOAK:" + e.getMessage());
        }
        return tokenRoles;
    }

}
