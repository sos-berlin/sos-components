package com.sos.joc.joc.versions.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.sos.joc.model.joc.CompatibilityLevel;

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
            put("2.5.0", Arrays.asList());
            put("2.6.0", Arrays.asList());
        }
    });
    private static final Map<String, List<String>> controllerCompatibleAgentExemptions = Collections.unmodifiableMap(new HashMap<String, List<String>>(){
        private static final long serialVersionUID = 1L;
        {
            // key : controller version
            // value : List of agent versions partially compatible with the given controller version
            put("2.4.1", Arrays.asList());
            put("2.5.0", Arrays.asList("2.5.1"));
            put("2.6.0", Arrays.asList());
        }
    });
    
    private static final Set<String> versionBackwardIncompatibilties = Collections.unmodifiableSet(new HashSet<String>() {

        private static final long serialVersionUID = 1L;
        {
            add("2.5.6");
            add("2.6.3");
        }
    });
    
    public static CompatibilityLevel checkControllerVersionMatches2Joc (String controllerVersion, String jocVersion) {
        String coreControllerVersion = extractCoreVersion(controllerVersion);
        String coreJocVersion = extractCoreVersion(jocVersion);
        List<Integer> controllerVersionSplitted = splitVersion(coreControllerVersion);
        List<Integer> jocVersionSplitted = splitVersion(coreJocVersion);
        if(jocCompatibleControllerExemptions.get(coreJocVersion) != null && !jocCompatibleControllerExemptions.get(coreJocVersion).isEmpty()) {
            if(jocCompatibleControllerExemptions.get(coreJocVersion).contains(coreControllerVersion)) {
                return CompatibilityLevel.PARTIALLY_COMPATIBLE;
            }
        }
        if(controllerVersionSplitted.get(0) != jocVersionSplitted.get(0)) {
            return CompatibilityLevel.NOT_COMPATIBLE;
        } else if(controllerVersionSplitted.get(1) != jocVersionSplitted.get(1)) {
            return CompatibilityLevel.NOT_COMPATIBLE;
        } else if (controllerVersionSplitted.get(2) <= jocVersionSplitted.get(2) && !isIncompatible(controllerVersion, null, jocVersion)) {
            return CompatibilityLevel.COMPATIBLE;
        } else {
            return CompatibilityLevel.NOT_COMPATIBLE;
        }
    }
    
    public static CompatibilityLevel checkAgentVersionMatches2Controller(String agentVersion, String controllerVersion) {
        String coreAgentVersion = extractCoreVersion(agentVersion);
        String coreControllerVersion = extractCoreVersion(controllerVersion);
        List<Integer> agentVersionSplitted = splitVersion(coreAgentVersion);
        List<Integer> controllerVersionSplitted = splitVersion(coreControllerVersion);
        if(controllerCompatibleAgentExemptions.get(coreControllerVersion) != null && !controllerCompatibleAgentExemptions.get(coreControllerVersion).isEmpty()) {
            if(controllerCompatibleAgentExemptions.get(coreControllerVersion).contains(coreAgentVersion)) {
                return CompatibilityLevel.PARTIALLY_COMPATIBLE;
            }
        }
        if(agentVersionSplitted.get(0) != controllerVersionSplitted.get(0)) {
            return CompatibilityLevel.NOT_COMPATIBLE;
        } else if (agentVersionSplitted.get(1) != controllerVersionSplitted.get(1)) {
            return CompatibilityLevel.NOT_COMPATIBLE;
        } else if (agentVersionSplitted.get(2) <= controllerVersionSplitted.get(2) && !isIncompatible(controllerVersion, agentVersion, null)) {
            return CompatibilityLevel.COMPATIBLE;
        } else {
            return CompatibilityLevel.NOT_COMPATIBLE;
        }
    }
    
    private static List<Integer> splitVersion (String version) {
        String[] splitted = version.split(VERSION_SPLITTER);
        if (splitted[2].contains(VERSION_SUFFIX_SPLITTER)) {
            splitted [2] = splitted[2].split(VERSION_SUFFIX_SPLITTER)[0];
        }
        return Arrays.asList(Integer.parseInt(splitted[0]), Integer.parseInt(splitted[1]), Integer.parseInt(splitted[2]));
    }
    
    private static String extractCoreVersion (String version) {
        return version.contains("-") ? version.split("-")[0] : version;
    }
    
    private static boolean isIncompatible(String controllerVersion, String agentVersion, String jocVersion) {
        String coreControllerVersion = extractCoreVersion(controllerVersion);
        if(jocVersion != null) {
            String coreJocVersion = extractCoreVersion(jocVersion);
            String lowestIncompatibleVersion = getLowestIncompatibilityVersionMatch(coreControllerVersion);
            if(!lowestIncompatibleVersion.isEmpty() 
                    && !isLower(coreJocVersion, lowestIncompatibleVersion)
                    && isLower(coreControllerVersion, coreJocVersion) 
                    && ( isEqual(coreJocVersion, lowestIncompatibleVersion) || isHigher(coreJocVersion, lowestIncompatibleVersion)) 
                    && isLower(controllerVersion, lowestIncompatibleVersion)) {
                return true;
            }
        } else if (agentVersion != null) {
            String coreAgentVersion = extractCoreVersion(agentVersion);
            String lowestIncompatibleVersion = getLowestIncompatibilityVersionMatch(coreAgentVersion);
            if(!lowestIncompatibleVersion.isEmpty() 
                    && ! isLower(coreAgentVersion, lowestIncompatibleVersion)
                    && isLower(coreAgentVersion, coreControllerVersion)
                    && (isEqual(controllerVersion, lowestIncompatibleVersion) || isHigher(controllerVersion, lowestIncompatibleVersion))
                    && isLower(agentVersion, lowestIncompatibleVersion)) {
                return true;
            }
        }
        return false;
    }
    
    private static String getLowestIncompatibilityVersionMatch(String currentVersion) {
        Comparator<String> comp = new Comparator<String>() {
            @Override
            public int compare(String arg0, String arg1) {
                if(isLower(arg0, arg1)) {
                    return +1;
                } else if (isHigher(arg0, arg1)) {
                    return -1;
                } else {
                    return 0;
                }
            }
        };
        String lowestIncompatibilty = "";
        for (String incomp : versionBackwardIncompatibilties) {
            if (isLower(currentVersion, incomp) && (lowestIncompatibilty.isEmpty() || !isLower(lowestIncompatibilty, incomp))) {
                lowestIncompatibilty = incomp;
            }
        }
        return lowestIncompatibilty;
    }
    
    /**
     * @param version1
     * @param version2
     * 
     * @return boolean; if version1 is considered lower as version2
     */
    private static boolean isLower(String version1, String version2) {
        List<Integer> version1Splitted = splitVersion(version1);
        List<Integer> version2Splitted = splitVersion(version2);
        if(version1Splitted.get(0) < version2Splitted.get(0)) {
            return true;
        } else if (version1Splitted.get(0) > version2Splitted.get(0)) {
            return false;
        } else if (version1Splitted.get(1) < version2Splitted.get(1)) {
            return true;
        } else if (version1Splitted.get(1) > version2Splitted.get(1)) {
            return false;
        } else if (version1Splitted.get(2) < version2Splitted.get(2)) {
            return true;
        } else if (version1Splitted.get(2) > version2Splitted.get(2)) {
            return false;
        }
        return false;
    }
    /**
     * @param version1
     * @param version2
     * 
     * @return boolean; if version1 is considered higher as version2
     */
    private static boolean isHigher(String version1, String version2) {
        List<Integer> version1Splitted = splitVersion(version1);
        List<Integer> version2Splitted = splitVersion(version2);
        if(version1Splitted.get(0) > version2Splitted.get(0)) {
            return true;
        } else if (version1Splitted.get(0) < version2Splitted.get(0)) {
            return false;
        } else if (version1Splitted.get(1) > version2Splitted.get(1)) {
            return true;
        } else if (version1Splitted.get(1) < version2Splitted.get(1)) {
            return false;
        } else if (version1Splitted.get(2) > version2Splitted.get(2)) {
            return true;
        } else if (version1Splitted.get(2) < version2Splitted.get(2)) {
            return false;
        }
        return false;
    }
    
    /**
     * @param version1
     * @param version2
     * 
     * @return boolean; if version1 is considered equal as version2
     */
    private static boolean isEqual(String version1, String version2) {
        List<Integer> version1Splitted = splitVersion(version1);
        List<Integer> version2Splitted = splitVersion(version2);
        if(version1Splitted.get(0) == version2Splitted.get(0) 
                && version1Splitted.get(1) == version2Splitted.get(1)
                && version1Splitted.get(2) == version2Splitted.get(2)) {
            return true;
        } else {
            return false;
        }
    }
    
}
