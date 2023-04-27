package com.sos.joc.db.inventory.items;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.search.ResponseBaseSearchItem;

public class InventoryQuickSearchItem extends ResponseBaseSearchItem {

    @JsonIgnore
    private Integer type;
    @JsonIgnore
    private String folder;

    @JsonIgnore
    public Integer getType() {
        return type;
    }

    public void setType(Integer val) {
        type = val;
        try {
            setObjectType(ConfigurationType.fromValue(val));
        } catch (Exception e) {
            setObjectType(null);
        }
    }
    
    @JsonIgnore
    public String getFolder() {
        return folder;
    }

    public void setFolder(String val) {
        folder = val;
    }
}
