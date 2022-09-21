package com.sos.auth.openid;

import java.io.IOException;
import java.io.StringReader;
import java.net.SocketException;
import java.net.URI;
import java.security.KeyStore;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.auth.classes.SOSAuthAccessTokenHandler;
import com.sos.auth.classes.SOSAuthHelper;
import com.sos.auth.openid.classes.SOSOpenIdAccountAccessToken;
import com.sos.auth.openid.classes.SOSOpenIdWebserviceCredentials;
import com.sos.commons.exception.SOSException;
import com.sos.commons.httpclient.SOSRestApiClient;
import com.sos.commons.util.SOSString;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;

import js7.base.time.Timezone;

public class SOSOpenIdHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSOpenIdHandler.class);

    private static final String EXPIRATION_FIELD = "exp";
    private static final String EXPIRES_IN_FIELD = "expires_in";
    private static final String ISS_FIELD = "iss";
    private static final String AUD_FIELD = "aud";
    private static final String ALG_FIELD = "alg";
    private static final String EMAIL_FIELD = "email";
    private SOSOpenIdWebserviceCredentials webserviceCredentials;
    private static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String BEARER = "Bearer";
    private static final String AUTHORIZATION = "Authorization";
    private JsonObject jsonHeader;
    private JsonObject jsonPayload;
    private KeyStore truststore = null;
    private URI tokenEndpoint;

    public SOSOpenIdHandler(SOSOpenIdWebserviceCredentials webserviceCredentials, KeyStore truststore) {
        this.webserviceCredentials = webserviceCredentials;
        this.truststore = truststore;
    }

    private String getFormResponse(Boolean post, URI requestUri, Map<String, String> body, String xAccessToken) throws SOSException, SocketException {
        SOSRestApiClient restApiClient = new SOSRestApiClient();
        restApiClient.setConnectionTimeout(SOSAuthHelper.RESTAPI_CONNECTION_TIMEOUT);
        restApiClient.addHeader(CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED);
        if (!(xAccessToken == null || xAccessToken.isEmpty())) {
            restApiClient.addHeader(AUTHORIZATION, BEARER + " " + xAccessToken);
        }

        if (truststore != null) {
            restApiClient.setSSLContext(null, null, truststore);
        }

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
            jocError.setMessage(httpReplyCode + ":" + response);
            throw new JocException(jocError);

        default:
            jocError.setMessage(httpReplyCode + " " + restApiClient.getHttpResponse().getStatusLine().getReasonPhrase());
            throw new JocException(jocError);
        }

    }

    public SOSOpenIdAccountAccessToken login() throws SOSException, JsonParseException, JsonMappingException, IOException {

        SOSOpenIdAccountAccessToken sosOpenIdAccountAccessToken = new SOSOpenIdAccountAccessToken();

        String accountFromUrl = "";
        Long expiresIn = 0L;
        String account = null;
        String alg = null;
        String aud = null;
        String iss = null;

        URI requestUri = URI.create(webserviceCredentials.getAuthenticationUrl() + "/.well-known/openid-configuration");
        String configurationResponse = "";
        configurationResponse = getFormResponse(false, requestUri, null, webserviceCredentials.getAccessToken());
        JsonReader jsonReaderConfigurationResponse = Json.createReader(new StringReader(configurationResponse));
        JsonObject jsonConfigurationResponse = jsonReaderConfigurationResponse.readObject();
        String userinfoEndpoint = jsonConfigurationResponse.getString("userinfo_endpoint", "");
        tokenEndpoint = URI.create(jsonConfigurationResponse.getString("token_endpoint", ""));

        if ((userinfoEndpoint != null) && !userinfoEndpoint.isEmpty()) {
            requestUri = URI.create(userinfoEndpoint);
            String userInfoResponse = "";

            Map<String, String> body = new HashMap<String, String>();
            body.put("client_id", webserviceCredentials.getClientId());
            body.put("client_secret", webserviceCredentials.getClientSecret());
            userInfoResponse = getFormResponse(true, requestUri, body, webserviceCredentials.getAccessToken());

            JsonReader jsonReaderUserInfoResponse = Json.createReader(new StringReader(userInfoResponse));
            JsonObject jsonUserInfoResponse = jsonReaderUserInfoResponse.readObject();
            accountFromUrl = jsonUserInfoResponse.getString(EMAIL_FIELD, "");
        }

        if (jsonHeader == null) {
            this.decodeIdToken();
        }

        try {
            if (expiresIn.equals(0L)) {
                Long expiration = Long.valueOf(jsonPayload.getInt(EXPIRATION_FIELD, 0));
                expiresIn = expiration- Instant.now().getEpochSecond();
            }
            alg = jsonHeader.getString(ALG_FIELD, "");
            aud = jsonPayload.getString(AUD_FIELD, ""); // clientid
            iss = jsonPayload.getString(ISS_FIELD, ""); // url
            account = jsonPayload.getString(EMAIL_FIELD, "");

            sosOpenIdAccountAccessToken.setExpiresIn(expiresIn);

        } catch (Exception e) {
            LOGGER.warn(String.format("Could not determine expiration"));
        }

        boolean valid = true;
        valid = valid && webserviceCredentials.getClientId().equals(aud);
        valid = valid && webserviceCredentials.getAuthenticationUrl().equals(iss);
        valid = valid && webserviceCredentials.getAccount().equals(account);

        if (valid && (userinfoEndpoint != null) && !userinfoEndpoint.isEmpty()) {
            valid = valid && webserviceCredentials.getAccount().equals(accountFromUrl);
        }

        valid = valid && expiresIn > 0;

        if (valid) {
            sosOpenIdAccountAccessToken.setAccessToken(webserviceCredentials.getAccessToken());
            return sosOpenIdAccountAccessToken;
        } else {
            return null;
        }
    }

    public boolean accountAccessTokenIsValid(SOSOpenIdAccountAccessToken accessToken) {
        boolean valid = (accessToken.getExpiresIn() - SOSAuthAccessTokenHandler.TIME_GAP_SECONDS > 0);
        return valid;
    }

    public SOSOpenIdAccountAccessToken renewAccountAccess(SOSOpenIdAccountAccessToken sosOpenIdAccountAccessToken) {
        if (sosOpenIdAccountAccessToken != null) {

            Map<String, String> body = new HashMap<String, String>();
            body.put("client_id", webserviceCredentials.getClientId());
            body.put("grant_type", "refresh_token");
            body.put("client_secret", webserviceCredentials.getClientSecret());
            body.put("refresh_token", sosOpenIdAccountAccessToken.getRefreshToken());

            String response;
            try {
                response = getFormResponse(true, tokenEndpoint, body, webserviceCredentials.getAccessToken());

                JsonReader jsonReaderTokenResponse = Json.createReader(new StringReader(response));
                JsonObject jsonTokenResponse = jsonReaderTokenResponse.readObject();
                String newAccessToken = jsonTokenResponse.getString("access_token", "");

                LOGGER.debug("new access_token:" + newAccessToken);

                sosOpenIdAccountAccessToken.setAccessToken(newAccessToken);

                LOGGER.debug(SOSString.toString(sosOpenIdAccountAccessToken));
            } catch (SocketException | SOSException e) {
                LOGGER.error("", e);
            }
        }
        return sosOpenIdAccountAccessToken;
    }

    public String decodeIdToken() throws SocketException, SOSException {

        JsonReader jsonReaderHeader = null;
        JsonReader jsonReaderPayload = null;

        try {
            String[] accessTokenParts = webserviceCredentials.getIdToken().split("\\.");
            Base64.Decoder decoder = Base64.getUrlDecoder();

            String header = new String(decoder.decode(accessTokenParts[0]));
            String payload = new String(decoder.decode(accessTokenParts[1]));

            jsonReaderHeader = Json.createReader(new StringReader(header));
            jsonReaderPayload = Json.createReader(new StringReader(payload));
            jsonHeader = jsonReaderHeader.readObject();
            jsonPayload = jsonReaderPayload.readObject();
            return jsonPayload.getString(EMAIL_FIELD, "");

        } catch (Exception e) {
            LOGGER.warn(String.format("Could not decode jwt id-token"));
        } finally {
            jsonReaderHeader.close();
            jsonReaderPayload.close();
        }

        return "";
    }

}
