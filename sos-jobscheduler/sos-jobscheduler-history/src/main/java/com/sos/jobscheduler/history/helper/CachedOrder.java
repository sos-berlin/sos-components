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

    private Long currentOrderStepId;
    private boolean hasChildren;
    private Date lastOrderStepEndTime;

    public CachedOrder(final DBItemOrder item) {
        id = item.getId();
        currentOrderStepId = item.getCurrentOrderStepId();
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

    public Long getCurrentOrderStepId() {
        return currentOrderStepId;
    }

    public void setCurrentOrderStepId(Long val) {
        currentOrderStepId = val;
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

    public Date getLastOrderStepEndTime() {
        return lastOrderStepEndTime;
    }

    public void setLastOrderStepEndTime(Date val) {
        lastOrderStepEndTime = val;
    }
}
