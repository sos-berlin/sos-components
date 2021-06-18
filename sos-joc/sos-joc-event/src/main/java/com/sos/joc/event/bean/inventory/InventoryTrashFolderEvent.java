package com.sos.joc.event.bean.inventory;

public class InventoryTrashFolderEvent extends InventoryEvent {
    
    /**
     * No args constructor for use in serialization
     * 
     */
    public InventoryTrashFolderEvent() {
    }

    /**
     * @param folder
     */
    public InventoryTrashFolderEvent(String folder) {
        super("InventoryTrashFolderUpdated", folder);
    }
}
