package com.sos.joc.event.bean.inventory;

public class InventoryGroupAddEvent extends InventoryTagEvent {
    
    /**
     * @param folder
     */
    public InventoryGroupAddEvent(String group) {
        super("InventoryGroupAdded", group);
    }
}
