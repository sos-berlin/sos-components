package com.sos.auth.openid;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.net.SocketException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.auth.classes.SOSAuthAccessTokenHandler;
import com.sos.auth.classes.SOSAuthHelper;
import com.sos.auth.openid.classes.SOSJWTVerifier;
import com.sos.auth.openid.classes.SOSOpenIdAccountAccessToken;
import com.sos.auth.openid.classes.SOSOpenIdWebserviceCredentials;
import com.sos.commons.exception.SOSException;
import com.sos.commons.httpclient.SOSRestApiClient;
import com.sos.commons.sign.keys.keyStore.KeyStoreUtil;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;

public class SOSOpenIdHandler {

    private static final String PREFERRED_USERNAME = "preferred_username";
    private static final String EMAIL = "email";
    private static final String CLAIMS_SUPPORTED = "claims_supported";
    private static final String WELL_KNOWN_OPENID_CONFIGURATION = "/.well-known/openid-configuration";
    private static final String JWKS_URI_ENDPOINT = "jwks_uri";
    private static final Logger LOGGER = LoggerFactory.getLogger(SOSOpenIdHandler.class);
    private static final String EXPIRATION_FIELD = "exp";
    private static final String ISS_FIELD = "iss";
    private static final String AUD_FIELD = "aud";
    private static final String ALG_FIELD = "alg";
    private static final String KID_FIELD = "kid";
    private SOSOpenIdWebserviceCredentials webserviceCredentials;
    private static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String BEARER = "Bearer";
    private static final String AUTHORIZATION = "Authorization";
    private JsonObject jsonHeader;
    private JsonObject jsonPayload;
    private KeyStore truststore = null;
    private String accountIdentifier = null;

