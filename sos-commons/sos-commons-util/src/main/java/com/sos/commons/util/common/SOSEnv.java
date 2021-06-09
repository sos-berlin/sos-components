package com.sos.commons.util.common;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SOSEnv {

    private final Map<String, String> envVars;

    public SOSEnv(Map<String, String> envVars) {
        this.envVars = envVars;
    }

    public Map<String, String> getEnvVars() {
        return envVars;
    }
    
    public SOSEnv merge(SOSEnv toAdd) {
    	return merge(toAdd, false);
    }

    public SOSEnv merge(SOSEnv toAdd, boolean overwrite) {
    	if (overwrite) {
    		// same key: values will be taken from map of second SOSEnv 
    		return new SOSEnv(Stream.concat(envVars.entrySet().stream(), toAdd.getEnvVars().entrySet().stream())
    				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (envVarsEntry, toAddEntry) -> toAddEntry)));
    	} else {
    		// same key: values will be taken from map of first SOSEnv 
    		return new SOSEnv(Stream.concat(envVars.entrySet().stream(), toAdd.getEnvVars().entrySet().stream())
    				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (envVarsEntry, toAddEntry) -> envVarsEntry)));
    	}
    }

}
