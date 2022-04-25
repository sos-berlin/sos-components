package com.sos.jitl.jobs.checkhistory.classes;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.exception.SOSException;
import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.httpclient.SOSRestApiClient;
import com.sos.commons.httpclient.exception.SOSSSLException;
import com.sos.commons.sign.keys.keyStore.KeyStoreUtil;
import com.sos.commons.sign.keys.keyStore.KeystoreType;

public class ApiAccessToken {

    private static final String NOT_VALID = "not-valid";
    private SOSRestApiClient jocRestApiClient;
    private String jocUrl;
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiAccessToken.class);

    public ApiAccessToken(String jocUrl) {
        super();
        this.jocUrl = jocUrl;
    }

   
    private void createRestApiClient(WebserviceCredentials webserviceCredentials) {
        if (jocRestApiClient == null) {
            jocRestApiClient = new SOSRestApiClient();

            jocRestApiClient.addHeader("Content-Type", "application/json");
            jocRestApiClient.addHeader("Accept", "application/json");          
        }
    }

    private JsonObject jsonFromString(String jsonObjectStr) {
        if ("".equals(jsonObjectStr) || jsonObjectStr == null) {
            jsonObjectStr = "{}";
        }
        JsonReader jsonReader = Json.createReader(new StringReader(jsonObjectStr));
        JsonObject object = jsonReader.readObject();
        jsonReader.close();
        return object;

    }

    private boolean isValid(JsonObject jsonAnswer) {
        return !(jsonAnswer.get("accessToken") == null || jsonAnswer.getString("accessToken").isEmpty() || NOT_VALID.equals(jsonAnswer.getString(
                "accessToken")));
    }

    public boolean isValidAccessToken(String xAccessToken, WebserviceCredentials webserviceCredentials) throws SOSException, URISyntaxException {

        boolean valid = false;
        if (xAccessToken == null || xAccessToken.isEmpty() || jocUrl == null || jocUrl.isEmpty()) {
            LOGGER.debug("Empty Access-Token or empty jocUrl");
            return false;
        }

        createRestApiClient(webserviceCredentials);
        jocRestApiClient.addHeader("X-Access-Token", xAccessToken);

        String s = jocUrl + "/authentication/userbytoken";
        LOGGER.debug("uri:" + s);
        String answer = jocRestApiClient.postRestService(new URI(s), "");
        LOGGER.debug("answer:" + answer);

        JsonObject userByTokenAnswer = jsonFromString(answer);
        valid = isValid(userByTokenAnswer);
        if (valid) {
            s = jocUrl + "/controller/ids";
            LOGGER.debug("uri:" + s);
            answer = jocRestApiClient.postRestService(new URI(s), "");
            LOGGER.debug("answer:" + answer);

            JsonObject schedulerIds = jsonFromString(answer);
            valid = (schedulerIds.get("error") == null);
        }
        return valid;

    }

    public String login(WebserviceCredentials webserviceCredentials) throws SOSException, URISyntaxException {
        createRestApiClient(webserviceCredentials);
        jocRestApiClient.addAuthorizationHeader(webserviceCredentials.getUserDecodedAccount());

        String s = jocUrl + "/authentication/login";
        LOGGER.debug("uri:" + s);
        String answer = jocRestApiClient.postRestService(new URI(s), "");

        LOGGER.debug("answer:" + answer);
        JsonObject login = jsonFromString(answer);
        if (login.get("accessToken") != null) {
            return login.getString("accessToken");
        } else {
            LOGGER.error(login.toString());
            return "";
        }
    }

    public void setJocUrl(String jocUrl) {
        this.jocUrl = jocUrl;
    }

    
    public SOSRestApiClient getJocRestApiClient() {
        return jocRestApiClient;
    }

}
