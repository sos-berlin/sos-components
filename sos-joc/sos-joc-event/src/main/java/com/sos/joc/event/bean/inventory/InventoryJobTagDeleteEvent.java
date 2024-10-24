package com.sos.joc.event.bean.inventory;

public class InventoryJobTagDeleteEvent extends InventoryTagEvent {
    
    public InventoryJobTagDeleteEvent(String tag) {
        super("InventoryJobTagDeleted", tag);
    }
}
