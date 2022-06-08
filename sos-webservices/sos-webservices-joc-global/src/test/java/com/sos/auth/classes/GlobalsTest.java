package com.sos.auth.classes;

import com.sos.auth.classes.SOSAuthCurrentAccountAnswer;
import com.sos.auth.classes.SOSServicePermissionIam;

public class GlobalsTest {

    private static final String PASSWORD = "root";
    private static final String USER = "root";
    protected static final String AUTH_ROLE = "all";
    protected static SOSServicePermissionIam sosServicePermissionIam;
    protected static SOSAuthCurrentAccountAnswer sosAuthCurrentAccountAnswer;

    protected static String getAccessToken() throws Exception {
        sosServicePermissionIam = new SOSServicePermissionIam();
        sosAuthCurrentAccountAnswer = (SOSAuthCurrentAccountAnswer) sosServicePermissionIam.login(null, "", "", USER, PASSWORD).getEntity();
        return sosAuthCurrentAccountAnswer.getAccessToken();
    }

    protected static void logout() {
        sosServicePermissionIam.logout(sosAuthCurrentAccountAnswer.getAccessToken());
    }

}
