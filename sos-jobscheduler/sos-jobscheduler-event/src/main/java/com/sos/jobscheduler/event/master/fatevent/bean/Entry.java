package com.sos.jobscheduler.event.master.fatevent.bean;

import java.time.Instant;
import java.util.LinkedHashMap;

import com.sos.commons.util.SOSString;
import com.sos.jobscheduler.event.master.EventMeta;
import com.sos.jobscheduler.event.master.bean.IEntry;
import com.sos.jobscheduler.event.master.fatevent.EventMeta.EventType;

public class Entry implements IEntry {

    private Long eventId;
    private Long timestamp;
    private String key;
    private EventType type;
    private String parent;
    private String cause;
    private WorkflowPosition workflowPosition;
    private Long scheduledAt;
    private Outcome outcome;
    private LinkedHashMap<String, String> variables;
    private String agentUri;
    private String jobPath;
    private String chunk;

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long val) {
        eventId = val;
    }

    public Instant getEventIdAsInstant() {
        return eventId == null ? null : EventMeta.eventId2Instant(eventId);
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public Instant getTimestampAsInstant() {
        return timestamp == null ? null : EventMeta.timestamp2Instant(timestamp);
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

    public String getParent() {
        return parent;
    }

    public void setParent(String val) {
        parent = val;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String val) {
        cause = val;
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

    public void setScheduledAt(Long val) {
        scheduledAt = val;
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

    @Override
    public String toString() {
        return SOSString.toString(this);
    }
}
