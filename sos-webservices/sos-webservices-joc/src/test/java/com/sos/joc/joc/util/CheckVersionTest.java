package com.sos.joc.joc.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.joc.versions.util.CheckVersion;
import com.sos.joc.model.joc.CompatibilityLevel;

public class CheckVersionTest {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckVersionTest.class);
    
    @Test
    public void checkControllerVersionMatches() {
        String controllerVersion = "2.4.0";
        String jocVersion = "2.4.1-SNAPSHOT";
        assertEquals(CompatibilityLevel.COMPATIBLE, CheckVersion.checkControllerVersionMatches2Joc(controllerVersion, jocVersion));
    }
    
    @Test
    public void checkControllerVersionNotMatches() {
        String controllerVersion = "2.4.1-SNAPSHOT";
        String jocVersion = "2.4.0";
        assertEquals(CompatibilityLevel.NOT_COMPATIBLE, CheckVersion.checkControllerVersionMatches2Joc(controllerVersion, jocVersion));
    }
    
    @Test
    public void checkAgentVersionMatches() {
        String controllerVersion = "2.4.1-SNAPSHOT";
        String agentVersion = "2.4.0";
        assertEquals(CompatibilityLevel.COMPATIBLE, CheckVersion.checkAgentVersionMatches2Controller(agentVersion, controllerVersion));
    }
    
    @Test
    public void checkAgentVersionNotMatches() {
        String controllerVersion = "2.4.0";
        String agentVersion = "2.4.1-SNAPSHOT";
        assertEquals(CompatibilityLevel.NOT_COMPATIBLE, CheckVersion.checkAgentVersionMatches2Controller(agentVersion, controllerVersion));
    }
    
    
    @Test
    public void checkDiverseMatchesAndIncompatibilities() {
        
        String controller24Version = "2.4.0";
        String controller25Version = "2.5.0";
        String controller25CompatibleVersion = "2.5.6";
        String controller26Version = "2.6.0";
        String controller26CompatibleVersion = "2.6.3";
        String agent24Version = "2.4.1";
        String agent25Version = "2.5.1";
        String agent26Version = "2.6.1";
        String agent25CompatibleVersion = "2.5.6";
        String agent26CompatibleVersion = "2.6.3";
        String joc24Version = "2.4.2";
        String joc25Version = "2.5.6";
        String joc26Version = "2.6.3";
        assertEquals(CompatibilityLevel.COMPATIBLE, CheckVersion.checkControllerVersionMatches2Joc(controller24Version, joc24Version));
        assertEquals(CompatibilityLevel.NOT_COMPATIBLE, CheckVersion.checkControllerVersionMatches2Joc(controller24Version, joc25Version));
        assertEquals(CompatibilityLevel.NOT_COMPATIBLE, CheckVersion.checkControllerVersionMatches2Joc(controller24Version, joc26Version));
        assertEquals(CompatibilityLevel.NOT_COMPATIBLE, CheckVersion.checkAgentVersionMatches2Controller(agent24Version, controller24Version));
        assertEquals(CompatibilityLevel.NOT_COMPATIBLE, CheckVersion.checkControllerVersionMatches2Joc(controller25Version, joc24Version));
        assertEquals(CompatibilityLevel.NOT_COMPATIBLE, CheckVersion.checkControllerVersionMatches2Joc(controller25Version, joc25Version));
        assertEquals(CompatibilityLevel.NOT_COMPATIBLE, CheckVersion.checkControllerVersionMatches2Joc(controller25Version, joc26Version));
        assertEquals(CompatibilityLevel.COMPATIBLE, CheckVersion.checkAgentVersionMatches2Controller(agent25Version, controller25CompatibleVersion));
        assertEquals(CompatibilityLevel.COMPATIBLE, CheckVersion.checkControllerVersionMatches2Joc(controller25CompatibleVersion, joc25Version));
        assertEquals(CompatibilityLevel.COMPATIBLE, CheckVersion.checkAgentVersionMatches2Controller(agent25CompatibleVersion, controller25CompatibleVersion));
        assertEquals(CompatibilityLevel.NOT_COMPATIBLE, CheckVersion.checkControllerVersionMatches2Joc(controller26Version, joc24Version));
        assertEquals(CompatibilityLevel.NOT_COMPATIBLE, CheckVersion.checkControllerVersionMatches2Joc(controller26Version, joc25Version));
        assertEquals(CompatibilityLevel.NOT_COMPATIBLE, CheckVersion.checkControllerVersionMatches2Joc(controller26Version, joc26Version));
        assertEquals(CompatibilityLevel.COMPATIBLE, CheckVersion.checkAgentVersionMatches2Controller(agent26Version, controller26CompatibleVersion));
        assertEquals(CompatibilityLevel.COMPATIBLE, CheckVersion.checkControllerVersionMatches2Joc(controller26CompatibleVersion, joc26Version));
        assertEquals(CompatibilityLevel.COMPATIBLE, CheckVersion.checkAgentVersionMatches2Controller(agent26CompatibleVersion, controller26CompatibleVersion));
    }
    
    @Test
    public void checkIncompatibile() {
        
        String controllerVersion = "2.7.2";
//        String agentVersion = "2.7.2";
        String jocVersion = "2.7.3";
        assertEquals(CompatibilityLevel.NOT_COMPATIBLE, CheckVersion.checkControllerVersionMatches2Joc(controllerVersion, jocVersion));
    }
    
}
