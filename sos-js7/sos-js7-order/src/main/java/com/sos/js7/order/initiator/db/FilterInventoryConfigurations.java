package com.sos.js7.order.initiator.db;

import java.util.List;

import com.sos.joc.db.SOSFilter;
import com.sos.joc.model.inventory.common.ConfigurationType;

public class FilterInventoryConfigurations extends SOSFilter {

    
    private ConfigurationType  type;
    private String name;
    private List<String> listOfControllerIds;
    private Boolean deployed;
    private Boolean released;
    private Boolean isActive;
    
    public ConfigurationType getType() {
        return type;
    }
    
    public void setType(ConfigurationType type) {
        this.type = type;
    }
     
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    
    public Boolean getDeployed() {
        return deployed;
    }

    
    public void setDeployed(Boolean deployed) {
        this.deployed = deployed;
    }

    
    public Boolean getReleased() {
        return released;
    }

    
    public void setReleased(Boolean released) {
        this.released = released;
    }

    
    public List<String> getListOfControllerIds() {
        return listOfControllerIds;
    }

    
    public void setListOfControllerIds(List<String> listOfControllerIds) {
        this.listOfControllerIds = listOfControllerIds;
    }

    
    public String getName() {
        return name;
    }

    
    public void setName(String name) {
        this.name = name;
    } 
}