package com.sos.auth.classes;

import com.sos.auth.classes.SOSAuthCurrentAccountAnswer;
import com.sos.auth.classes.SOSServicePermissionIam;

public class GlobalsTest {

    private static final String PASSWORD = "root";
    private static final String USER = "root";
    protected static final String SHIRO_ROLE = "all";
    protected static SOSServicePermissionIam sosServicePermissionShiro;
    protected static SOSAuthCurrentAccountAnswer sosShiroCurrentUserAnswer;

    protected static String getAccessToken() throws Exception {
        sosServicePermissionShiro = new SOSServicePermissionIam();
        sosShiroCurrentUserAnswer = (SOSAuthCurrentAccountAnswer) sosServicePermissionShiro.login(null, "","", USER, PASSWORD).getEntity();
        return sosShiroCurrentUserAnswer.getAccessToken();
    }

    protected static void logout() {
        sosShiroCurrentUserAnswer = (SOSAuthCurrentAccountAnswer) sosServicePermissionShiro.logout(sosShiroCurrentUserAnswer.getAccessToken())
                .getEntity();
    }

}
