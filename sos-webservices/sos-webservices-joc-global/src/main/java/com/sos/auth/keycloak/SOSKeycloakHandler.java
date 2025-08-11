package com.sos.auth.keycloak;

import java.net.URI;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.time.Duration;
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
import com.sos.commons.httpclient.BaseHttpClient;
import com.sos.commons.httpclient.commons.HttpExecutionResult;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.http.HttpUtils;
import com.sos.commons.util.loggers.impl.SLF4JLogger;
import com.sos.joc.Globals;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.security.identityservice.IdentityServiceTypes;

public class SOSKeycloakHandler {

    private static final String KEYCLOAK_COMPATIBILITY = "16";
    private static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
    private static final boolean POST = true;
    private static final boolean GET = false;
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String BEARER = "Bearer";
    private static final String AUTHORIZATION = "Authorization";

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSKeycloakHandler.class);
    private KeyStore truststore = null;
    private SOSKeycloakWebserviceCredentials webserviceCredentials;

    public SOSKeycloakHandler(SOSKeycloakWebserviceCredentials webserviceCredentials, KeyStore truststore) {
        this.truststore = truststore;
        this.webserviceCredentials = webserviceCredentials;
    }

    private String getRelativePath() {
        if (this.webserviceCredentials.getCompatibility() == null || this.webserviceCredentials.getCompatibility().equalsIgnoreCase(
                KEYCLOAK_COMPATIBILITY)) {
            return "/auth";
        } else {
            return "";
        }
    }

    private String getKeycloakErrorResponse(String response) {
        return response;

    }

    private String getFormResponse(Boolean post, String api, Map<String, String> body, String xKeycloakAccessToken) throws SOSException,
            Exception {
        BaseHttpClient.Builder builder =  BaseHttpClient.withBuilder().withConnectTimeout(Duration.ofMillis(SOSAuthHelper.RESTAPI_CONNECTION_TIMEOUT))
                .withLogger(new SLF4JLogger(LOGGER));

        Map<String, String> requestHeaders = new HashMap<String, String>();
        requestHeaders.put(CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED);
        if (!(xKeycloakAccessToken == null || xKeycloakAccessToken.isEmpty())) {
            requestHeaders.put(AUTHORIZATION, BEARER + " " + xKeycloakAccessToken);
        }
        if (truststore != null) {
            builder.withSSLContext(com.sos.joc.classes.SSLContext.createSslContext(truststore));
        }
        BaseHttpClient client = builder.build();
        String serviceUrl = webserviceCredentials.getServiceUrl();
        if (serviceUrl.endsWith("/")) {
            serviceUrl = serviceUrl.substring(0, serviceUrl.length() - 1);
        }
        URI requestUri = URI.create(serviceUrl + api);
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

        HttpExecutionResult<String> result;
        if (post) {
            result = client.executePOST(requestUri, requestHeaders,
                    BodyPublishers.ofByteArray(HttpUtils.createUrlEncodedBodyfromMap(body).getBytes(StandardCharsets.UTF_8)), BodyHandlers.ofString());
        } else {
            result = client.executeGET(requestUri, requestHeaders, BodyHandlers.ofString());
        }
        String response = result.response().body();
        LOGGER.debug(response);
        int httpReplyCode = result.response().statusCode();
        String contentType = result.response().headers().firstValue(CONTENT_TYPE).orElse("");
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
            jocError.setMessage(httpReplyCode + " " + HttpUtils.getReasonPhrase(httpReplyCode));
            throw new JocException(jocError);
        }

    }

    public SOSKeycloakAccountAccessToken login(IdentityServiceTypes identityServiceType, String password) throws SOSException, JsonParseException,
            JsonMappingException, Exception {
        if (identityServiceType == IdentityServiceTypes.KEYCLOAK_JOC) {
            if (!SOSAuthHelper.accountExist(webserviceCredentials.getAccount(), webserviceCredentials.getIdentityServiceId())) {
                return null;
            }
        }
        Map<String, String> body = new HashMap<String, String>();
        body.put("username", webserviceCredentials.getAccount());
        body.put("password", password);
        body.put("client_id", webserviceCredentials.getClientId());
        body.put("grant_type", "password");
        body.put("client_secret", webserviceCredentials.getClientSecret());
        String response = getFormResponse(POST, this.getRelativePath() + "/realms/" + webserviceCredentials.getRealm()
                + "/protocol/openid-connect/token", body, null);
        SOSKeycloakAccountAccessToken sosKeycloakUserAccessToken = Globals.objectMapper.readValue(response, SOSKeycloakAccountAccessToken.class);
        return sosKeycloakUserAccessToken;
    }

    public boolean accountAccessTokenIsValid(SOSKeycloakAccountAccessToken sosKeycloakAccountAccessToken) throws JsonParseException,
            JsonMappingException, SOSException , Exception{
        Map<String, String> body = new HashMap<String, String>();
        body.put("client_id", webserviceCredentials.getClientId());
        body.put("token", sosKeycloakAccountAccessToken.getAccess_token());
        body.put("client_secret", webserviceCredentials.getClientSecret());
        String response = getFormResponse(POST, this.getRelativePath() + "/realms/" + webserviceCredentials.getRealm()
                + "/protocol/openid-connect/token/introspect", body, null);
        LOGGER.debug("accountTokenIsValid");
        SOSKeycloakIntrospectRepresentation sosKeycloakUserAccessToken = null;
        try {
            sosKeycloakUserAccessToken = Globals.objectMapper.readValue(response, SOSKeycloakIntrospectRepresentation.class);
        } catch (com.fasterxml.jackson.databind.exc.MismatchedInputException e) {
            LOGGER.warn("Could deserialize the response: " + response);
            throw e;
        }
        LOGGER.debug(SOSString.toString(sosKeycloakAccountAccessToken));
        boolean valid = (sosKeycloakUserAccessToken.getActive() && sosKeycloakAccountAccessToken.getExpires_in()
                - SOSAuthAccessTokenHandler.TIME_GAP_SECONDS > 0);
        return valid;
    }

    public SOSKeycloakAccountAccessToken renewAccountAccess(SOSKeycloakAccountAccessToken sosKeycloakAccountAccessToken) throws SOSException,
            JsonMappingException, JsonProcessingException, Exception {
        if (sosKeycloakAccountAccessToken != null) {
            Map<String, String> body = new HashMap<String, String>();
            body.put("client_id", webserviceCredentials.getClientId());
            body.put("grant_type", "refresh_token");
            body.put("client_secret", webserviceCredentials.getClientSecret());
            body.put("refresh_token", sosKeycloakAccountAccessToken.getRefresh_token());
            String response = getFormResponse(POST, this.getRelativePath() + "/realms/" + webserviceCredentials.getRealm()
                    + "/protocol/openid-connect/token", body, null);
            SOSKeycloakAccountAccessToken newKeycloakUserAccessToken = Globals.objectMapper.readValue(response, SOSKeycloakAccountAccessToken.class);
            LOGGER.debug(SOSString.toString(newKeycloakUserAccessToken));
            return newKeycloakUserAccessToken;
        }
        return sosKeycloakAccountAccessToken;
    }

    private SOSKeycloakAccountAccessToken getAdminAccessToken() throws SOSException, JsonMappingException, JsonProcessingException,  Exception {
        Map<String, String> body = new HashMap<String, String>();
        body.put("username", webserviceCredentials.getAdminAccount());
        body.put("password", webserviceCredentials.getAdminPassword());
        body.put("client_id", webserviceCredentials.getClientId());
        body.put("grant_type", "password");
        body.put("client_secret", webserviceCredentials.getClientSecret());
        try {
            String response = getFormResponse(POST, this.getRelativePath() + "/realms/" + webserviceCredentials.getRealm()
                    + "/protocol/openid-connect/token", body, null);
            SOSKeycloakAccountAccessToken sosKeycloakUserAccessToken = Globals.objectMapper.readValue(response, SOSKeycloakAccountAccessToken.class);
            return sosKeycloakUserAccessToken;
        } catch (JocException e) {
            LOGGER.info("Error accessing the admin account");
            throw e;
        }
    }

    private SOSKeycloakUserRepresentation getUserId(SOSKeycloakAccountAccessToken adminAccessToken) throws SOSException,
            JsonMappingException, JsonProcessingException, Exception {
        String response = getFormResponse(GET, this.getRelativePath() + "/admin/realms/" + webserviceCredentials.getRealm() + "/users/?username="
                + webserviceCredentials.getAccount(), null, adminAccessToken.getAccess_token());
        SOSKeycloakUserRepresentation[] sosKeycloakUserRepresentation = Globals.objectMapper.readValue(response,
                SOSKeycloakUserRepresentation[].class);
        if (sosKeycloakUserRepresentation.length > 0) {
            return sosKeycloakUserRepresentation[0];
        } else {
            return null;
        }
    }

    private SOSKeycloakClientRepresentation getClientId(SOSKeycloakAccountAccessToken adminAccessToken) throws SOSException,
            JsonMappingException, JsonProcessingException, Exception {
        String response = getFormResponse(GET, this.getRelativePath() + "/admin/realms/" + webserviceCredentials.getRealm() + "/clients/?clientId="
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
            SOSKeycloakUserRepresentation userRepresentation) throws SOSException, JsonMappingException, JsonProcessingException, Exception {
        String response = getFormResponse(GET, this.getRelativePath() + "/admin/realms/" + webserviceCredentials.getRealm() + "/users/"
                + userRepresentation.getId() + "/role-mappings/realm", null, adminAccessToken.getAccess_token());
        SOSKeycloakRoleRepresentation[] sosKeycloakRoleRepresentation = Globals.objectMapper.readValue(response,
                SOSKeycloakRoleRepresentation[].class);
        return sosKeycloakRoleRepresentation;
    }

    private SOSKeycloakRoleRepresentation[] getGroupRealmRoles(SOSKeycloakAccountAccessToken adminAccessToken,
            SOSKeycloakGroupRepresentation sosKeycloakGroupRepresentation) throws SOSException, JsonMappingException, JsonProcessingException, 
    Exception {
        String response = getFormResponse(GET, this.getRelativePath() + "/admin/realms/" + webserviceCredentials.getRealm() + "/groups/"
                + sosKeycloakGroupRepresentation.getId() + "/role-mappings/realm", null, adminAccessToken.getAccess_token());
        SOSKeycloakRoleRepresentation[] sosKeycloakRoleRepresentation = Globals.objectMapper.readValue(response,
                SOSKeycloakRoleRepresentation[].class);
        return sosKeycloakRoleRepresentation;
    }

    private SOSKeycloakRoleRepresentation[] getGroupClientRoles(SOSKeycloakAccountAccessToken adminAccessToken,
            SOSKeycloakGroupRepresentation sosKeycloakGroupRepresentation, SOSKeycloakClientRepresentation clientRepresentation)
            throws SOSException, JsonMappingException, JsonProcessingException, Exception {
        String response = getFormResponse(GET, this.getRelativePath() + "/admin/realms/" + webserviceCredentials.getRealm() + "/groups/"
                + sosKeycloakGroupRepresentation.getId() + "/role-mappings/clients/" + clientRepresentation.getId(), null, adminAccessToken
                        .getAccess_token());
        SOSKeycloakRoleRepresentation[] sosKeycloakRoleRepresentation = Globals.objectMapper.readValue(response,
                SOSKeycloakRoleRepresentation[].class);
        return sosKeycloakRoleRepresentation;
    }

    private SOSKeycloakRoleRepresentation[] getAccountClientRoles(SOSKeycloakAccountAccessToken adminAccessToken,
                SOSKeycloakUserRepresentation userRepresentation, SOSKeycloakClientRepresentation clientRepresentation) throws SOSException, 
            JsonMappingException, JsonProcessingException, Exception {
        String response = getFormResponse(GET, this.getRelativePath() + "/admin/realms/" + webserviceCredentials.getRealm() + "/users/"
                + userRepresentation.getId() + "/role-mappings/clients/" + clientRepresentation.getId(), null, adminAccessToken.getAccess_token());
        SOSKeycloakRoleRepresentation[] sosKeycloakRoleRepresentation = Globals.objectMapper.readValue(response,
                SOSKeycloakRoleRepresentation[].class);
        return sosKeycloakRoleRepresentation;
    }

    private SOSKeycloakGroupRepresentation[] getAccountGroups(SOSKeycloakAccountAccessToken adminAccessToken,
            SOSKeycloakUserRepresentation userRepresentation) throws SOSException, JsonMappingException, JsonProcessingException, Exception {
        String response = getFormResponse(GET, this.getRelativePath() + "/admin/realms/" + webserviceCredentials.getRealm() + "/users/"
                + userRepresentation.getId() + "/groups", null, adminAccessToken.getAccess_token());
        SOSKeycloakGroupRepresentation[] sosKeycloakGroupRepresentation = Globals.objectMapper.readValue(response,
                SOSKeycloakGroupRepresentation[].class);
        return sosKeycloakGroupRepresentation;
    }

    public Set<String> getTokenRoles() throws SOSException, JsonMappingException, JsonProcessingException, Exception {
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
