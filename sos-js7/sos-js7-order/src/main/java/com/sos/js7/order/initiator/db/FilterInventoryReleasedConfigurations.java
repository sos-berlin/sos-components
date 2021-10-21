package com.sos.js7.order.initiator.db;

import java.util.List;

import com.sos.joc.db.DBFilter;
import com.sos.joc.model.inventory.common.ConfigurationType;

public class FilterInventoryReleasedConfigurations extends DBFilter {

    
    private ConfigurationType  type;
    private String name;
    private List<String> listOfControllerIds;
    private Long id;

    
    public ConfigurationType getType() {
        return type;
    }
    
    public void setType(ConfigurationType type) {
        this.type = type;
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

    
    public Long getId() {
        return id;
    }

    
    public void setId(Long id) {
        this.id = id;
    } 
}