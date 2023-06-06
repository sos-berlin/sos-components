package com.sos.auth.fido2.classes;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

public class SOSFido2ClientData {

    private static final String CHALLENGE = "challenge";
    private static final String ORIGIN = "origin";
    private String challenge;
    private String origin;
    private byte[] clientDataJsonDecoded;
    private byte[] challengeDecoded;
    String challengeDecodedString;

    public SOSFido2ClientData(String encodedClientDataJson) {
        super();
        read(encodedClientDataJson);
    }

    public String getChallenge() {
        return challenge;
    }

    public String getOrigin() {
        return origin;
    }

    public void read(String encodedClientDataJson) {

        clientDataJsonDecoded = Base64.getDecoder().decode(encodedClientDataJson);

        String clientDataJson = new String(clientDataJsonDecoded, StandardCharsets.UTF_8);
        JsonReader jsonReader = Json.createReader(new StringReader(clientDataJson));
        JsonObject jsonClientData = jsonReader.readObject();
        origin = jsonClientData.getString(ORIGIN, "");
        challenge = jsonClientData.getString(CHALLENGE, "");
        challengeDecoded = Base64.getDecoder().decode(challenge);
        challengeDecodedString = new String(challengeDecoded, StandardCharsets.UTF_8);

    }

    public byte[] getClientDataJsonDecoded() {
        return clientDataJsonDecoded;
    }

    public byte[] getChallengeDecoded() {
        return challengeDecoded;
    }

    
    public String getChallengeDecodedString() {
        return challengeDecodedString;
    }

}
