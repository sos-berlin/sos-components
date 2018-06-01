package com.sos.jobscheduler.history.helper;

import java.util.Date;

import com.sos.jobscheduler.db.DBItemSchedulerLogs.OutType;
import com.sos.jobscheduler.db.DBItemSchedulerOrderStepHistory;
import com.sos.jobscheduler.event.master.fatevent.bean.Entry;

public class ChunkLogEntry {

    private final OutType outType;
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

    public ChunkLogEntry(OutType out, Entry entry, DBItemSchedulerOrderStepHistory item) {
        outType = out;
        eventId = entry.getEventId();
        orderKey = item.getOrderKey();
        mainOrderHistoryId = item.getMainOrderHistoryId();
        orderHistoryId = item.getOrderHistoryId();
        orderStepHistoryId = item.getId();
        jobPath = item.getJobPath();
        agentUri = item.getAgentUri();
        agentTimezone = "."; // TODO
        date = entry.getTimestamp() == null ? entry.getEventIdAsDate() : entry.getTimestampAsDate();
        chunk = entry.getChunk();
    }

    public OutType getOutType() {
        return outType;
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
