package com.sos.joc.db.inventory.items;

import com.sos.joc.db.inventory.DBItemInventoryNote;

public class InventoryNoteItem {
    
    private DBItemInventoryNote in;
    private String folder;
    
    public InventoryNoteItem(DBItemInventoryNote in, String folder) {
        this.in = in;
        this.folder = folder;
    }
    
    public DBItemInventoryNote getDBItemInventoryNote() {
        return in;
    }
    
    public String getFolder() {
        return folder;
    }
}
