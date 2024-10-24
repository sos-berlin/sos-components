package com.sos.joc.event.bean.inventory;

public class InventoryGroupDeleteEvent extends InventoryTagEvent {
    
    public InventoryGroupDeleteEvent(String group) {
        super("InventoryGroupDeleted", group);
    }
}
