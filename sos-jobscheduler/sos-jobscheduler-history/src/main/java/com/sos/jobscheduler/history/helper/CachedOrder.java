package com.sos.jobscheduler.history.helper;

import java.util.Date;

import com.sos.jobscheduler.db.history.DBItemOrder;

public class CachedOrder {

    private final Long id;
    private final String orderKey;
    private final Long mainParentId;
    private final String startCause;
    private final String startWorkflowPosition;
    private final String workflowPosition;
    private final Date endTime;

    private Long currentStepId;
    private boolean hasChildren;

    public CachedOrder(final DBItemOrder item) {
        id = item.getId();
        currentStepId = item.getCurrentStepId();
        orderKey = item.getOrderKey();
        mainParentId = item.getMainParentId();
        startCause = item.getStartCause();
        startWorkflowPosition = item.getStartWorkflowPosition();
        workflowPosition = item.getWorkflowPosition();
        hasChildren = item.getHasChildren();
        endTime = item.getEndTime();
    }

    public Long getId() {
        return id;
    }

    public Long getCurrentStepId() {
        return currentStepId;
    }

    public void setCurrentStepId(Long val) {
        currentStepId = val;
    }

    public String getOrderKey() {
        return orderKey;
    }

    public Long getMainParentId() {
        return mainParentId;
    }

    public String getStartCause() {
        return startCause;
    }

    public String getStartWorkflowPosition() {
        return startWorkflowPosition;
    }

    public String getWorkflowPosition() {
        return workflowPosition;
    }

    public boolean getHasChildren() {
        return hasChildren;
    }

    public void setHasChildren(boolean val) {
        hasChildren = val;
    }

    public Date getEndTime() {
        return endTime;
    }

}
