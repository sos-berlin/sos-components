package com.sos.joc.event.bean.yade;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class YADEConfigurationDeployed extends YADEEvent {

    public YADEConfigurationDeployed(String controllerId, String jobResource) {
        super(YADEConfigurationDeployed.class.getSimpleName(), controllerId, null);
        putVariable("jobResource", jobResource);
    }

    @JsonIgnore
    public String getJobResource() {
        try {
            return (String) getVariables().get("jobResource");
        } catch (Throwable e) {
            return null;
        }
    }
}
