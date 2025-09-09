package com.sos.auth.openid;

import java.io.StringReader;
import java.math.BigInteger;
import java.net.SocketException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.auth.classes.SOSAuthAccessTokenHandler;
import com.sos.auth.classes.SOSAuthHelper;
import com.sos.auth.openid.classes.SOSJWTVerifier;
import com.sos.auth.openid.classes.SOSOpenIdAccountAccessToken;
import com.sos.auth.openid.classes.SOSOpenIdWebserviceCredentials;
import com.sos.commons.exception.SOSException;
import com.sos.commons.httpclient.SOSRestApiClient;
import com.sos.commons.sign.keys.keyStore.KeyStoreUtil;
import com.sos.commons.util.SOSString;
import com.sos.joc.Globals;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.security.oidc.OpenIdConfiguration;
import com.sos.joc.model.security.properties.oidc.OidcFlowTypes;

public class SOSOpenIdHandler {

    public static final String PREFERRED_USERNAME = "preferred_username";
    private static final String CLIENT_CREDENTIAL_APP_ID = "appid";
    public static final String EMAIL = "email";
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
//    private static final String BEARER = "Bearer";
//    private static final String AUTHORIZATION = "Authorization";
    private JsonObject jsonHeader;
    private JsonObject jsonPayload;
    private KeyStore truststore = null;
    private String accountIdentifier = null;
    private String kid;
    String openidConfiguration;

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

