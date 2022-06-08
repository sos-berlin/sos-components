package com.sos.auth.classes;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.sos.joc.exceptions.SessionNotExistException;

public class TestSOSServicePermissionAuth {

    private static String accessToken;

    @BeforeClass
    public static void setUp() throws Exception {
        accessToken = GlobalsTest.getAccessToken();
    }

    @Ignore
    @Test
    public void testHasRole() throws SessionNotExistException {
        SOSAuthCurrentAccountAnswer sosAuthCurrentAccountAnswer = GlobalsTest.sosServicePermissionIam.hasRoleTest(accessToken, GlobalsTest.AUTH_ROLE);
        assertEquals("testCurrentUserAnswer is authenticated", true, sosAuthCurrentAccountAnswer.getIsAuthenticated());
        assertEquals("testCurrentUserAnswer is has role " + GlobalsTest.AUTH_ROLE, true, sosAuthCurrentAccountAnswer.hasRole());
    }

    @Ignore
    @Test
    public void testIsPermitted() throws SessionNotExistException {
        Arrays.asList("sos:products:joc:inventory:view", "sos:products:joc:auditlog:view", "sos:products:controller:deployment:deploy",
                "sos:products:controller:orders:create", "sos:products:controller:orders:suspend_resume", "sos:products:controller:orders:cancel")
                .forEach(permission -> {
                    assertEquals("testCurrentUserAnswer is permitted -> " + permission, true, isPermitted(permission));
                });
    }

    private static boolean isPermitted(String permission) {
        return GlobalsTest.sosServicePermissionIam.isPermittedTest(accessToken, permission).isPermitted();
    }

}