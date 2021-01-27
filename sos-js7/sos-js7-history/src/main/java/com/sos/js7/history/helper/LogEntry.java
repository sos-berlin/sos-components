package com.sos.js7.history.helper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.sos.commons.util.SOSString;
import com.sos.controller.model.event.EventType;
import com.sos.joc.model.order.OrderStateText;
import com.sos.js7.history.controller.proxy.HistoryEventEntry.HistoryOrder.OrderLock;
import com.sos.js7.history.controller.proxy.fatevent.AFatEventOrderLock;
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
    private String orderId = ".";
    private Long historyOrderMainParentId = new Long(0L);
    private Long historyOrderId = new Long(0L);
    private Long historyOrderStepId = new Long(0L);
    private String position;
    private String jobName = ".";
    private String agentTimezone = null;
    private String agentId = ".";
    private String agentUri = ".";
    private String chunk;
    private Integer state;
    private boolean error;
    private String errorState;
    private String errorReason;
    private String errorCode;
    private String errorText;
    private Integer returnCode;
    private OrderLock orderLock;

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
        orderId = order.getOrderId();
        historyOrderMainParentId = order.getMainParentId();
        historyOrderId = order.getId();
        position = workflowPosition;
        chunk = order.getOrderId();
    }

    public void onOrderLock(CachedOrder order, AFatEventOrderLock entry) {
        onOrder(order, "...", null); // position TODO
        orderLock = entry.getOrderLock();
    }

    public void setError(String state, CachedOrderStep cos) {
        error = true;
        errorState = state == null ? null : state.toLowerCase();
        errorReason = cos.getError().getReason();
        errorCode = cos.getError().getCode();
        errorText = cos.getError().getText();
    }

    public void setError(String state, FatOutcome outcome) {
        error = true;
        errorState = state == null ? null : state.toLowerCase();
        errorReason = outcome.getType().name();
        errorCode = outcome.getErrorCode();
        errorText = outcome.getErrorMessage();
    }

    public void onOrderJoined(CachedOrder order, String workflowPosition, List<String> childs, FatOutcome outcome) {
        orderId = order.getOrderId();
        historyOrderMainParentId = order.getMainParentId();
        historyOrderId = order.getId();
        position = workflowPosition;
        chunk = String.join(", ", childs);
        if (outcome != null) {
            returnCode = outcome.getReturnCode();
            if (outcome.isFailed()) {
                setError(OrderStateText.FAILED.value(), outcome);
            }
        }
    }

    public void onOrderStep(CachedOrderStep orderStep) {
        onOrderStep(orderStep, null);
    }

    public void onOrderStep(CachedOrderStep orderStep, String entryChunk) {
        orderId = orderStep.getOrderId();
        historyOrderMainParentId = orderStep.getHistoryOrderMainParentId();
        historyOrderId = orderStep.getHistoryOrderId();
        historyOrderStepId = orderStep.getId();
        position = orderStep.getWorkflowPosition();
        jobName = orderStep.getJobName();
        agentTimezone = orderStep.getAgentTimezone();
        agentId = orderStep.getAgentId();
        agentUri = orderStep.getAgentUri();
        StringBuilder sb;
        switch (eventType) {
        case OrderProcessingStarted:
            chunk = String.format("[Start] Job=%s, Agent (url=%s, id=%s)", jobName, agentUri, agentId);
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

    public String getOrderId() {
        return orderId;
    }

    public Long getHistoryOrderMainParentId() {
        return historyOrderMainParentId;
    }

    public Long getHistoryOrderId() {
        return historyOrderId;
    }

    public Long getHistoryOrderStepId() {
        return historyOrderStepId;
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

    public String getAgentId() {
        return agentId;
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

    public void setState(Integer val) {
        state = val;
    }

    public Integer getState() {
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

    public void setErrorText(String val) {
        if (SOSString.isEmpty(val)) {
            val = null;
        }
        errorText = val;
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

    public OrderLock getOrderLock() {
        return orderLock;
    }
}
