package com.sos.jobscheduler.history.helper;

import java.util.Date;
import java.util.List;

import com.sos.jobscheduler.db.history.DBItemLog.LogLevel;
import com.sos.jobscheduler.db.history.DBItemLog.LogType;
import com.sos.jobscheduler.db.history.DBItemLog.OutType;
import com.sos.jobscheduler.event.master.fatevent.bean.OrderForkedChild;
import com.sos.jobscheduler.event.master.handler.EventHandlerMasterSettings;

public class ChunkLogEntry {

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
    private String jobName = ".";
    private String agentUri = ".";
    private String chunk;

    public ChunkLogEntry(LogLevel level, OutType out, LogType type, String logTimezone, Long entryEventId, Long entryTimestamp, Date entryDate) {
        logLevel = level;
        outType = out;
        logType = type;
        timezone = logTimezone;
        eventId = entryEventId;
        eventTimestamp = entryTimestamp;
        date = entryDate;
    }

    public void onOrder(CachedOrder order) {
        onOrder(order, null);
    }

    public void onOrder(CachedOrder order, List<OrderForkedChild> childs) {
        orderKey = order.getOrderKey();
        mainOrderId = order.getMainParentId();
        orderId = order.getId();

        switch (logType) {
        case OrderAdded:
            chunk = String.format("[order][added]%s", order.getOrderKey());
            break;
        case OrderStart:
            chunk = String.format("[order][started][%s]%s", order.getOrderKey(), order.getStartCause());
            break;
        case OrderForked:
            chunk = String.format("[order][forked]%s", order.getOrderKey());
            break;
        case OrderEnd:
            chunk = String.format("[order][finished]%s", order.getOrderKey());
            break;
        default:
            break;
        }
    }

    public void onOrderJoined(CachedOrder order, List<String> childs) {
        orderKey = order.getOrderKey();
        mainOrderId = order.getMainParentId();
        orderId = order.getId();
        chunk = String.format("[order][joined]%s", order.getOrderKey());
    }

    public void onOrderStep(CachedOrderStep orderStep) {
        onOrderStep(orderStep, null);
    }

    public void onOrderStep(CachedOrderStep orderStep, String entryChunk) {
        orderKey = orderStep.getOrderKey();
        mainOrderId = orderStep.getMainOrderId();
        orderId = orderStep.getOrderId();
        orderStepId = orderStep.getId();
        jobName = orderStep.getJobName();
        agentUri = orderStep.getAgentUri();

        switch (logType) {
        case OrderStepStart:
            chunk = String.format("[order step][started][%s][%s][%s]%s", orderStep.getOrderKey(), orderStep.getAgentUri(), orderStep
                    .getWorkflowPosition(), orderStep.getJobName());
            break;
        case OrderStepEnd:
            StringBuilder c = new StringBuilder("[order step][finished][");
            c.append(orderStep.getOrderKey());
            c.append("][");
            c.append(orderStep.getAgentUri());
            c.append("][");
            c.append(orderStep.getWorkflowPosition());
            c.append("]");
            if (orderStep.getError()) {
                c.append("[");
                c.append(orderStep.getJobName());
                c.append("][error]");
                c.append(orderStep.getErrorText());
            } else {
                c.append(orderStep.getJobName());
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
            chunk = String.format("[agent][ready][%s]%s", agent.getPath(), agent.getUri());
            break;
        default:
            break;
        }
    }

    public void onMaster(EventHandlerMasterSettings masterSettings) {
        switch (logType) {
        case MasterReady:
            chunk = String.format("[master][ready][%s:%s]%s ", masterSettings.getHostname(), masterSettings.getPort(), masterSettings.getId());
            break;
        default:
            break;
        }
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

    public String getJobName() {
        return jobName;
    }

    public String getAgentUri() {
        return agentUri;
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

}
