package com.sos.jobscheduler.event.master.fatevent.bean;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import com.sos.commons.util.SOSString;
import com.sos.jobscheduler.event.master.EventMeta;
import com.sos.jobscheduler.event.master.bean.IEntry;
import com.sos.jobscheduler.event.master.fatevent.EventMeta.EventType;

public class Entry implements IEntry {

    private Long eventId;
    private Long timestamp;
    private String key;
    private EventType type;
    private WorkflowPosition workflowPosition;
    private Long scheduledAt;
    private Outcome outcome;
    private LinkedHashMap<String, String> variables;
    private String agentPath;
    private String agentUri;
    private String jobPath;
    private String chunk;
    private String masterId;
    private String timezone;
    private List<OrderForkedChild> children;
    private List<String> childOrderIds;

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long val) {
        eventId = val;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long val) {
        timestamp = val;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String val) {
        key = val;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType val) {
        type = val;
    }

    public WorkflowPosition getWorkflowPosition() {
        return workflowPosition;
    }

    public void setWorkflowPosition(WorkflowPosition val) {
        workflowPosition = val;
    }

    public Long getScheduledAt() {
        return scheduledAt;
    }

    public Date getSchedulerAtAsDate() {
        return scheduledAt == null ? null : Date.from(EventMeta.timestamp2Instant(scheduledAt));
    }

    public void setScheduledAt(Long val) {
        scheduledAt = val;
    }

    public String getAgentPath() {
        return agentPath;
    }

    public void setAgentPath(String val) {
        agentPath = val;
    }

    public String getAgentUri() {
        return agentUri;
    }

    public void setAgentUri(String val) {
        agentUri = val;
    }

    public String getJobPath() {
        return jobPath;
    }

    public void setJobPath(String val) {
        jobPath = val;
    }

    public String getChunk() {
        return chunk;
    }

    public void setChunk(String val) {
        chunk = val;
    }

    public Outcome getOutcome() {
        return outcome;
    }

    public void setOutcome(Outcome val) {
        outcome = val;
    }

    public LinkedHashMap<String, String> getVariables() {
        return variables;
    }

    public void setVariables(LinkedHashMap<String, String> val) {
        variables = val;
    }

    public String getMasterId() {
        return masterId;
    }

    public void setMasterId(String val) {
        masterId = val;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String val) {
        timezone = val;
    }

    public List<OrderForkedChild> getChildren() {
        return children;
    }

    public void setChildren(List<OrderForkedChild> val) {
        children = val;
    }

    public List<String> getChildOrderIds() {
        return childOrderIds;
    }

    public void setChildOrderIds(List<String> val) {
        childOrderIds = val;
    }

    public Date getEventDate() {
        return timestamp == null ? getEventIdAsDate() : getTimestampAsDate();
    }

    private Date getEventIdAsDate() {
        return eventId == null ? null : Date.from(EventMeta.eventId2Instant(eventId));
    }

    private Date getTimestampAsDate() {
        return timestamp == null ? null : Date.from(EventMeta.timestamp2Instant(timestamp));
    }

    @Override
    public String toString() {
        return SOSString.toString(this);
    }
}
