package com.sos.jobscheduler.history.helper;

import java.util.Date;

import com.sos.jobscheduler.db.history.DBItemOrder;

public class CachedOrder {

    private final Long id;
    private final Long parentId;
    private final String orderKey;
    private final Long mainParentId;
    private final String startCause;
    private final String startWorkflowPosition;
    private final String workflowPosition;
    private final Date endTime;
    private boolean error;
    private String errorStatus;
    private String errorReason;
    private String errorText;
    private Long errorReturnCode;

    private Long currentOrderStepId;
    private boolean hasChildren;
    private Date lastOrderStepEndTime;

    public CachedOrder(final DBItemOrder item) {
        id = item.getId();
        parentId = item.getParentId();
        currentOrderStepId = item.getCurrentOrderStepId();
        orderKey = item.getOrderKey();
        mainParentId = item.getMainParentId();
        startCause = item.getStartCause();
        startWorkflowPosition = item.getStartWorkflowPosition();
        workflowPosition = item.getWorkflowPosition();
        hasChildren = item.getHasChildren();
        endTime = item.getEndTime();
        error = item.getError();
        errorStatus = item.getErrorStatus();
        errorReason = item.getErrorReason();
        errorReturnCode = item.getErrorReturnCode();
    }

    public Long getId() {
        return id;
    }

    public Long getParentId() {
        return parentId;
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

    public boolean getError() {
        return error;
    }

    public void setError(boolean val) {
        error = val;
    }

    public String getErrorStatus() {
        return errorStatus;
    }

    public void setErrorStatus(String val) {
        errorStatus = val;
    }

    public String getErrorReason() {
        return errorReason;
    }

    public void setErrorReason(String val) {
        errorReason = val;
    }

    public String getErrorText() {
        return errorText;
    }

    public void setErrorText(String val) {
        errorText = val;
    }

    public void setErrorReturnCode(Long val) {
        errorReturnCode = val;
    }

    public Long getErrorReturnCode() {
        return errorReturnCode;
    }
}
