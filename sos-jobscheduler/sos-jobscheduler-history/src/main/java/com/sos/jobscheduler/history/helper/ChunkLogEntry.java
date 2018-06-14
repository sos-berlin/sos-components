package com.sos.jobscheduler.history.helper;

import java.util.Date;

import com.sos.jobscheduler.db.DBItemSchedulerLogs.LogLevel;
import com.sos.jobscheduler.db.DBItemSchedulerLogs.LogType;
import com.sos.jobscheduler.db.DBItemSchedulerLogs.OutType;
import com.sos.jobscheduler.db.DBItemSchedulerOrderHistory;
import com.sos.jobscheduler.db.DBItemSchedulerOrderStepHistory;
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
    private final String agentTimezone;
    private final Date date;
    private final String chunk;

    public ChunkLogEntry(LogLevel level, OutType out, LogType type, Entry entry, DBItemSchedulerOrderStepHistory item) {
        logLevel = level;
        outType = out;
        logType = type;
        eventId = entry.getEventId();
        orderKey = item.getOrderKey();
        mainOrderHistoryId = item.getMainOrderHistoryId();
        orderHistoryId = item.getOrderHistoryId();
        orderStepHistoryId = item.getId();
        jobPath = item.getJobPath();
        agentUri = item.getAgentUri();
        agentTimezone = "."; // TODO
        date = entry.getTimestamp() == null ? entry.getEventIdAsDate() : entry.getTimestampAsDate();
        switch (logType) {
        case OrderStepStart:
            chunk = String.format("order step started: %s, jobPath=%s, agentUri=%s", item.getOrderKey(), item.getJobPath(), item.getAgentUri());
            break;
        case OrderStepEnd:
            chunk = String.format("order step ended: %s, jobPath=%s, agentUri=%s", item.getOrderKey(), item.getJobPath(), item.getAgentUri());
            break;
        default:
            chunk = entry.getChunk();
        }
    }

    public ChunkLogEntry(LogLevel level, OutType out, LogType type, Entry entry, DBItemSchedulerOrderHistory item) {
        logLevel = level;
        outType = out;
        logType = type;
        eventId = entry.getEventId();
        orderKey = item.getOrderKey();
        mainOrderHistoryId = item.getMainParentId();
        orderHistoryId = item.getId();
        orderStepHistoryId = new Long(0);
        jobPath = ".";
        agentUri = ".";
        agentTimezone = "."; // TODO
        date = entry.getTimestamp() == null ? entry.getEventIdAsDate() : entry.getTimestampAsDate();
        switch (logType) {
        case OrderAdded:
            chunk = String.format("order added: %s", item.getOrderKey());
            break;
        case OrderStart:
            chunk = String.format("order started: %s, cause=%s", item.getOrderKey(), item.getStartCause());
            break;
        case OrderEnd:
            chunk = String.format("order ended: %s", item.getOrderKey());
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

    public String getAgentTimezone() {
        return agentTimezone;
    }

    public Date getDate() {
        return date;
    }

    public String getChunk() {
        return chunk;
    }

}
