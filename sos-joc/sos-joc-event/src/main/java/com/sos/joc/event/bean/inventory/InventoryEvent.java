package com.sos.joc.event.bean.inventory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.joc.event.bean.JOCEvent;

public class InventoryEvent extends JOCEvent {
    
    /**
     * No args constructor for use in serialization
     * 
     */
    public InventoryEvent() {
    }

    /**
     * @param folder
     */
    public InventoryEvent(String folder) {
        super("InventoryUpdated", null, null);
        putVariable("folder", folder);
    }
    
    public InventoryEvent(String key, String folder) {
        super(key, null, null);
        putVariable("folder", folder);
    }
    
    @JsonIgnore
    public String getFolder() {
        return (String) getVariables().get("folder");
    }
}
