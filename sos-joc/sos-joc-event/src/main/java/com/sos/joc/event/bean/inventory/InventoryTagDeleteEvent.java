package com.sos.joc.event.bean.inventory;

public class InventoryTagDeleteEvent extends InventoryTagEvent {
    
    /**
     * No args constructor for use in serialization
     * 
     */
    public InventoryTagDeleteEvent() {
    }

    /**
     * @param folder
     */
    public InventoryTagDeleteEvent(String tag) {
        super("InventoryTagDeleted", tag);
    }
}
