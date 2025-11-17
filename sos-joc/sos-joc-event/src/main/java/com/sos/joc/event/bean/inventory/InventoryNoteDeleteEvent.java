package com.sos.joc.event.bean.inventory;

public class InventoryNoteDeleteEvent extends InventoryNoteEvent {
    
    public InventoryNoteDeleteEvent(String path, String type) {
        super("InventoryNoteDeleted", path, type);
    }
}
