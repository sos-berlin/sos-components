package com.sos.joc.cluster.bean.history;

import java.util.Date;
import java.util.List;

import com.sos.controller.model.event.EventType;
import com.sos.joc.db.history.DBItemHistoryOrder;

public class HistoryOrderBean extends AHistoryBean {

    private String orderId;
    private String workflowPath;
    private String workflowVersionId;
    private String workflowPosition;
    private String workflowFolder;
    private String workflowName;
    private String workflowTitle;// TODO
    private Long mainParentId;
    private Long parentId;
    private String parentOrderId;
    private String name;
    private String startCause;
    private Date startTimePlanned;
    private Date startTime;
    private String startWorkflowPosition;
    private String startParameters;
    private Long currentHistoryOrderStepId;
    private Date endTime;
    private String endWorkflowPosition;
    private Long endHistoryOrderStepId;
    private Integer severity;
    private Integer state;
    private Date stateTime;
    private String stateText;
    private boolean error;
    private String errorState;
    private String errorReason;
    private Integer errorReturnCode;
    private String errorCode;
    private String errorText;
    private Long logId;
    private List<HistoryOrderBean> children;

    public HistoryOrderBean(EventType eventType, Long eventId, String controllerId, Long historyId) {
        super(eventType, eventId, controllerId, historyId);
    }

    public HistoryOrderBean(EventType eventType, Long eventId, DBItemHistoryOrder item) {
        super(eventType, eventId, item.getControllerId(), item.getId());

        this.orderId = item.getOrderId();
        this.workflowPath = item.getWorkflowPath();
        this.workflowVersionId = item.getWorkflowVersionId();
        this.workflowPosition = item.getWorkflowPosition();
        this.workflowFolder = item.getWorkflowFolder();
        this.workflowName = item.getWorkflowName();
        this.workflowTitle = item.getWorkflowTitle();
        this.mainParentId = item.getMainParentId();
        this.parentId = item.getParentId();
        this.parentOrderId = item.getParentOrderId();
        this.name = item.getName();
        this.startCause = item.getStartCause();
        this.startTimePlanned = item.getStartTimePlanned();
        this.startTime = item.getStartTime();
        this.startWorkflowPosition = item.getStartWorkflowPosition();
        this.startParameters = item.getStartParameters();
        this.currentHistoryOrderStepId = item.getCurrentHistoryOrderStepId();
        this.endTime = item.getEndTime();
        this.endWorkflowPosition = item.getEndWorkflowPosition();
        this.endHistoryOrderStepId = item.getEndHistoryOrderStepId();
        this.severity = item.getSeverity();
        this.state = item.getState();
        this.stateTime = item.getStateTime();
        this.stateText = item.getStateText();
        this.error = item.getError();
        this.errorState = item.getErrorState();
        this.errorReason = item.getErrorReason();
        this.errorReturnCode = item.getErrorReturnCode();
        this.errorCode = item.getErrorCode();
        this.errorText = item.getErrorText();
        this.logId = item.getLogId();
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String val) {
        orderId = val;
    }

    public String getWorkflowPath() {
        return workflowPath;
    }

    public void setWorkflowPath(String val) {
        workflowPath = val;
    }

    public String getWorkflowVersionId() {
        return workflowVersionId;
    }

    public void setWorkflowVersionId(String val) {
        workflowVersionId = val;
    }

    public String getWorkflowPosition() {
        return workflowPosition;
    }

    public void setWorkflowPosition(String val) {
        workflowPosition = val;
    }

    public String getWorkflowFolder() {
        return workflowFolder;
    }

    public void setWorkflowFolder(String val) {
        workflowFolder = val;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String val) {
        workflowName = val;
    }

    public String getWorkflowTitle() {
        return workflowTitle;
    }

    public void setWorkflowTitle(String val) {
        workflowTitle = val;
    }

    public Long getMainParentId() {
        return mainParentId;
    }

    public void setMainParentId(Long val) {
        mainParentId = val;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long val) {
        parentId = val;
    }

    public String getParentOrderId() {
        return parentOrderId;
    }

    public void setParentOrderId(String val) {
        parentOrderId = val;
    }

    public String getName() {
        return name;
    }

    public void setName(String val) {
        name = val;
    }

    public String getStartCause() {
        return startCause;
    }

    public void setStartCause(String val) {
        startCause = val;
    }

    public Date getStartTimePlanned() {
        return startTimePlanned;
    }

    public void setStartTimePlanned(Date val) {
        startTimePlanned = val;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date val) {
        startTime = val;
    }

    public String getStartWorkflowPosition() {
        return startWorkflowPosition;
    }

    public void setStartWorkflowPosition(String val) {
        startWorkflowPosition = val;
    }

    public String getStartParameters() {
        return startParameters;
    }

    public void setStartParameters(String val) {
        startParameters = val;
    }

    public Long getCurrentHistoryOrderStepId() {
        return currentHistoryOrderStepId;
    }

    public void setCurrentHistoryOrderStepId(Long val) {
        currentHistoryOrderStepId = val;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date val) {
        endTime = val;
    }

    public String getEndWorkflowPosition() {
        return endWorkflowPosition;
    }

    public void setEndWorkflowPosition(String val) {
        endWorkflowPosition = val;
    }

    public void setEndHistoryOrderStepId(Long val) {
        endHistoryOrderStepId = val;
    }

    public Long getEndHistoryOrderStepId() {
        return endHistoryOrderStepId;
    }

    public Integer getSeverity() {
        return severity;
    }

    public void setSeverity(Integer val) {
        severity = val;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer val) {
        state = val;
    }

    public Date getStateTime() {
        return stateTime;
    }

    public void setStateTime(Date val) {
        stateTime = val;
    }

    public String getStateText() {
        return stateText;
    }

    public void setStateText(String val) {
        stateText = val;
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

    public void setErrorReturnCode(Integer val) {
        errorReturnCode = val;
    }

    public Integer getErrorReturnCode() {
        return errorReturnCode;
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

    public void setChildren(List<HistoryOrderBean> val) {
        children = val;
    }

    public List<HistoryOrderBean> getChildren() {
        return children;
    }

}
