package com.sos.jobscheduler.history.helper;

import java.util.Date;

import com.sos.jobscheduler.db.DBItemSchedulerLogs.LogLevel;
import com.sos.jobscheduler.db.DBItemSchedulerLogs.LogType;
import com.sos.jobscheduler.db.DBItemSchedulerLogs.OutType;
import com.sos.jobscheduler.event.master.fatevent.bean.Entry;

public class ChunkLogEntry {

    private final LogLevel logLevel;
    private final OutType outType;
    private final LogType logType;
    private final Long eventId;
    private final String orderKey;
    private final Long mainOrderHistoryId;
    private final Long orderHistoryId;
    private final Long orderStepHistoryId;
    private final String jobPath;
    private final String agentUri;
    private final String timezone;
    private final Date date;
    private final String chunk;

    public ChunkLogEntry(LogLevel level, OutType out, LogType type, Entry entry, CachedOrderStep orderStep) {
        logLevel = level;
        outType = out;
        logType = type;
        eventId = entry.getEventId();
        orderKey = orderStep.getOrderKey();
        mainOrderHistoryId = orderStep.getMainOrderHistoryId();
        orderHistoryId = orderStep.getOrderHistoryId();
        orderStepHistoryId = orderStep.getId();
        jobPath = orderStep.getJobPath();
        agentUri = orderStep.getAgentUri();
        timezone = "."; // TODO
        date = entry.getTimestamp() == null ? entry.getEventIdAsDate() : entry.getTimestampAsDate();
        switch (logType) {
        case OrderStepStart:
            chunk = String.format("order step started: %s, jobPath=%s, agentUri=%s", orderStep.getOrderKey(), orderStep.getJobPath(), orderStep
                    .getAgentUri());
            break;
        case OrderStepEnd:
            chunk = String.format("order step ended: %s, jobPath=%s, agentUri=%s", orderStep.getOrderKey(), orderStep.getJobPath(), orderStep
                    .getAgentUri());
            break;
        default:
            chunk = entry.getChunk();
        }
    }

    public ChunkLogEntry(LogLevel level, OutType out, LogType type, Entry entry, CachedOrder order) {
        logLevel = level;
        outType = out;
        logType = type;
        eventId = entry.getEventId();
        orderKey = order.getOrderKey();
        mainOrderHistoryId = order.getMainParentId();
        orderHistoryId = order.getId();
        orderStepHistoryId = new Long(0);
        jobPath = ".";
        agentUri = ".";
        timezone = "."; // TODO
        date = entry.getTimestamp() == null ? entry.getEventIdAsDate() : entry.getTimestampAsDate();
        switch (logType) {
        case OrderAdded:
            chunk = String.format("order added: %s", order.getOrderKey());
            break;
        case OrderStart:
            chunk = String.format("order started: %s, cause=%s", order.getOrderKey(), order.getStartCause());
            break;
        case OrderEnd:
            chunk = String.format("order ended: %s", order.getOrderKey());
            break;
        default:
            chunk = "";
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

    public String getOrderKey() {
        return orderKey;
    }

    public Long getMainOrderHistoryId() {
        return mainOrderHistoryId;
    }

    public Long getOrderHistoryId() {
        return orderHistoryId;
    }

    public Long getOrderStepHistoryId() {
        return orderStepHistoryId;
    }

    public String getJobPath() {
        return jobPath;
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
