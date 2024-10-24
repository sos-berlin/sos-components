package com.sos.joc.event.bean.inventory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.joc.event.bean.JOCEvent;

public class InventoryTagEvent extends JOCEvent {
    
    public InventoryTagEvent(String tag) {
        super("InventoryTaggingUpdated", null, null);
        putVariable("tag", tag);
    }
    
    public InventoryTagEvent(String key, String tag) {
        super(key, null, null);
        putVariable("tag", tag);
    }
    
    @JsonIgnore
    public String getTag() {
        return (String) getVariables().get("tag");
    }
}
