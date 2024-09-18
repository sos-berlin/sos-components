package com.sos.joc.event.bean.inventory;

public class InventoryTagDeleteEvent extends InventoryTagEvent {
    
    /**
     * @param folder
     */
    public InventoryTagDeleteEvent(String tag) {
        super("InventoryTagDeleted", tag);
    }
}
