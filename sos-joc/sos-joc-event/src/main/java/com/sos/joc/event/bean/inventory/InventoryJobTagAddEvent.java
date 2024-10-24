package com.sos.joc.event.bean.inventory;

public class InventoryJobTagAddEvent extends InventoryTagEvent {
    
    public InventoryJobTagAddEvent(String tag) {
        super("InventoryJobTagAdded", tag);
    }
}
