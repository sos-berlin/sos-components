package com.sos.jobscheduler.history.helper;

import com.sos.jobscheduler.db.history.DBItemOrder;
import java.util.Date;

public class CachedOrder {

    private final Long id;
    private final String orderKey;
    private final Long mainParentId;
    private final Long parentId;
    private final String startWorkflowPosition;
    private final String workflowPosition;
    private String state;
    private boolean hasChildren;
    private Long currentOrderStepId;
    private final Date endTime;
    private final Date created;

    public CachedOrder(DBItemOrder item) {
        id = item.getId();
        orderKey = item.getOrderKey();
        mainParentId = item.getMainParentId();
        parentId = item.getParentId();
        startWorkflowPosition = item.getStartWorkflowPosition();
        workflowPosition = item.getWorkflowPosition();
        state = item.getState();
        hasChildren = item.getHasChildren();
        currentOrderStepId = item.getCurrentOrderStepId();
        endTime = item.getEndTime();
        created = new Date();
    }

    public Long getId() {
        return id;
    }

    public String getOrderKey() {
        return orderKey;
    }

    public Long getMainParentId() {
        return mainParentId;
    }

    public Long getParentId() {
        return parentId;
    }

    public String getStartWorkflowPosition() {
        return startWorkflowPosition;
    }

    public String getWorkflowPosition() {
        return workflowPosition;
    }

    public void setState(String val) {
        state = val;
    }

    public String getState() {
        return state;
    }

    public boolean getHasChildren() {
        return hasChildren;
    }

    public void setHasChildren(boolean val) {
        hasChildren = val;
    }

    public Long getCurrentOrderStepId() {
        return currentOrderStepId;
    }

    public void setCurrentOrderStepId(Long val) {
        currentOrderStepId = val;
    }

    public Date getEndTime() {
        return endTime;
    }

    public Date getCreated() {
        return created;
    }
}
