package com.sos.jobscheduler.event.master.fatevent;

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

}