    private String getFormResponse(URI requestUri) throws SOSException, SocketException {
        SOSRestApiClient restApiClient = new SOSRestApiClient();
        restApiClient.setConnectionTimeout(SOSAuthHelper.RESTAPI_CONNECTION_TIMEOUT);
        restApiClient.addHeader(CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED);

        if (truststore != null) {
            restApiClient.setSSLContext(null, null, truststore);
        }

        LOGGER.debug(requestUri.toString());

        String response;

        response = restApiClient.getRestService(requestUri);

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

    private String getAccountIdentifier() throws SocketException, SOSException, JsonMappingException, JsonProcessingException {
        String result = "";
        if ((webserviceCredentials.getUserAttribute() != null) && (!webserviceCredentials.getUserAttribute().isEmpty())) {
            return webserviceCredentials.getUserAttribute();
        }

        if (webserviceCredentials.getFlowType().equals(OidcFlowTypes.CLIENT_CREDENTIAL)) {
            return CLIENT_CREDENTIAL_APP_ID;
        }

        if (openidConfiguration == null) {
            if (webserviceCredentials.getOpenidConfiguration() == null) {
                URI requestUri = URI.create(webserviceCredentials.getAuthenticationUrl() + WELL_KNOWN_OPENID_CONFIGURATION);
                openidConfiguration = getFormResponse(requestUri);
            } else {
                openidConfiguration = new String(Base64.getUrlDecoder().decode(webserviceCredentials.getOpenidConfiguration()),
                        StandardCharsets.UTF_8);
            }
        }
        
        OpenIdConfiguration oic = Globals.objectMapper.readValue(openidConfiguration, OpenIdConfiguration.class);
        List<String> claimsSupported = oic.getClaims_supported();
        if (claimsSupported != null) {
            if (claimsSupported.contains(PREFERRED_USERNAME)) {
                result = PREFERRED_USERNAME;
            }
            if (claimsSupported.contains(EMAIL)) {
                result = EMAIL;
            }
        }

//        JsonReader jsonReaderConfigurationResponse = Json.createReader(new StringReader(openidConfiguration));
//        JsonObject jsonConfigurationResponse = jsonReaderConfigurationResponse.readObject();
//        JsonArray claimsSupported = jsonConfigurationResponse.getJsonArray(CLAIMS_SUPPORTED);
//        if (claimsSupported != null && claimsSupported.size() > 0) {
//            int len = claimsSupported.size();
//            for (int j = 0; j < len; j++) {
//                String supported = claimsSupported.getString(j);
//                if (supported.equals(PREFERRED_USERNAME)) {
//                    result = PREFERRED_USERNAME;
//                    break;
//                }
//            }
//            if (result == null || result.isEmpty()) {
//                for (int j = 0; j < len; j++) {
//                    String supported = claimsSupported.getString(j);
//                    if (supported.equals(EMAIL)) {
//                        result = EMAIL;
//                        break;
//                    }
//                }
//            }
//        }
        if (result == null || result.isEmpty()) {
            result = EMAIL;
            LOGGER.info("Could not get attribute name from " + CLAIMS_SUPPORTED + ". Default=" + EMAIL);
        }

        return result;
    }

    public SOSOpenIdAccountAccessToken login() throws Exception {

        SOSOpenIdAccountAccessToken sosOpenIdAccountAccessToken = new SOSOpenIdAccountAccessToken();

        Long expiresIn = 0L;
        String account = null;
        String alg = null;
        kid = null;
        String aud = null;
        String iss = null;

        if (webserviceCredentials.getOpenidConfiguration() == null) {
            URI requestUri = URI.create(webserviceCredentials.getAuthenticationUrl() + WELL_KNOWN_OPENID_CONFIGURATION);
            openidConfiguration = getFormResponse(requestUri);
        } else {
            openidConfiguration = new String(Base64.getUrlDecoder().decode(webserviceCredentials.getOpenidConfiguration()), StandardCharsets.UTF_8);
        }

        JsonReader jsonReaderConfigurationResponse = null;
        try {
            jsonReaderConfigurationResponse = Json.createReader(new StringReader(openidConfiguration));
            JsonObject jsonConfigurationResponse = jsonReaderConfigurationResponse.readObject();

            String certEndpoit = jsonConfigurationResponse.getString(JWKS_URI_ENDPOINT, "");
            if (jsonHeader == null) {
                this.decodeIdToken(webserviceCredentials.getIdToken());
            }

            try {
                if (expiresIn == 0L) {
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
            if (webserviceCredentials.getFlowType().equals(OidcFlowTypes.CLIENT_CREDENTIAL)) {
                valid = valid && webserviceCredentials.getClientId().equals(account);
            } else {
                valid = valid && webserviceCredentials.getClientId().equals(aud);
            }
            valid = valid && webserviceCredentials.getAuthenticationUrl().equals(iss);
            valid = valid && webserviceCredentials.getAccount().equals(account);

            valid = valid && expiresIn > 0;
            if (valid) {
                try {
                    RSAPublicKey publicKey = this.getPublicKey(webserviceCredentials, certEndpoit, kid);
                    valid = valid && (SOSJWTVerifier.verify(webserviceCredentials, alg, publicKey).getHeader() != null);
                } catch (CertificateException | NoSuchAlgorithmException | InvalidKeySpecException e) {
                    LOGGER.error("", e);
                    valid = false;
                }
            }

            if (valid) {
                sosOpenIdAccountAccessToken.setAccessToken(SOSAuthHelper.createAccessToken());
                return sosOpenIdAccountAccessToken;
            } else {
                return null;
            }
        } finally {
            if (jsonReaderConfigurationResponse != null) {
                jsonReaderConfigurationResponse.close();
            }
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
        response = getFormResponse(certEndpointUri);

        JsonReader jsonReaderCertResponse = null;
        try {
            jsonReaderCertResponse = Json.createReader(new StringReader(response));
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
        } finally {
            if (jsonReaderCertResponse != null) {
                jsonReaderCertResponse.close();
            }
        }
    }

    public String decodeIdToken(String idToken) throws Exception {
        
        String accountNameClaim = this.webserviceCredentials.getAccountNameClaim();

        JsonReader jsonReaderHeader = null;
        JsonReader jsonReaderPayload = null;

        try {

            if (accountIdentifier == null) {
                accountIdentifier = getAccountIdentifier();
            }
            String[] accessTokenParts = idToken.split("\\.");
            Base64.Decoder decoder = Base64.getUrlDecoder();

            String header = new String(decoder.decode(accessTokenParts[0]), StandardCharsets.UTF_8);
            String payload = new String(decoder.decode(accessTokenParts[1]), StandardCharsets.UTF_8);

            jsonReaderHeader = Json.createReader(new StringReader(header));
            jsonReaderPayload = Json.createReader(new StringReader(payload));
            jsonHeader = jsonReaderHeader.readObject();
            jsonPayload = jsonReaderPayload.readObject();
            
            if (!SOSString.isEmpty(accountNameClaim)) {
                JsonValue jv = jsonPayload.get(accountNameClaim);
                if (jv != null && jv.getValueType().equals(ValueType.STRING)) {
                    accountIdentifier = accountNameClaim;
                } else {
                    accountIdentifier = EMAIL;
                    LOGGER.info("AccountName claim '" + accountNameClaim + "' is not supported. '" + accountIdentifier + "' is used instead.");
                }
            }
            return jsonPayload.getString(accountIdentifier, "");

        } catch (Exception e) {
            LOGGER.warn(String.format("Could not decode jwt id-token"));
            throw e;
        } finally {
            if (jsonReaderHeader != null) {
                jsonReaderHeader.close();
            }
            if (jsonReaderPayload != null) {
                jsonReaderPayload.close();
            }
        }
    }

    public Set<String> getTokenRoles() {

        Set<String> roles = new HashSet<String>();
        JsonReader jsonReaderHeader = null;
        JsonReader jsonReaderPayload = null;
        String idToken = webserviceCredentials.getIdToken();

        try {

            String[] accessTokenParts = idToken.split("\\.");
            Base64.Decoder decoder = Base64.getUrlDecoder();

            String header = new String(decoder.decode(accessTokenParts[0]), StandardCharsets.UTF_8);
            String payload = new String(decoder.decode(accessTokenParts[1]), StandardCharsets.UTF_8);

            jsonReaderHeader = Json.createReader(new StringReader(header));
            jsonReaderPayload = Json.createReader(new StringReader(payload));
            jsonHeader = jsonReaderHeader.readObject();
            jsonPayload = jsonReaderPayload.readObject();
            
            Map<String, List<String>> groupRolesMap = webserviceCredentials.getGroupRolesMap();

            if (webserviceCredentials.getClaims() != null && groupRolesMap != null) {

                for (String claim : webserviceCredentials.getClaims()) {
                    JsonValue jv = jsonPayload.get(claim);
                    if (jv == null) {
                        LOGGER.info("Configured claim <" + claim + "> not found in JWT Id-Token");
                        continue;
                    }
                    if (jv.getValueType().equals(ValueType.ARRAY)) {
                        JsonArray array = jv.asJsonArray();
                        for (int i = 0; i < array.size(); i++) {
                            String group = array.getString(i);
                            addRoles(group, groupRolesMap, roles);
                        }
                    } else if (jv.getValueType().equals(ValueType.STRING)) {
                        String group = jsonPayload.getString(claim);
                        addRoles(group, groupRolesMap, roles);
                    }
                }
            }

            return roles;

        } finally {
            if (jsonReaderHeader != null) {
                jsonReaderHeader.close();
            }
            if (jsonReaderPayload != null) {
                jsonReaderPayload.close();
            }
        }
    }
    
    private void addRoles(String group, Map<String, List<String>> groupRolesMap, Set<String> roles) {
        LOGGER.debug("--------- account is member of group:" + group);
        List<String> mappedRoles = groupRolesMap.get(group);
        if (mappedRoles != null) {
            for (String mappedRole : mappedRoles) {
                LOGGER.debug("--------- role added:" + mappedRole);
                roles.add(mappedRole);
            }
        } else {
            LOGGER.debug("---------  group:" + group + " not found in group/roles mapping");
        }
    }

    public String getKid() {
        return kid;
    }

}
