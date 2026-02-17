package com.sos.joc.event.bean.note;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.joc.event.bean.JOCEvent;

public class NoteEvent extends JOCEvent {
    
    public NoteEvent(String path, String type, Map<String, Long> involvedAccounts, boolean withNotification, boolean onlyNotification) {
        super("InventoryNoteUpdated", null, null);
        putVariable("path", path);
        putVariable("type", type);
        putVariable("involvedAccounts", involvedAccounts);
        putVariable("withNotification", withNotification);
        putVariable("onlyNotification", onlyNotification);
    }

    public NoteEvent(String key, String path, String type, Map<String, Long> involvedAccounts, boolean withNotification, boolean onlyNotification) {
        super(key, null, null);
        putVariable("path", path);
        putVariable("type", type);
        putVariable("involvedAccounts", involvedAccounts);
        putVariable("withNotification", withNotification);
        putVariable("onlyNotification", onlyNotification);
    }
    
    @JsonIgnore
    public String getPath() {
        return (String) getVariables().get("path");
    }
    
    @JsonIgnore
    public String getObjectType() {
        return (String) getVariables().get("type");
    }
    
    @SuppressWarnings("unchecked")
    @JsonIgnore
    public Map<String, Long> getInvolvedAccounts() {
        return (Map<String, Long>) getVariables().get("involvedAccounts");
    }
    
    @JsonIgnore
    public Boolean withNotification() {
        return (Boolean) getVariables().get("withNotification");
    }
    
    @JsonIgnore
    public Boolean onlyNotification() {
        return (Boolean) getVariables().get("onlyNotification");
    }
}
