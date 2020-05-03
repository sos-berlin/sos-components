package com.sos.jobscheduler.history.helper;

import com.google.common.base.Joiner;
import com.sos.jobscheduler.event.master.configuration.master.MasterConfiguration;
import com.sos.jobscheduler.event.master.fatevent.bean.OrderForkedChild;
import com.sos.jobscheduler.event.master.fatevent.bean.Outcome;
import com.sos.jobscheduler.history.master.model.HistoryModel;
import com.sos.jobscheduler.model.event.EventType;
import java.util.Date;
import java.util.List;

public class LogEntry {

    public enum LogLevel {
        Info, Debug, Error, Warn, Trace;
    }

    private final LogLevel logLevel;
    private final EventType eventType;
    private final Date masterDatetime;
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
    private boolean error;
    private String errorState;
    private String errorReason;
    private String errorCode;
    private String errorText;
    private Long returnCode;

    public LogEntry(LogLevel level, EventType type, Date masterDate, Date agentDate) {
        logLevel = level;
        eventType = type;
        masterDatetime = masterDate;
        agentDatetime = agentDate;
    }

    public void onOrder(CachedOrder order, String position) {
        onOrder(order, position, null);
    }

    public void onOrder(CachedOrder order, String workflowPosition, List<OrderForkedChild> childs) {
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

    public void onOrderJoined(CachedOrder order, String workflowPosition, List<String> childs, Outcome outcome) {
        orderKey = order.getOrderKey();
        mainOrderId = order.getMainParentId();
        orderId = order.getId();
        position = workflowPosition;
        chunk = Joiner.on(",").join(childs);
        if (outcome != null) {
            returnCode = outcome.getReturnCode();
            if (outcome.getType().equalsIgnoreCase(HistoryModel.OrderErrorType.failed.name())) {
                String errorReason = null;
                String errorText = null;
                if (outcome.getReason() != null) {
                    errorReason = outcome.getReason().getType();
                    errorText = outcome.getReason().getProblem().getMessage();
                }
                setError(outcome.getType().toLowerCase(), errorReason, errorText);
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
        StringBuilder c;
        switch (eventType) {
        case ORDER_PROCESSING_STARTED:
            chunk = String.format("[start][%s][%s][%s]", agentPath, agentUri, jobName);
            return;
        case ORDER_PROCESSED:
            returnCode = orderStep.getReturnCode();
            c = new StringBuilder("[end]");
            c.append("[").append(agentPath).append("]");
            c.append("[").append(agentUri).append("]");
            c.append("[").append(jobName).append("]");
            c.append("[returnCode=").append((returnCode == null) ? "" : returnCode).append("]");
            if (error) {
                orderStep.setError(errorState, errorReason, errorCode, errorText);
                c.append("[ERROR]");
                if (errorState != null) {
                    c.append("[").append(errorState).append("]");
                }
                if (errorReason != null) {
                    c.append("[").append(errorReason).append("]");
                }
                if (errorCode != null) {
                    c.append("[").append(errorCode).append("]");
                }
                if (errorText != null) {
                    c.append(errorText);
                }
            } else {
                c.append("[success]");
            }
            chunk = c.toString();
            return;
        default:
            chunk = entryChunk;
            break;
        }

    }

    public void onAgent(CachedAgent agent) {
        agentTimezone = agent.getTimezone();
        agentUri = agent.getUri();
        chunk = String.format("[%s]%s", agent.getPath(), agent.getUri());
    }

    public void onMaster(MasterConfiguration master) {
        chunk = String.format("[%s][primary=%s]%s ", master.getCurrent().getUri(), master.getCurrent().isPrimary(), master.getCurrent()
                .getJobSchedulerId());
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

    public Date getMasterDatetime() {
        return masterDatetime;
    }

    public Date getAgentDatetime() {
        return agentDatetime;
    }

    public String getChunk() {
        return chunk;
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

    public void setReturnCode(Long val) {
        returnCode = val;
    }

    public Long getReturnCode() {
        return returnCode;
    }
}
