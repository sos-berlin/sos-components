package com.sos.js7.history.helper;

import com.sos.joc.db.history.DBItemHistoryOrder;
import com.sos.joc.model.order.OrderStateText;

import java.util.Date;

public class CachedOrder {

    private final Long id;
    private final String orderId;
    private final Long mainParentId;
    private final Long parentId;
    private final String startWorkflowPosition;
    private final String workflowPosition;
    private final Date endTime;
    private final Date created;

    private Integer state;
    private boolean hasChildren;
    private boolean hasStates;
    private Long currentHistoryOrderStepId;
    private Date startTime;

    public CachedOrder(DBItemHistoryOrder item) {
        id = item.getId();
        orderId = item.getOrderId();
        mainParentId = item.getMainParentId();
        parentId = item.getParentId();
        startWorkflowPosition = item.getStartWorkflowPosition();
        workflowPosition = item.getWorkflowPosition();
        state = item.getState();
        hasChildren = item.getHasChildren();
        hasStates = item.getHasStates();
        currentHistoryOrderStepId = item.getCurrentHistoryOrderStepId();
        startTime = item.getStartTime();
        endTime = item.getEndTime();
        created = new Date();
    }

    public Long getId() {
        return id;
    }

    public String getOrderId() {
        return orderId;
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

    public void setState(Integer val) {
        state = val;
    }

    public Integer getState() {
        return state;
    }

    public String getStateAsText() {
        try {
            return state == null ? null : OrderStateText.fromValue(state).name();
        } catch (Throwable e) {
            return null;
        }
    }

    public boolean getHasChildren() {
        return hasChildren;
    }

    public void setHasChildren(boolean val) {
        hasChildren = val;
    }

    public boolean getHasStates() {
        return hasStates;
    }

    public void setHasStates(boolean val) {
        hasStates = val;
    }

    public Long getCurrentHistoryOrderStepId() {
        return currentHistoryOrderStepId;
    }

    public void setCurrentHistoryOrderStepId(Long val) {
        currentHistoryOrderStepId = val;
    }

    public void setStartTime(Date val) {
        startTime = val;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public Date getCreated() {
        return created;
    }
}
