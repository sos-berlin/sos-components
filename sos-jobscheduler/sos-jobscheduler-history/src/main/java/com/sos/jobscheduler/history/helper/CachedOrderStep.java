package com.sos.jobscheduler.history.helper;

import java.util.Date;

import com.sos.jobscheduler.db.history.DBItemOrderStep;

public class CachedOrderStep {

    private final Long id;
    private final Long mainOrderId;
    private final Long orderId;
    private final String orderKey;
    private final String jobName;
    private final String agentPath;
    private final String agentUri;
    private final String workflowPosition;
    private final Date endTime;
    private boolean error;
    private String errorStatus;
    private String errorReason;
    private String errorCode;
    private String errorText;
    private Long returnCode;

    public CachedOrderStep(DBItemOrderStep item) {
        id = item.getId();
        mainOrderId = item.getMainOrderId();
        orderId = item.getOrderId();
        orderKey = item.getOrderKey();
        jobName = item.getJobName();
        agentPath = item.getAgentPath();
        agentUri = item.getAgentUri();
        workflowPosition = item.getWorkflowPosition();
        endTime = item.getEndTime();
        error = item.getError();
        errorStatus = item.getErrorStatus();
        errorReason = item.getErrorReason();
        errorCode = item.getErrorCode();
        errorText = item.getErrorText();
        returnCode = item.getReturnCode();
    }

    public Long getId() {
        return id;
    }

    public Long getMainOrderId() {
        return mainOrderId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getOrderKey() {
        return orderKey;
    }

    public String getJobName() {
        return jobName;
    }

    public String getAgentPath() {
        return agentPath;
    }

    public String getAgentUri() {
        return agentUri;
    }

    public String getWorkflowPosition() {
        return workflowPosition;
    }

    public Date getEndTime() {
        return endTime;
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

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String val) {
        errorCode = val;
    }

    public String getErrorText() {
        return errorText;
    }

    public void setErrorText(String val) {
        errorText = val;
    }

    public void setReturnCode(Long val) {
        returnCode = val;
    }

    public Long getReturnCode() {
        return returnCode;
    }
}
