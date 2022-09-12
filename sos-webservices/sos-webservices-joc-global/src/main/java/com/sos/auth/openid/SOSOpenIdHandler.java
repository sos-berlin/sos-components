package com.sos.auth.openid;

import java.io.IOException;
import java.io.StringReader;
import java.util.Base64;

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
import com.sos.auth.openid.classes.SOSOpenIdWebserviceCredentials;
import com.sos.commons.exception.SOSException;

public class SOSOpenIdHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSOpenIdHandler.class);
    private SOSOpenIdWebserviceCredentials webserviceCredentials;

    public SOSOpenIdHandler(SOSAuthCurrentAccount currentAccount, SOSOpenIdWebserviceCredentials webserviceCredentials) {
        this.webserviceCredentials = webserviceCredentials;
    }

    public SOSOpenIdAccountAccessToken login(SOSAuthCurrentAccount currentAccount) throws SOSException, JsonParseException, JsonMappingException,
            IOException {

        String jsonString =
                "{\"iss\": \"https://accounts.google.com\",\"sub\": \"110169484474386276334\",\"azp\": \"1008719970978-hb24n2dstb40o45d4feuo2ukqmcc6381.apps.googleusercontent.com\",\"aud\": \"1008719970978-hb24n2dstb40o45d4feuo2ukqmcc6381.apps.googleusercontent.com\",            \"iat\": \"1433978353\",            \"exp\": \"1433981953\",     \"email\": \"testuser@gmail.com\",            \"email_verified\": \"true\",            \"name\" : \"Test User\",            \"picture\": \"https://lh4.googleusercontent.com/-kYgzyAWpZzJ/ABCDEFGHI/AAAJKLMNOP/tIXL9Ir44LE/s99-c/photo.jpg\",            \"given_name\": \"Test\",            \"family_name\": \"User\",            \"locale\": \"en\"           }";
        SOSOpenIdAccountAccessToken sosOpenIdAccountAccessToken = new SOSOpenIdAccountAccessToken();
        String accessToken =
                "eyJhbGciOiJSUzI1NiIsImtpZCI6ImNhYWJmNjkwODE5MTYxNmE5MDhhMTM4OTIyMGE5NzViM2MwZmJjYTEiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI2Mzg1MzAzNTA3OC02Y201dHY1MXBwMzRzdmoyYTZjZDk0MjFmamhsMTgxMy5hcHBzLmdvb2dsZXVzZXJjb250ZW50LmNvbSIsImF1ZCI6IjYzODUzMDM1MDc4LTZjbTV0djUxcHAzNHN2ajJhNmNkOTQyMWZqaGwxODEzLmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwic3ViIjoiMTEwMTQ3MTk2NTk0OTYxODI4NTYwIiwiZW1haWwiOiJzb3VyYWJoLmFncmF3YWwxOTAwQGdtYWlsLmNvbSIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJub25jZSI6IjMzZmQ5YjljNDdiOTU2ZGRkMjAxNDU0OTRkY2RmYTVjM2U5czlOMU4zIiwibmFtZSI6IlNvdXJhYmggYWdyYXdhbCIsInBpY3R1cmUiOiJodHRwczovL2xoMy5nb29nbGV1c2VyY29udGVudC5jb20vYS0vQUZkWnVjcHJXdmFyMTZyRTZGekNYMVgwSWlLQzk5N3U2WnVOdFlEdVI2Z0JrQT1zOTYtYyIsImdpdmVuX25hbWUiOiJTb3VyYWJoIiwiZmFtaWx5X25hbWUiOiJhZ3Jhd2FsIiwibG9jYWxlIjoiZW4tR0IiLCJpYXQiOjE2NjI3MTM4MjcsImV4cCI6MTY2MjcxNzQyNywianRpIjoiNDZhZGM5MzA4ZTk1ZjY2ZTVjNGQ3ZGViNGNlMDViNGFmMWVhNWYxMSJ9.Ig0QGHeuo5xpUDMTocAox2Xu3hQG3n1ADg9vn2tjMKP24DD7deoHZ8wmvI01Keo05kbQrT-XyqKY3YGQ-_ALsEkgY2tP0ZNDcTiaJrveuSAPgVmt7leOgyOXEEdh_nT-FYNrVhXyGFMmHKCYJB9raVMauyM2MON68TavCmHAcE8iAUX2RACYvSMHOgwCnKGc_lLQkZ23IWquWJCumz5jrUi2TL6ZL3TiFhUMyp96WxnyOmSm1ihzxQ1VRAd9CCa_9gbus7lRlaJNZxtkiOFEfFINKcqGQhzdvx2wrtItJk-FEAm86d5rP9MUcZo_d1hskzA5ABcTcW7DCn1KJDdGbA";

        String[] chunks = accessToken.split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();

        String header = new String(decoder.decode(chunks[0]));
        String payload = new String(decoder.decode(chunks[1]));

        JsonReader jsonReaderHeader = null;
        JsonReader jsonReaderPayload = null;
        Integer expiration = null;
        String account = null;
        String alg = null;
        String aud = null;
        String iss = null;

        try {
            jsonReaderHeader = Json.createReader(new StringReader(header));
            jsonReaderPayload = Json.createReader(new StringReader(payload));
            JsonObject jsonHeader = jsonReaderHeader.readObject();
            JsonObject jsonPayload = jsonReaderPayload.readObject();

            expiration = jsonPayload.getInt("exp",0);
            alg = jsonHeader.getString("alg", "");
            account = jsonPayload.getString("email", "");
            aud = jsonPayload.getString("aud", ""); //clientid
            iss = jsonPayload.getString("iss", ""); //url
            account = jsonPayload.getString("email", "");

            sosOpenIdAccountAccessToken.setExpires_in(Integer.valueOf(expiration));

            String s = webserviceCredentials.getClientId();
            s =  webserviceCredentials.getServiceUrl();

            if (true || currentAccount.getAccountname().equals(account)) {
                sosOpenIdAccountAccessToken.setAccess_token(currentAccount.getSosLoginParameters().getAccessToken());
                return sosOpenIdAccountAccessToken;
            } else {
                return null;
            }

        } catch (Exception e) {
            LOGGER.warn(String.format("Could not determine expiration"));
        } finally {
            jsonReaderHeader.close();
            jsonReaderPayload.close();
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
