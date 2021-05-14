package com.sos.commons.util.common;

import java.util.Map;

public class SOSEnv {

    private final Map<String, String> envVars;

    public SOSEnv(Map<String, String> envVars) {
        this.envVars = envVars;
    }

    public Map<String, String> getEnvVars() {
        return envVars;
    }
}
