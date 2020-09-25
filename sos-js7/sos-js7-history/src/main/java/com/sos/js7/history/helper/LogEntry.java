package com.sos.js7.history.helper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.sos.commons.util.SOSString;
import com.sos.jobscheduler.model.event.EventType;
import com.sos.js7.history.controller.proxy.fatevent.FatForkedChild;
import com.sos.js7.history.controller.proxy.fatevent.FatOutcome;

public class LogEntry {

    public enum LogLevel {
        MAIN, DETAIL, INFO, ERROR;
    }

    private LogLevel logLevel;
    private final EventType eventType;
    private final Date controllerDatetime;
    private final Date agentDatetime;
    private String orderKey = ".";
    private Long mainOrderId = new Long(0L);
    private Long orderId = new Long(0L);
    private Long orderStepId = new Long(0L);
    private String position;
    private String jobName = ".";
    private String agentTimezone = null;
    private String agentPath = ".";
    private String agentUri = ".";
    private String chunk;
    private String state;
    private boolean error;
    private String errorState;
    private String errorReason;
    private String errorCode;
    private String errorText;
    private Integer returnCode;

    public LogEntry(LogLevel level, EventType type, Date controllerDate, Date agentDate) {
        logLevel = level;
        eventType = type;
        controllerDatetime = controllerDate;
        agentDatetime = agentDate;
    }

    public void onOrder(CachedOrder order, String position) {
        onOrder(order, position, null);
    }

    public void onOrder(CachedOrder order, String workflowPosition, List<FatForkedChild> childs) {
        orderKey = order.getOrderKey();
        mainOrderId = order.getMainParentId();
        orderId = order.getId();
        position = workflowPosition;
        chunk = order.getOrderKey();
    }

    public void setError(String state, String reason, String text) {
        error = true;
        errorState = state;
        errorReason = reason;
        errorText = text;
    }

    public void onOrderJoined(CachedOrder order, String workflowPosition, List<String> childs, FatOutcome outcome) {
        orderKey = order.getOrderKey();
        mainOrderId = order.getMainParentId();
        orderId = order.getId();
        position = workflowPosition;
        chunk = String.join(", ", childs);
        if (outcome != null) {
            returnCode = outcome.getReturnCode();
            if (outcome.isFailed()) {
                String errorReason = null;
                String errorText = null;
                // if (outcome.getReason() != null) {
                // errorReason = outcome.getReason().getType();
                // errorText = outcome.getReason().getProblem().getMessage();
                // }
                if (!SOSString.isEmpty(outcome.getErrorMessage())) {
                    errorText = outcome.getErrorMessage();
                }
                // TODO
                setError("failed", errorReason, errorText);
            }
        }
    }

    public void onOrderStep(CachedOrderStep orderStep) {
        onOrderStep(orderStep, null);
    }

    public void onOrderStep(CachedOrderStep orderStep, String entryChunk) {
        orderKey = orderStep.getOrderKey();
        mainOrderId = orderStep.getMainOrderId();
        orderId = orderStep.getOrderId();
        orderStepId = orderStep.getId();
        position = orderStep.getWorkflowPosition();
        jobName = orderStep.getJobName();
        agentTimezone = orderStep.getAgentTimezone();
        agentPath = orderStep.getAgentPath();
        agentUri = orderStep.getAgentUri();
        StringBuilder sb;
        switch (eventType) {
        case OrderProcessingStarted:
            chunk = String.format("[Start] Job=%s, Agent (url=%s, path=%s)", jobName, agentUri, agentPath);
            return;
        case OrderProcessed:
            returnCode = orderStep.getReturnCode();
            sb = new StringBuilder("[End]");
            if (error) {
                sb.append(" [Error]");
            } else {
                sb.append(" [Success]");
            }
            sb.append(" returnCode=").append((returnCode == null) ? "" : returnCode);
            if (error) {
                orderStep.setError(errorState, errorReason, errorCode, errorText);
                List<String> errorInfo = new ArrayList<String>();
                if (errorState != null) {
                    errorInfo.add("errorState=" + errorState);
                }
                if (errorCode != null) {
                    errorInfo.add("code=" + errorCode);
                }
                if (errorReason != null) {
                    errorInfo.add("reason=" + errorReason);
                }
                if (errorText != null) {
                    errorInfo.add("msg=" + errorText);
                }
                if (errorInfo.size() > 0) {
                    sb.append(", ").append(String.join(", ", errorInfo));
                }
            }
            chunk = sb.toString();
            return;
        default:
            chunk = entryChunk;
            break;
        }

    }

    public void setLogLevel(LogLevel val) {
        logLevel = val;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public EventType getEventType() {
        return eventType;
    }

    public String getOrderKey() {
        return orderKey;
    }

    public Long getMainOrderId() {
        return mainOrderId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getOrderStepId() {
        return orderStepId;
    }

    public String getPosition() {
        return position;
    }

    public String getJobName() {
        return jobName;
    }

    public String getAgentTimezone() {
        return agentTimezone;
    }

    public void setAgentTimezone(String val) {
        agentTimezone = val;
    }

    public String getAgentUri() {
        return agentUri;
    }

    public String getAgentPath() {
        return agentPath;
    }

    public Date getControllerDatetime() {
        return controllerDatetime;
    }

    public Date getAgentDatetime() {
        return agentDatetime;
    }

    public String getChunk() {
        return chunk;
    }

    public void setState(String val) {
        state = val;
    }

    public String getState() {
        return state;
    }

    public boolean isError() {
        return error;
    }

    public String getErrorState() {
        return errorState;
    }

    public String getErrorReason() {
        return errorReason;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorText() {
        return errorText;
    }

    public void setReturnCode(Integer val) {
        returnCode = val;
    }

    public Integer getReturnCode() {
        return returnCode;
    }
}
