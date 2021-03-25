package com.sos.joc.db.yade;

public class YadeGroupedSummary {

    private Long count = null;
    private String folder = null;

    public YadeGroupedSummary(Long count, String workflowPath) {
        this.count = count;
        if (workflowPath != null) {
            this.folder = getFolder(workflowPath);
        }
    }

    public int getCount() {
        return count.intValue();
    }

    public String getFolder() {
        return folder;
    }

    private String getFolder(String path) {
        if (!path.startsWith("/")) {
            return "/";
        }
        int li = path.lastIndexOf("/");
        if (li == 0) {
            return path.substring(0, 1);
        }
        return li > -1 ? path.substring(0, li) : path;
    }
}
