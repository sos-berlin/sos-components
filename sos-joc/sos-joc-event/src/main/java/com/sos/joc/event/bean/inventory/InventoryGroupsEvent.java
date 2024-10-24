package com.sos.joc.event.bean.inventory;

import com.sos.joc.event.bean.JOCEvent;

public class InventoryGroupsEvent extends JOCEvent {
    
    /**
     * No args constructor for use in serialization
     * 
     */
    public InventoryGroupsEvent() {
        super("InventoryGroupsUpdated", null, null);
    }
}
