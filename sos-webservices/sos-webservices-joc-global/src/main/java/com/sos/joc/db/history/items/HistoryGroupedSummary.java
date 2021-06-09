package com.sos.joc.db.history.items;

public class HistoryGroupedSummary {

    private Long count = null;
    private String controllerId = null;
    private String folder = null;

    public HistoryGroupedSummary(Long count, String controllerId, String folder) {
        this.count = count;
        this.folder = folder;
        this.controllerId = controllerId;
    }

    public int getCount() {
        return count.intValue();
    }
    
    public String getControllerId() {
        return controllerId;
    }

    public String getFolder() {
        return folder;
    }
}
