package com.sos.joc.event.bean.inventory;

public class InventoryTrashEvent extends InventoryEvent {
    
    /**
     * No args constructor for use in serialization
     * 
     */
    public InventoryTrashEvent() {
    }

    /**
     * @param folder
     */
    public InventoryTrashEvent(String folder) {
        super("InventoryTrashUpdated", folder);
    }
}
