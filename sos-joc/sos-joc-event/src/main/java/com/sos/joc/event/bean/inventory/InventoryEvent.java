package com.sos.joc.event.bean.inventory;

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
    
    public String getFolder() {
        return getVariables().get("folder");
    }
}
