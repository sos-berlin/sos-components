package com.sos.jobscheduler.event.master.fatevent.bean;

import com.sos.commons.util.SOSString;

public class WorkflowId {

    private String path;
    private String versionId;

    public String getPath() {
        return path;
    }

    public void setPath(String val) {
        path = val;
    }

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String val) {
        versionId = val;
    }

    @Override
    public String toString() {
        return SOSString.toString(this);
    }
}
