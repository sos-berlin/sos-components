package com.sos.joc.event.bean.inventory;

public class InventoryJobTagEvent extends InventoryTagEvent {
    
    public InventoryJobTagEvent(String tag) {
        super("InventoryJobTaggingUpdated", tag);
    }
}
