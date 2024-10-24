package com.sos.joc.event.bean.inventory;

public class InventoryTagAddEvent extends InventoryTagEvent {
    
    public InventoryTagAddEvent(String tag) {
        super("InventoryTagAdded", tag);
    }
}