    public SOSOpenIdHandler(SOSOpenIdWebserviceCredentials webserviceCredentials) throws Exception {
        this.webserviceCredentials = webserviceCredentials;

        if (webserviceCredentials != null && webserviceCredentials.getTruststorePath() != null) {
            if (Files.exists(Paths.get(webserviceCredentials.getTruststorePath()))) {
                try {
                    this.truststore = KeyStoreUtil.readTrustStore(webserviceCredentials.getTruststorePath(), webserviceCredentials
                            .getTrustStoreType(), webserviceCredentials.getTruststorePassword());
                } catch (Exception e) {
                    throw e;
                }
            } else {
                throw new Exception("Truststore file " + webserviceCredentials.getTruststorePath() + " not existing");
            }
        }
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

    public String getAccountIdentifier() throws SocketException, SOSException {
        String result = "";
        if ((webserviceCredentials.getUserAttribute() != null) && (!webserviceCredentials.getUserAttribute().isEmpty())) {
            return webserviceCredentials.getUserAttribute();
        }

        URI requestUri = URI.create(webserviceCredentials.getAuthenticationUrl() + WELL_KNOWN_OPENID_CONFIGURATION);
        String configurationResponse = getFormResponse(false, requestUri, null, null);
        JsonReader jsonReaderConfigurationResponse = Json.createReader(new StringReader(configurationResponse));
        JsonObject jsonConfigurationResponse = jsonReaderConfigurationResponse.readObject();
        JsonArray claimsSupported = jsonConfigurationResponse.getJsonArray(CLAIMS_SUPPORTED);
        if (claimsSupported != null && claimsSupported.size() > 0) {
            int len = claimsSupported.size();
            for (int j = 0; j < len; j++) {
                String supported = claimsSupported.getString(j);
                if (supported.equals(PREFERRED_USERNAME)) {
                    result = PREFERRED_USERNAME;
                    break;
                }
            }
            if (result == null || result.isEmpty()) {
                for (int j = 0; j < len; j++) {
                    String supported = claimsSupported.getString(j);
                    if (supported.equals(EMAIL)) {
                        result = EMAIL;
                        break;
                    }
                }
            }
        }
        if (result == null || result.isEmpty()) {
            result = EMAIL;
            LOGGER.info("Could not get attribute name from " + CLAIMS_SUPPORTED + ". Default=" + EMAIL);
        }

        return result;
    }

    public SOSOpenIdAccountAccessToken login() throws SOSException, JsonParseException, JsonMappingException, IOException {

        SOSOpenIdAccountAccessToken sosOpenIdAccountAccessToken = new SOSOpenIdAccountAccessToken();

        Long expiresIn = 0L;
        String account = null;
        String alg = null;
        String kid = null;
        String aud = null;
        String iss = null;

        URI requestUri = URI.create(webserviceCredentials.getAuthenticationUrl() + WELL_KNOWN_OPENID_CONFIGURATION);
        String configurationResponse = "";
        configurationResponse = getFormResponse(false, requestUri, null, null);
        JsonReader jsonReaderConfigurationResponse = Json.createReader(new StringReader(configurationResponse));
        JsonObject jsonConfigurationResponse = jsonReaderConfigurationResponse.readObject();
        String tokenVerificationEndpoint = "";

        String certEndpoit = jsonConfigurationResponse.getString(JWKS_URI_ENDPOINT, "");

        boolean tokenIsActive = true;
        if ((tokenVerificationEndpoint != null) && !tokenVerificationEndpoint.isEmpty()) {
            requestUri = URI.create(tokenVerificationEndpoint);
            String tokenVerificationResponse = "";

            Map<String, String> body = new HashMap<String, String>();
            body.put("client_id", webserviceCredentials.getClientId());
            body.put("client_secret", webserviceCredentials.getClientSecret());
            body.put("token", webserviceCredentials.getIdToken());
            tokenVerificationResponse = getFormResponse(true, requestUri, body, null);

            JsonReader jsonReaderTokenVerificationResponse = Json.createReader(new StringReader(tokenVerificationResponse));
            JsonObject jsonTokenVerificationResponse = jsonReaderTokenVerificationResponse.readObject();
            tokenIsActive = jsonTokenVerificationResponse.getBoolean("active", false);
        }

        if (jsonHeader == null) {
            this.decodeIdToken(webserviceCredentials.getIdToken());
        }

        try {
            if (expiresIn.equals(0L)) {
                Long expiration = Long.valueOf(jsonPayload.getInt(EXPIRATION_FIELD, 0));
                expiresIn = expiration - Instant.now().getEpochSecond();
            }
            alg = jsonHeader.getString(ALG_FIELD, "");
            kid = jsonHeader.getString(KID_FIELD, "");
            aud = jsonPayload.getString(AUD_FIELD, ""); // clientid
            iss = jsonPayload.getString(ISS_FIELD, ""); // url
            account = jsonPayload.getString(accountIdentifier, "");

            sosOpenIdAccountAccessToken.setExpiresIn(expiresIn);

        } catch (Exception e) {
            LOGGER.warn(String.format("Could not determine expiration"));
        }

        boolean valid = true;
        valid = valid && tokenIsActive;
        valid = valid && webserviceCredentials.getClientId().equals(aud);
        valid = valid && webserviceCredentials.getAuthenticationUrl().equals(iss);
        valid = valid && webserviceCredentials.getAccount().equals(account);

        valid = valid && expiresIn > 0;
        try {
            RSAPublicKey publicKey = this.getPublicKey(webserviceCredentials, certEndpoit, kid);
            valid = valid && (SOSJWTVerifier.verify(webserviceCredentials, alg, publicKey).getHeader() != null);
        } catch (CertificateException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            LOGGER.error("", e);
            valid = false;
        }

        if (valid) {
            sosOpenIdAccountAccessToken.setAccessToken(SOSAuthHelper.createAccessToken());
            return sosOpenIdAccountAccessToken;
        } else {
            return null;
        }
    }

    public boolean accountAccessTokenIsValid(SOSOpenIdAccountAccessToken accessToken) {
        boolean valid = (accessToken.getExpiresIn() - SOSAuthAccessTokenHandler.TIME_GAP_SECONDS > 0);
        return valid;
    }

    public RSAPublicKey getPublicKey(SOSOpenIdWebserviceCredentials webserviceCredentials, String certEndpoit, String kid)
            throws NoSuchAlgorithmException, InvalidKeySpecException, SocketException, SOSException {

        URI certEndpointUri = URI.create(certEndpoit);

        String response;
        response = getFormResponse(false, certEndpointUri, null, null);

        JsonReader jsonReaderCertResponse = Json.createReader(new StringReader(response));
        JsonObject jsonKeys = jsonReaderCertResponse.readObject();
        JsonArray keys = jsonKeys.getJsonArray("keys");

        int len = keys.size();
        String eValue = "";
        String nValue = "";
        String ktyValue = "";

        for (int j = 0; j < len; j++) {
            JsonObject json = keys.getJsonObject(j);
            String k = json.getString("kid");
            if (k.equals(kid)) {
                eValue = json.getString("e");
                nValue = json.getString("n");
                ktyValue = json.getString("kty");
            }
        }

        byte[] nBytes = Base64.getUrlDecoder().decode(nValue);
        byte[] eBytes = Base64.getUrlDecoder().decode(eValue);

        BigInteger n = new BigInteger(1, nBytes);
        BigInteger e = new BigInteger(1, eBytes);

        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(n, e);
        KeyFactory keyFactory = KeyFactory.getInstance(ktyValue); // ktyValue will be "RSA"
        RSAPublicKey rsaPublicKey = (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);
        return rsaPublicKey;
    }

    public String decodeIdToken(String idToken) throws SocketException, SOSException {

        JsonReader jsonReaderHeader = null;
        JsonReader jsonReaderPayload = null;

        try {

            if (accountIdentifier == null) {
                accountIdentifier = getAccountIdentifier();
            }
            String[] accessTokenParts = idToken.split("\\.");
            Base64.Decoder decoder = Base64.getUrlDecoder();

            String header = new String(decoder.decode(accessTokenParts[0]));
            String payload = new String(decoder.decode(accessTokenParts[1]));

            jsonReaderHeader = Json.createReader(new StringReader(header));
            jsonReaderPayload = Json.createReader(new StringReader(payload));
            jsonHeader = jsonReaderHeader.readObject();
            jsonPayload = jsonReaderPayload.readObject();
            return jsonPayload.getString(accountIdentifier, "");

        } catch (Exception e) {
            LOGGER.warn(String.format("Could not decode jwt id-token"));
            throw e;
        } finally {
            if (jsonReaderHeader != null) {
                jsonReaderHeader.close();
                jsonReaderPayload.close();
            }
        }
    }

}
