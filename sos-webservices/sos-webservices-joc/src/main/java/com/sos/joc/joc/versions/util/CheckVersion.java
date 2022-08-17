package com.sos.joc.joc.versions.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * - The general rule for compatibility between JOC Cockpit, Controller and Agent components includes that within a minor release such as 2.3
 *      1. a JOC Cockpit with a newer maintenance release number such as 2.3.2
 *          can be operated with a Controller of an earlier maintenance release number such as 2.3.0
 *      2. a Controller with a newer maintenance release number such as 2.3.2 
 *          can be operated with an Agent of an earlier maintenance release number such as 2.3.1
 *      2. between minor releases such as 2.3 and 2.4 the JS7 components have to be upgraded to a common minor release.
 *          There can be exceptions to this rule.
 *
 */
public class CheckVersion {
    
    private static final String VERSION_SPLITTER = "\\.";
    private static final String VERSION_SUFFIX_SPLITTER = "-";
    
    private static final Map<String, List<String>> jocCompatibleControllerExemptions = Collections.unmodifiableMap(new HashMap<String, List<String>>(){
        
        private static final long serialVersionUID = 1L;
        {
            // key : joc version
            // value : List of controller versions partially compatible with the given joc version
            put("2.4.1", Arrays.asList());
        }
    });
    private static final Map<String, List<String>> controllerCompatibleAgentExemptions = Collections.unmodifiableMap(new HashMap<String, List<String>>(){
        private static final long serialVersionUID = 1L;
        {
            // key : controller version
            // value : List of agent versions partially compatible with the given controller version
            put("2.4.1", Arrays.asList());
        }
    });
    
    public static CompatibiltyLevel checkControllerVersionMatches2Joc (String controllerVersion, String jocVersion) {
        List<Integer> controllerVersionSplitted = splitVersion(controllerVersion);
        List<Integer> jocVersionSplitted = splitVersion(jocVersion);
        if(jocCompatibleControllerExemptions.get(jocVersion) != null && !jocCompatibleControllerExemptions.get(jocVersion).isEmpty()) {
            if(jocCompatibleControllerExemptions.get(jocVersion).contains(controllerVersion)) {
                return CompatibiltyLevel.PARTIALLY_COMPATIBLE;
            } else {
                return CompatibiltyLevel.NOT_COMPATIBLE;
            }
        }
        if(controllerVersionSplitted.get(0) != jocVersionSplitted.get(0)) {
            return CompatibiltyLevel.NOT_COMPATIBLE;
        } else if(controllerVersionSplitted.get(1) != jocVersionSplitted.get(1)) {
            return CompatibiltyLevel.NOT_COMPATIBLE;
        } else if (controllerVersionSplitted.get(2) <= jocVersionSplitted.get(2)) {
            return CompatibiltyLevel.COMPATIBLE;
        } else {
            return CompatibiltyLevel.NOT_COMPATIBLE;
        }
    }
    
    public static CompatibiltyLevel checkAgentVersionMatches2Controller(String agentVersion, String controllerVersion) {
        List<Integer> agentVersionSplitted = splitVersion(agentVersion);
        List<Integer> controllerVersionSplitted = splitVersion(controllerVersion);
        if(controllerCompatibleAgentExemptions.get(controllerVersion) != null && !controllerCompatibleAgentExemptions.get(controllerVersion).isEmpty()) {
            if(controllerCompatibleAgentExemptions.get(controllerVersion).contains(agentVersion)) {
                return CompatibiltyLevel.PARTIALLY_COMPATIBLE;
            } else {
                return CompatibiltyLevel.NOT_COMPATIBLE;
            }
        }
        if(agentVersionSplitted.get(0) != controllerVersionSplitted.get(0)) {
            return CompatibiltyLevel.NOT_COMPATIBLE;
        } else if (agentVersionSplitted.get(1) != controllerVersionSplitted.get(1)) {
            return CompatibiltyLevel.NOT_COMPATIBLE;
        } else if (agentVersionSplitted.get(2) <= controllerVersionSplitted.get(2)) {
            return CompatibiltyLevel.COMPATIBLE;
        } else {
            return CompatibiltyLevel.NOT_COMPATIBLE;
        }
    }
    
    private static List<Integer> splitVersion (String version) {
        String[] splitted = version.split(VERSION_SPLITTER);
        if (splitted[2].contains(VERSION_SUFFIX_SPLITTER)) {
            splitted [2] = splitted[2].split(VERSION_SUFFIX_SPLITTER)[0];
        }
        return Arrays.asList(Integer.parseInt(splitted[0]), Integer.parseInt(splitted[1]), Integer.parseInt(splitted[2]));
    }
}
