package com.sos.joc.cluster.bean.history;

import java.util.Date;

import com.sos.controller.model.event.EventType;
import com.sos.joc.db.history.DBItemHistoryOrderStep;

public class HistoryOrderStepBean extends AHistoryBean {

    private String workflowPosition;
    private String workflowPath;
    private Long historyOrderMainParentId;
    private Long historyOrderId;
    private String orderId;
    private Integer position;
    private String jobName;
    private String jobLabel;
    private String jobTitle;
    private Integer criticality;
    private String agentId;
    private String agentUri;
    private String startCause;
    private Date startTime;
    private String startVariables;
    private Date endTime;
    private String endVariables;
    private Integer returnCode;
    private Integer severity;
    private boolean error;
    private String errorState;
    private String errorReason;
    private String errorCode;
    private String errorText;
    private Long logId;

    private Integer warnIfLonger;
    private Integer warnIfShorter;

    public HistoryOrderStepBean(EventType eventType, Long eventId, String controllerId, Long historyId) {
        super(eventType, eventId, controllerId, historyId);
    }

    public HistoryOrderStepBean(EventType eventType, Long eventId, DBItemHistoryOrderStep item, Integer warnIfLonger, Integer warnIfShorter) {
        super(eventType, eventId, item.getControllerId(), item.getId());

        this.workflowPosition = item.getWorkflowPosition();
        this.workflowPath = item.getWorkflowPath();
        this.historyOrderMainParentId = item.getHistoryOrderMainParentId();
        this.historyOrderId = item.getHistoryOrderId();
        this.orderId = item.getOrderId();
        this.position = item.getPosition();
        this.jobName = item.getJobName();
        this.jobLabel = item.getJobLabel();
        this.jobTitle = item.getJobTitle();
        this.criticality = item.getCriticality();
        this.agentId = item.getAgentId();
        this.agentUri = item.getAgentUri();
        this.startCause = item.getStartCause();
        this.startTime = item.getStartTime();
        this.startVariables = item.getStartVariables();
        this.endTime = item.getEndTime();
        this.endVariables = item.getEndVariables();
        this.returnCode = item.getReturnCode();
        this.severity = item.getSeverity();
        this.error = item.getError();
        this.errorState = item.getErrorState();
        this.errorReason = item.getErrorReason();
        this.errorCode = item.getErrorCode();
        this.errorText = item.getErrorText();
        this.logId = item.getLogId();

        this.warnIfLonger = warnIfLonger;
        this.warnIfShorter = warnIfShorter;
    }

    public String getWorkflowPosition() {
        return workflowPosition;
    }

    public void setWorkflowPosition(String val) {
        workflowPosition = val;
    }

    public String getWorkflowPath() {
        return workflowPath;
    }

    public void setWorkflowPath(String val) {
        workflowPath = val;
    }

    public Long getHistoryOrderMainParentId() {
        return historyOrderMainParentId;
    }

    public void setHistoryOrderMainParentId(Long val) {
        historyOrderMainParentId = val;
    }

    public Long getHistoryOrderId() {
        return historyOrderId;
    }

    public void setHistoryOrderId(Long val) {
        historyOrderId = val;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String val) {
        orderId = val;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer val) {
        position = val;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String val) {
        jobName = val;
    }

    public String getJobLabel() {
        return jobLabel;
    }

    public void setJobLabel(String val) {
        jobLabel = val;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String val) {
        jobTitle = val;
    }

    public Integer getCriticality() {
        return criticality;
    }

    public void setCriticality(Integer val) {
        criticality = val;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String val) {
        agentId = val;
    }

    public String getAgentUri() {
        return agentUri;
    }

    public void setAgentUri(String val) {
        agentUri = val;
    }

    public String getStartCause() {
        return startCause;
    }

    public void setStartCause(String val) {
        startCause = val;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date val) {
        startTime = val;
    }

    public String getStartVariables() {
        return startVariables;
    }

    public void setStartVariables(String val) {
        startVariables = val;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date val) {
        endTime = val;
    }

    public String getEndVariables() {
        return endVariables;
    }

    public void setEndVariables(String val) {
        endVariables = val;
    }

    public Integer getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(Integer val) {
        returnCode = val;
    }

    public Integer getSeverity() {
        return severity;
    }

    public void setSeverity(Integer val) {
        severity = val;
    }

    public void setError(boolean val) {
        error = val;
    }

    public boolean getError() {
        return error;
    }

    public void setErrorState(String val) {
        errorState = val;
    }

    public String getErrorState() {
        return errorState;
    }

    public void setErrorReason(String val) {
        errorReason = val;
    }

    public String getErrorReason() {
        return errorReason;
    }

    public void setErrorCode(String val) {
        errorCode = val;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorText(String val) {
        errorText = val;
    }

    public String getErrorText() {
        return errorText;
    }

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long val) {
        logId = val;
    }

    public void setWarnIfLonger(Integer val) {
        warnIfLonger = val;
    }

    public Integer getWarnIfLonger() {
        return warnIfLonger;
    }

    public void setWarnIfShorter(Integer val) {
        warnIfShorter = val;
    }

    public Integer getWarnIfShorter() {
        return warnIfShorter;
    }

}
