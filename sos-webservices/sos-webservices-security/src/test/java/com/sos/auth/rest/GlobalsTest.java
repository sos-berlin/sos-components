package com.sos.auth.rest;

public class GlobalsTest {

    private static final String PASSWORD = "root";
    private static final String USER = "root";
    protected static final String SHIRO_ROLE = "all";
    protected static SOSServicePermissionShiro sosServicePermissionShiro;
    protected static SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer;

    protected static String getAccessToken() throws Exception {
        sosServicePermissionShiro = new SOSServicePermissionShiro();
        sosShiroCurrentUserAnswer = (SOSShiroCurrentUserAnswer) sosServicePermissionShiro.login(null, "","", USER, PASSWORD).getEntity();
        return sosShiroCurrentUserAnswer.getAccessToken();
    }

    protected static void logout() {
        sosShiroCurrentUserAnswer = (SOSShiroCurrentUserAnswer) sosServicePermissionShiro.logout(sosShiroCurrentUserAnswer.getAccessToken())
                .getEntity();
    }

}
