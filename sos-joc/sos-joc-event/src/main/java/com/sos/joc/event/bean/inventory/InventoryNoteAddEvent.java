package com.sos.joc.event.bean.inventory;

public class InventoryNoteAddEvent extends InventoryNoteEvent {
    
    public InventoryNoteAddEvent(String path, String type) {
        super("InventoryNoteAdded", path, type);
    }
}
