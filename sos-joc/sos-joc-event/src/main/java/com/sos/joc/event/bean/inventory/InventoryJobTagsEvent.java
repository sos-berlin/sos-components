package com.sos.joc.event.bean.inventory;

import com.sos.joc.event.bean.JOCEvent;

public class InventoryJobTagsEvent extends JOCEvent {
    
    /**
     * No args constructor for use in serialization
     * 
     */
    public InventoryJobTagsEvent() {
        super("InventoryJobTagsUpdated", null, null);
    }
}
