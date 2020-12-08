package com.sos.joc.db.deploy.items;

import com.sos.joc.model.inventory.common.ConfigurationType;

public class NumOfDeployment {

    private Long numOf;
    private ConfigurationType type;
    
    public NumOfDeployment(Integer type, Long numOf) {
        try {
            this.type = ConfigurationType.fromValue(type);
        } catch (Exception e) {
            this.type = null;
        }
        this.numOf = numOf;
    }

    public ConfigurationType getConfigurationType() {
        return type;
    }

    public Long getNumOf() {
        return numOf;
    }
}
