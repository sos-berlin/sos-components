package com.sos.jobscheduler.history.helper;

import java.util.Date;
import java.util.List;

import com.google.common.base.Joiner;
import com.sos.jobscheduler.event.master.configuration.master.MasterConfiguration;
import com.sos.jobscheduler.event.master.fatevent.bean.OrderForkedChild;
import com.sos.jobscheduler.model.event.EventType;

public class LogEntry {

    public static enum LogType {
        MasterReady(0), AgentReady(1), OrderAdded(2), OrderStarted(3), OrderFailed(4), OrderCancelled(5), OrderEnd(6), Fork(7), ForkBranchStarted(
                8), ForkBranchEnd(9), ForkJoin(10), OrderStepStart(11), OrderStepOut(12), OrderStepEnd(13);

        private int value;

        private LogType(int val) {
            value = val;
        }

        public Long getValue() {
            return new Long(value);
        }
    }

    public static enum OutType {
        Stdout(0), Stderr(1);

        private int value;

        private OutType(int val) {
            value = val;
        }

        public Long getValue() {
            return new Long(value);
        }
    }

    public static enum LogLevel {
        Info(0), Debug(1), Error(2), Warn(3), Trace(4);

        private int value;

        private LogLevel(int val) {
            value = val;
        }

        public Long getValue() {
            return new Long(value);
        }
    }

    private final LogLevel logLevel;
    private final OutType outType;
    private final LogType logType;
    private final String timezone;
    private final Long eventId;
    private final Long eventTimestamp;
    private final Date date;

    private String orderKey = ".";
    private Long mainOrderId = new Long(0);
    private Long orderId = new Long(0);
    private Long orderStepId = new Long(0);
    private String position;
    private String jobName = ".";
    private String agentPath = ".";
    private String agentUri = ".";
    private String chunk;

    private boolean error;
    private String errorState;
    private String errorReason;
    private String errorCode;
    private String errorText;
    private Long returnCode;

    public LogEntry(LogLevel level, OutType out, LogType type, String logTimezone, Long entryEventId, Long entryTimestamp, Date entryDate) {
        logLevel = level;
        outType = out;
        logType = type;
        timezone = logTimezone;
        eventId = entryEventId;
        eventTimestamp = entryTimestamp;
        date = entryDate;
    }

    public void onOrder(CachedOrder order, String position) {
        onOrder(order, position, null);
    }

    public void onOrder(CachedOrder order, String workflowPosition, List<OrderForkedChild> childs) {
        orderKey = order.getOrderKey();
        mainOrderId = order.getMainParentId();
        orderId = order.getId();
        position = workflowPosition;
        if (order.getError()) {
            error = true;
            errorState = order.getErrorState();
            errorReason = order.getErrorReason();
            errorText = order.getErrorText();
            returnCode = order.getErrorReturnCode();
        }
        chunk = order.getOrderKey();
    }

    public void onOrderJoined(CachedOrder order, String workflowPosition, List<String> childs) {
        orderKey = order.getOrderKey();
        mainOrderId = order.getMainParentId();
        orderId = order.getId();
        position = workflowPosition;
        chunk = Joiner.on(",").join(childs);
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
        agentPath = orderStep.getAgentPath();
        agentUri = orderStep.getAgentUri();

        switch (logType) {
        case OrderStepStart:
            chunk = String.format("[start][%s][%s][%s]", agentPath, agentUri, jobName);
            break;
        case OrderStepEnd:
            returnCode = orderStep.getReturnCode();

            StringBuilder c = new StringBuilder("[end]");
            c.append("[").append(agentPath).append("]");
            c.append("[").append(agentUri).append("]");
            c.append("[").append(jobName).append("]");
            c.append("[returnCode=").append(returnCode == null ? "" : returnCode).append("]");
            if (orderStep.getError()) {
                error = true;
                errorState = orderStep.getErrorState();
                errorReason = orderStep.getErrorReason();
                errorCode = orderStep.getErrorCode();
                errorText = orderStep.getErrorText();

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
            break;
        default:
            chunk = entryChunk;
        }
    }

    public void onAgent(CachedAgent agent) {
        agentUri = agent.getUri();

        switch (logType) {
        case AgentReady:
            chunk = String.format("[%s]%s", agent.getPath(), agent.getUri());
            break;
        default:
            break;
        }
    }

    public void onMaster(MasterConfiguration master) {
        switch (logType) {
        case MasterReady:
            chunk = String.format("[%s][primary=%s]%s ", master.getCurrent().getUri(), master.getCurrent().isPrimary(), master.getCurrent().getId());
            break;
        default:
            break;
        }
    }

    public EventType toEventType(String eventType) throws Exception {
        String val = eventType.endsWith("Fat") ? eventType.substring(0, eventType.length() - 3) : eventType;
        return EventType.valueOf(val);
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public OutType getOutType() {
        return outType;
    }

    public LogType getLogType() {
        return logType;
    }

    public Long getEventId() {
        return eventId;
    }

    public Long getEventTimestamp() {
        return eventTimestamp;
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

    public String getAgentUri() {
        return agentUri;
    }

    public String getAgentPath() {
        return agentPath;
    }

    public String getTimezone() {
        return timezone;
    }

    public Date getDate() {
        return date;
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

    public Long getReturnCode() {
        return returnCode;
    }
}
