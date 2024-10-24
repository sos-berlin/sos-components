package com.sos.joc.event.bean.inventory;

public class InventoryTagDeleteEvent extends InventoryTagEvent {
    
    public InventoryTagDeleteEvent(String tag) {
        super("InventoryTagDeleted", tag);
    }
}
