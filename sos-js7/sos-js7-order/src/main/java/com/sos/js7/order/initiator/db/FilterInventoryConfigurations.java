package com.sos.js7.order.initiator.db;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.db.SOSFilter;
import com.sos.joc.model.inventory.common.ConfigurationType;

public class FilterInventoryConfigurations extends SOSFilter {

    
    private ConfigurationType  type;
    private String path;
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
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
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
     
 
}