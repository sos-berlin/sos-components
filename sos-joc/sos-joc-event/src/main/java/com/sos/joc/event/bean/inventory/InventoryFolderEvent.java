package com.sos.joc.event.bean.inventory;

public class InventoryFolderEvent extends InventoryEvent {
    
    /**
     * No args constructor for use in serialization
     * 
     */
    public InventoryFolderEvent() {
    }

    /**
     * @param folder
     */
    public InventoryFolderEvent(String folder) {
        super("InventoryTreeUpdated", folder);
    }
}
