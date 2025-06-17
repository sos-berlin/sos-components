package com.sos.auth.classes;

import com.fasterxml.jackson.core.JsonProcessingException;

public class GlobalsTest {

    private static final String PASSWORD = "root";
    private static final String USER = "root";
    protected static final String AUTH_ROLE = "all";
    protected static SOSServicePermissionIam sosServicePermissionIam;
    protected static SOSAuthCurrentAccountAnswer sosAuthCurrentAccountAnswer;

    protected static String getAccessToken() throws Exception {
        SOSLoginParameters sosLoginParameters = new SOSLoginParameters();
        sosLoginParameters.setAccount(USER);
         sosServicePermissionIam = new SOSServicePermissionIam();
        sosAuthCurrentAccountAnswer = (SOSAuthCurrentAccountAnswer) sosServicePermissionIam.login(sosLoginParameters, PASSWORD).getEntity();
        return sosAuthCurrentAccountAnswer.getAccessToken();
    }

    protected static void logout() throws JsonProcessingException {
        sosServicePermissionIam.logout(sosAuthCurrentAccountAnswer.getAccessToken());
    }

}
