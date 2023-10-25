package com.sos.joc.event.bean.inventory;

import com.sos.joc.event.bean.JOCEvent;

public class InventoryTagsEvent extends JOCEvent {
    
    /**
     * No args constructor for use in serialization
     * 
     */
    public InventoryTagsEvent() {
        super("InventoryTagsUpdated", null, null);
    }
}
