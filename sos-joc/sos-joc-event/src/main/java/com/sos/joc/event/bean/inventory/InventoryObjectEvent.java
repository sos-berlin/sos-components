package com.sos.joc.event.bean.inventory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.joc.event.bean.JOCEvent;

public class InventoryObjectEvent extends JOCEvent {
    
    /**
     * No args constructor for use in serialization
     * 
     */
    public InventoryObjectEvent() {
    }

    /**
     * 
     * @param path
     * @param objectType
     */
    public InventoryObjectEvent(String path, String objectType) {
        super("InventoryObjectUpdated", null, null);
        putVariable("path", path);
        putVariable("objectType", objectType);
    }
    
    public InventoryObjectEvent(String key, String path, String objectType) {
        super(key, null, null);
        putVariable("path", path);
        putVariable("objectType", objectType);
    }
    
    @JsonIgnore
    public String getPath() {
        return (String) getVariables().get("path");
    }
    
    @JsonIgnore
    public String getObjectType() {
        return (String) getVariables().get("objectType");
    }
}
