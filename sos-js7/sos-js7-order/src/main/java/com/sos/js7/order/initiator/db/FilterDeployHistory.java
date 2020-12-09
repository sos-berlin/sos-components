package com.sos.js7.order.initiator.db;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.db.SOSFilter;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.publish.DeploymentState;

public class FilterDeployHistory extends SOSFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilterDeployHistory.class);
    private ConfigurationType  type;
    private String path;
    private List<String> listOfControllerIds;
    private DeploymentState state;
    
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

    
    public List<String> getListOfControllerIds() {
        return listOfControllerIds;
    }

    
    public void setListOfControllerIds(List<String> listOfControllerIds) {
        this.listOfControllerIds = listOfControllerIds;
    }

    
    public DeploymentState getState() {
        return state;
    }

    
    public void setState(DeploymentState state) {
        this.state = state;
    }
    
}