package com.sos.auth.openid;

import java.io.IOException;
import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.auth.classes.SOSAuthAccessTokenHandler;
import com.sos.auth.classes.SOSAuthCurrentAccount;
import com.sos.auth.openid.classes.SOSOpenIdAccountAccessToken;
import com.sos.commons.exception.SOSException;
import com.sos.joc.model.publish.ControllerObject;

public class SOSOpenIdHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSOpenIdHandler.class);

    public SOSOpenIdHandler(SOSAuthCurrentAccount currentAccount) {
    }

    public SOSOpenIdAccountAccessToken login(SOSAuthCurrentAccount currentAccount) throws SOSException, JsonParseException, JsonMappingException,
            IOException {

        String jsonString =
                "{\"iss\": \"https://accounts.google.com\",\"sub\": \"110169484474386276334\",\"azp\": \"1008719970978-hb24n2dstb40o45d4feuo2ukqmcc6381.apps.googleusercontent.com\",\"aud\": \"1008719970978-hb24n2dstb40o45d4feuo2ukqmcc6381.apps.googleusercontent.com\",            \"iat\": \"1433978353\",            \"exp\": \"1433981953\",            // These seven fields are only included when the user has granted the \"profile\" and            // \"email\" OAuth scopes to the application.            \"email\": \"testuser@gmail.com\",            \"email_verified\": \"true\",            \"name\" : \"Test User\",            \"picture\": \"https://lh4.googleusercontent.com/-kYgzyAWpZzJ/ABCDEFGHI/AAAJKLMNOP/tIXL9Ir44LE/s99-c/photo.jpg\",            \"given_name\": \"Test\",            \"family_name\": \"User\",            \"locale\": \"en\"           }";
        SOSOpenIdAccountAccessToken sosOpenIdAccountAccessToken = new SOSOpenIdAccountAccessToken();

        JsonReader jsonReader = null;
        String expiration = null;
        String account = null;

        try {
            jsonReader = Json.createReader(new StringReader(jsonString));
            JsonObject json = jsonReader.readObject();
            expiration = json.getString("exp", "0");
            account = json.getString("email", "");

            sosOpenIdAccountAccessToken.setAccess_token(currentAccount.getAccessToken());
            sosOpenIdAccountAccessToken.setExpires_in(Integer.valueOf(expiration));

            if (currentAccount.getAccountname().equals(account)) {
                return sosOpenIdAccountAccessToken;
            } else {
                return null;
            }

        } catch (Exception e) {
            LOGGER.warn(String.format("Could not determine expiration"));
        } finally {
            jsonReader.close();
        }

        return sosOpenIdAccountAccessToken;
    }

    public boolean accountAccessTokenIsValid(SOSOpenIdAccountAccessToken accessToken) {
        boolean valid = (accessToken.getExpires_in() - SOSAuthAccessTokenHandler.TIME_GAP_SECONDS > 0);
        return valid;
    }

    public SOSOpenIdAccountAccessToken renewAccountAccess(SOSOpenIdAccountAccessToken accessToken) {
        // TODO Auto-generated method stub
        return accessToken;
    }

}
