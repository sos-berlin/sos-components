package com.sos.joc.event.bean.inventory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.joc.event.bean.JOCEvent;

public class InventoryNoteEvent extends JOCEvent {
    
    public InventoryNoteEvent(String path, String type) {
        super("InventoryNoteUpdated", null, null);
        putVariable("path", path);
        putVariable("type", type);
    }
    
    public InventoryNoteEvent(String key, String path, String type) {
        super(key, null, null);
        putVariable("path", path);
        putVariable("type", type);
    }
    
    @JsonIgnore
    public String getPath() {
        return (String) getVariables().get("path");
    }
    
    @JsonIgnore
    public String getObjectType() {
        return (String) getVariables().get("type");
    }
}
