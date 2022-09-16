package com.sos.auth.openid;

import java.io.IOException;
import java.io.StringReader;
import java.net.SocketException;
import java.net.URI;
import java.time.Instant;
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
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;

public class SOSOpenIdHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSOpenIdHandler.class);
    private SOSOpenIdWebserviceCredentials webserviceCredentials;
    private static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String BEARER = "Bearer";
    private static final String AUTHORIZATION = "Authorization";
    private JsonObject jsonHeader;
    private JsonObject jsonPayload;

    public SOSOpenIdHandler(SOSOpenIdWebserviceCredentials webserviceCredentials) {
        this.webserviceCredentials = webserviceCredentials;
    }

    private String getFormResponse(Boolean post, URI requestUri, Map<String, String> body, String xAccessToken) throws SOSException, SocketException {
        SOSRestApiClient restApiClient = new SOSRestApiClient();
        restApiClient.setConnectionTimeout(SOSAuthHelper.RESTAPI_CONNECTION_TIMEOUT);
        restApiClient.addHeader(CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED);
        if (!(xAccessToken == null || xAccessToken.isEmpty())) {
            restApiClient.addHeader(AUTHORIZATION, BEARER + " " + xAccessToken);
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
        // webserviceCredentials.setIdToken("eyJhbGciOiJSUzI1NiIsImtpZCI6ImNhYWJmNjkwODE5MTYxNmE5MDhhMTM4OTIyMGE5NzViM2MwZmJjYTEiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI2Mzg1MzAzNTA3OC02Y201dHY1MXBwMzRzdmoyYTZjZDk0MjFmamhsMTgxMy5hcHBzLmdvb2dsZXVzZXJjb250ZW50LmNvbSIsImF1ZCI6IjYzODUzMDM1MDc4LTZjbTV0djUxcHAzNHN2ajJhNmNkOTQyMWZqaGwxODEzLmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwic3ViIjoiMTA2MTk5MDAwNTEwNzAxOTA1NzYyIiwiZW1haWwiOiJ1d2Uucmlzc2VAc29zLWJlcmxpbi5jb20iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiYXRfaGFzaCI6IkVKLWVhOWhLYXpaZEtRV196ak83elEiLCJub25jZSI6IlpsOWxlVFJPV0Y5aFlYcDBWMmxsT0dFMVFXZFJMVVJ1YlhkSGRVMVJRVVJvUXpkaVNVTnBkWGhRYlRGSSIsIm5hbWUiOiJVd2UgUmlzc2UiLCJwaWN0dXJlIjoiaHR0cHM6Ly9saDMuZ29vZ2xldXNlcmNvbnRlbnQuY29tL2EvQUl0YnZta3RHOVktNDRoTDR5Q0J1N3M1V2ZDZEl6NFZMLXRidVZkSE11MTU9czk2LWMiLCJnaXZlbl9uYW1lIjoiVXdlIiwiZmFtaWx5X25hbWUiOiJSaXNzZSIsImxvY2FsZSI6ImRlIiwiaWF0IjoxNjYzMTUyMzQ1LCJleHAiOjE2NjMxNTU5NDUsImp0aSI6IjUzYWZjMWNhZWE5ZjVlYmEwMTQ4NmE2NWFhOTczOTc5ZWRhOTMxOGEifQ.lbzh3LfnFT6I1jYKutnRKoavNCoYLadLkVEsh3T4NkyIlNONdLl1d-xPTNCdeeE9dQHD6YgbbrcQ7BQZhKf9JD4ipv5qrLSPg9FmmVMCjCfMLeshyxaSu2tgNIcIRFWan8YZ2P-geRIC_VZXA-coEmIGrV61sdylTygUD2QG-frh2E54zqR36ySWQ8dUl0YxqVOiEM4C9iZHGsaWL-45g15CpzZcrJmC6XYGfa2tu7xVNM7RdoUFwxa4eAGs04E77xGRnLHfDrEY_zNEHzoAALyLDmPUycTMK5ptnfocCyIh4p7tBQxIn4Dzx5DIXciFTeGTOiN0C2navGK66Mlw2g");

        String accountFromUrl = "";
        Integer expiresIn = 0;
        String account = null;
        String alg = null;
        String aud = null;
        String iss = null;
        String tokenVerificationUrl = webserviceCredentials.getTokenVerificationUrl();

        if ((tokenVerificationUrl != null) && !tokenVerificationUrl.isEmpty()) {
            URI requestUri = URI.create(tokenVerificationUrl);
            String tokenVerificationResponse = "";
            if (!tokenVerificationUrl.equals(webserviceCredentials.getOriginalTokenVerificationUrl())) {
                tokenVerificationResponse = getFormResponse(false, requestUri, null, null);
            } else {

                Map<String, String> body = new HashMap<String, String>();
                body.put("username", webserviceCredentials.getAccount());
                body.put("client_id", webserviceCredentials.getClientId());
                body.put("client_secret", webserviceCredentials.getClientSecret());
                tokenVerificationResponse = getFormResponse(true, requestUri, body, webserviceCredentials.getAccessToken());
            }
            JsonReader jsonReaderTokenVerificationResponse = Json.createReader(new StringReader(tokenVerificationResponse));
            JsonObject jsonTokenVerificationResponse = jsonReaderTokenVerificationResponse.readObject();
            accountFromUrl = jsonTokenVerificationResponse.getString(webserviceCredentials.getJwtEmailField(), "");
            expiresIn = Integer.valueOf(jsonTokenVerificationResponse.getString(webserviceCredentials.getExpiresInField(), "0"));

        }

        if (webserviceCredentials.getIsJwtToken()) {

            if (jsonHeader == null) {
                this.decodeIdToken();
            }

            try {
                if (expiresIn.equals(0)) {
                    Long expiration = Long.valueOf(jsonPayload.getInt(webserviceCredentials.getJwtExpirationField(), 0));
                    Long e = expiration - Instant.now().toEpochMilli();
                    expiresIn = e.intValue();
                }
                alg = jsonHeader.getString(webserviceCredentials.getJwtAlgorithmField(), "");
                account = jsonPayload.getString(webserviceCredentials.getJwtEmailField(), "");
                aud = jsonPayload.getString(webserviceCredentials.getJwtClientIdField(), ""); // clientid
                iss = jsonPayload.getString(webserviceCredentials.getJwtUrlField(), ""); // url
                account = jsonPayload.getString(webserviceCredentials.getJwtEmailField(), "");

                sosOpenIdAccountAccessToken.setExpiresIn(Integer.valueOf(expiresIn));

            } catch (Exception e) {
                LOGGER.warn(String.format("Could not determine expiration"));
            }
        }

        boolean valid = true;
        if (webserviceCredentials.getIsJwtToken()) {
            valid = valid && webserviceCredentials.getClientId().equals(aud);
            valid = valid && webserviceCredentials.getAuthenticationUrl().equals(iss);
            valid = valid && webserviceCredentials.getAccount().equals(account);
        }
        if (valid && (webserviceCredentials.getTokenVerificationUrl() != null) && !webserviceCredentials.getTokenVerificationUrl().isEmpty()) {
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

    public SOSOpenIdAccountAccessToken renewAccountAccess(SOSOpenIdAccountAccessToken accessToken) {
        return accessToken;
    }

    public void logout(String accessToken) throws SocketException, SOSException {
        URI requestUri = URI.create(webserviceCredentials.getLogoutUrl());
        getFormResponse(false, requestUri, null, null);

    }

    public String decodeIdToken() throws SocketException, SOSException {

        if (webserviceCredentials.getIsJwtToken()) {
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
                return jsonPayload.getString(webserviceCredentials.getJwtEmailField(), "");

            } catch (Exception e) {
                LOGGER.warn(String.format("Could not decode jwt id-token"));
            } finally {
                jsonReaderHeader.close();
                jsonReaderPayload.close();
            }
        } else {
            String tokenVerificationUrl = webserviceCredentials.getTokenVerificationUrl();

            if ((tokenVerificationUrl != null) && !tokenVerificationUrl.isEmpty()) {
                URI requestUri = URI.create(tokenVerificationUrl);
                String tokenVerificationResponse = "";
                if (!tokenVerificationUrl.equals(webserviceCredentials.getOriginalTokenVerificationUrl())) {
                    tokenVerificationResponse = getFormResponse(false, requestUri, null, null);
                } else {
                    Map<String, String> body = new HashMap<String, String>();
                    body.put("username", webserviceCredentials.getAccount());
                    body.put("client_id", webserviceCredentials.getClientId());
                    body.put("client_secret", webserviceCredentials.getClientSecret());
                    tokenVerificationResponse = getFormResponse(true, requestUri, body, webserviceCredentials.getAccessToken());
                }
                JsonReader jsonReaderTokenVerificationResponse = Json.createReader(new StringReader(tokenVerificationResponse));
                JsonObject jsonTokenVerificationResponse = jsonReaderTokenVerificationResponse.readObject();
                return jsonTokenVerificationResponse.getString(webserviceCredentials.getJwtEmailField(), "");
            }

        }

        return "";
    }

}
