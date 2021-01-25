package com.sos.auth.rest;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.sos.joc.exceptions.SessionNotExistException;

public class TestSOSServicePermissionShiro {

    private static final String SHIRO_MAPPED_ROLE = "application_manager";

    private String accessToken;

    @Before
    public void setUp() throws Exception {
        accessToken = GlobalsTest.getAccessToken();
    }

    @Test
    public void testIsAuthenticated() {
        GlobalsTest.logout();
        assertEquals("testCurrentUserAnswer is authenticated", true, GlobalsTest.sosShiroCurrentUserAnswer.getIsAuthenticated());
    }

    // Test fails in nightly build
    @Test
    @Ignore
    public void testHasRole() throws SessionNotExistException {
        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = GlobalsTest.sosServicePermissionShiro.hasRole("", "", accessToken, "", "",
                SHIRO_MAPPED_ROLE);
        assertEquals("testCurrentUserAnswer is authenticated", true, sosShiroCurrentUserAnswer.getIsAuthenticated());
        assertEquals("testCurrentUserAnswer is has role " + SHIRO_MAPPED_ROLE, true, sosShiroCurrentUserAnswer.hasRole());
    }

    @Test
    public void testIsPermitted() throws SessionNotExistException {
        SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer = GlobalsTest.sosServicePermissionShiro.isPermitted("", "", accessToken, "", "",
                "sos:products:joc_cockpit:js7_controller:pause");
        // assertEquals("testCurrentUserAnswer is permitted " + SHIRO_PERMISSION, true, sosShiroCurrentUserAnswer.isPermitted());
        assertEquals("testCurrentUserAnswer is authenticated", true, sosShiroCurrentUserAnswer.getIsAuthenticated());
        sosShiroCurrentUserAnswer = GlobalsTest.sosServicePermissionShiro.isPermitted("", "", accessToken, "", "",
                "sos:products:joc_cockpit:jobscheduler_master:pause");
        assertEquals("testCurrentUserAnswer is permitted  sos:products:joc_cockpit:js7_controller:pause", true, sosShiroCurrentUserAnswer
                .isPermitted());
        sosShiroCurrentUserAnswer = GlobalsTest.sosServicePermissionShiro.isPermitted("", "", accessToken, "", "",
                "sos:products:joc_cockpit:js7_controller:restart");
        assertEquals("testCurrentUserAnswer is permitted  sos:products:joc_cockpit:js7_controller:restart", true, sosShiroCurrentUserAnswer
                .isPermitted());
        sosShiroCurrentUserAnswer = GlobalsTest.sosServicePermissionShiro.isPermitted("", "", accessToken, "", "",
                "sos:products:joc_cockpit:js7_controller:continue");
        assertEquals("testCurrentUserAnswer is permitted  sos:products:joc_cockpit:js7_controller:continue", true, sosShiroCurrentUserAnswer
                .isPermitted());
        sosShiroCurrentUserAnswer = GlobalsTest.sosServicePermissionShiro.isPermitted("", "", accessToken, "", "",
                "sos:products:joc_cockpit:job_chain:view:status");
        assertEquals("testCurrentUserAnswer is permitted  sos:products:joc_cockpit:workflow:view:status", true, sosShiroCurrentUserAnswer
                .isPermitted());
        sosShiroCurrentUserAnswer = GlobalsTest.sosServicePermissionShiro.isPermitted("", "", accessToken, "", "",
                "sos:products:joc_cockpit:job_chain:view:history");
        assertEquals("testCurrentUserAnswer is permitted  sos:products:joc_cockpit:workflow:view:history", true, sosShiroCurrentUserAnswer
                .isPermitted());
    }

}