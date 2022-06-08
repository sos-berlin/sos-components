package com.sos.auth.classes;

import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;

import org.junit.Test;

public class TestSOSAuthCurrentUserAnswer {

    private SOSAuthCurrentAccountAnswer sosAuthCurrentAccountAnswer;

    @Test
    public void testSOSAuthCurrentUserAnswer() throws MalformedURLException {
        sosAuthCurrentAccountAnswer = new SOSAuthCurrentAccountAnswer();
        sosAuthCurrentAccountAnswer.setHasRole(true);
        sosAuthCurrentAccountAnswer.setIsAuthenticated(true);
        sosAuthCurrentAccountAnswer.setIsPermitted(true);
        sosAuthCurrentAccountAnswer.setPermission("permission");
        sosAuthCurrentAccountAnswer.setRole("role");
        sosAuthCurrentAccountAnswer.setAccount("user");
        assertEquals("testSOSAuthCurrentUserAnswer getHasRole", true, sosAuthCurrentAccountAnswer.getHasRole());
        assertEquals("testSOSAuthCurrentUserAnswer hasRole", true, sosAuthCurrentAccountAnswer.hasRole());
        assertEquals("testSOSAuthCurrentUserAnswer getIsPermitted", true, sosAuthCurrentAccountAnswer.getIsPermitted());
        assertEquals("testSOSAuthCurrentUserAnswer isPermitted", true, sosAuthCurrentAccountAnswer.isPermitted());
        assertEquals("testSOSAuthCurrentUserAnswer getIsAuthenticated", true, sosAuthCurrentAccountAnswer.getIsAuthenticated());
        assertEquals("testSOSAuthCurrentUserAnswer isAuthenticated", true, sosAuthCurrentAccountAnswer.isAuthenticated());
        assertEquals("testSOSAuthCurrentUserAnswer getRole", "role", sosAuthCurrentAccountAnswer.getRole());
        assertEquals("testSOSAuthCurrentUserAnswer getUser", "user", sosAuthCurrentAccountAnswer.getAccount());
        assertEquals("testSOSAuthCurrentUserAnswer getPermission", "permission", sosAuthCurrentAccountAnswer.getPermission());
    }

}