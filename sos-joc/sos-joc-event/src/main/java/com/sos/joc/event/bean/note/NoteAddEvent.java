package com.sos.joc.event.bean.note;

import java.util.Map;

public class NoteAddEvent extends NoteEvent {
    
    public NoteAddEvent(String path, String type, Map<String, Long> involvedAccounts, boolean withNotification, boolean onlyNotification) {
        super("InventoryNoteAdded", path, type, involvedAccounts, withNotification, onlyNotification);
    }
}
