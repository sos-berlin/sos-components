package com.sos.js7.order.initiator.db;

import java.util.List;

import com.sos.joc.db.SOSFilter;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.publish.DeploymentState;

public class FilterDeployHistory extends SOSFilter {

    private ConfigurationType  type;
    private String name;
    private Long inventoryId;
    private Integer operation;
    private List<String> listOfControllerIds;
    private DeploymentState state;
    
    public ConfigurationType getType() {
        return type; 
    }
    
    public void setType(ConfigurationType type) {
        this.type = type;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
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

    
    public Long getInventoryId() {
        return inventoryId;
    }

    
    public void setInventoryId(Long inventoryId) {
        this.inventoryId = inventoryId;
    }

    
    public Integer getOperation() {
        return operation;
    }

    
    public void setOperation(Integer operation) {
        this.operation = operation;
    }

   
    
}