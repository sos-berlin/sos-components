package com.sos.joc.event.bean.inventory;

public class InventoryTagAddEvent extends InventoryTagEvent {
    
    /**
     * No args constructor for use in serialization
     * 
     */
    public InventoryTagAddEvent() {
    }

    /**
     * @param folder
     */
    public InventoryTagAddEvent(String tag) {
        super("InventoryTagAdded", tag);
    }
}
