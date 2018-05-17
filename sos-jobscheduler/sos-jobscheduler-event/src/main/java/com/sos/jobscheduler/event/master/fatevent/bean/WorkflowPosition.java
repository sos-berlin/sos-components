package com.sos.jobscheduler.event.master.fatevent.bean;

import com.google.common.base.Joiner;
import com.sos.commons.util.SOSString;

public class WorkflowPosition {

    private WorkflowId workflowId;
    private String[] position;

    public WorkflowId getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(WorkflowId val) {
        workflowId = val;
    }

    public String[] getPosition() {
        return position;
    }

    public void setPosition(String[] val) {
        position = val;
    }

    public String getPositionAsString() {
        return Joiner.on('_').join(position);
    }

    @Override
    public String toString() {
        return SOSString.toString(this);
    }
}
