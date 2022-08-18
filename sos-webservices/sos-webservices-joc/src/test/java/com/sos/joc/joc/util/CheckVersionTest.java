package com.sos.joc.joc.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.sos.joc.joc.versions.util.CheckVersion;
import com.sos.joc.model.joc.CompatibilityLevel;

public class CheckVersionTest {

    
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
    
    
}
