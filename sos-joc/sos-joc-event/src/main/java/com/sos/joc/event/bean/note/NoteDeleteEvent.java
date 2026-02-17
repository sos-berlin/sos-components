package com.sos.joc.event.bean.note;

import java.util.Map;

public class NoteDeleteEvent extends NoteEvent {
    
    public NoteDeleteEvent(String path, String type, Map<String, Long> involvedAccounts, boolean withNotification, boolean onlyNotification) {
        super("InventoryNoteDeleted", path, type, involvedAccounts, withNotification, onlyNotification);
    }
}
