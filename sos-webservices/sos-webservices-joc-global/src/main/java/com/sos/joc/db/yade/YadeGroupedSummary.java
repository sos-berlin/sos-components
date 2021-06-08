package com.sos.joc.db.yade;

import java.nio.file.Paths;

public class YadeGroupedSummary {

    private Long count = null;
    private String controllerId = null;
    private String folder = null;

    public YadeGroupedSummary(Long count, String controllerId, String workflowPath) {
        this.count = count;
        try {
            this.folder = Paths.get(workflowPath).getParent().toString().replace('\\', '/');
        } catch (Exception e) {
            this.folder = null;
        }
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
