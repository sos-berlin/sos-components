package com.sos.commons.util.common;

import java.util.Map;

public class SOSEnv {

    private Map<String, String> localEnvs;
    private Map<String, String> globalEnvs;

    public SOSEnv() {

    }

    public SOSEnv(Map<String, String> localEnvs) {
        this(localEnvs, null);
    }

    public SOSEnv(Map<String, String> localEnvs, Map<String, String> globalEnvs) {
        this.localEnvs = localEnvs;
        this.globalEnvs = globalEnvs;
    }

    public Map<String, String> getLocalEnvs() {
        return localEnvs;
    }

    public void setLocalEnvs(Map<String, String> val) {
        localEnvs = val;
    }

    public Map<String, String> getGlobalEnvs() {
        return globalEnvs;
    }

    public void setGlobalEnvs(Map<String, String> val) {
        globalEnvs = val;
    }

}
