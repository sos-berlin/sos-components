package com.sos.js7.event.controller.fatevent.bean;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import com.sos.commons.util.SOSString;
import com.sos.js7.event.controller.EventMeta;
import com.sos.js7.event.controller.bean.IEntry;
import com.sos.js7.event.controller.fatevent.FatEventMeta.FatEventType;

public class Entry implements IEntry {

    private Long eventId;
    private Long timestamp;
    private String key;
    private FatEventType type;
    private WorkflowPosition workflowPosition;
    private Long scheduledFor;
    private Outcome outcome;
    // order or fork (branch) incoming variables
    private LinkedHashMap<String, String> arguments;
    // job incoming / outcoming variables
    private LinkedHashMap<String, String> keyValues;
    private String agentRefPath;
    private String agentUri;
    private String jobName;
    private String chunk;
    private String controllerId;
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

    public FatEventType getType() {
        return type;
    }

    public void setType(FatEventType val) {
        type = val;
    }

    public WorkflowPosition getWorkflowPosition() {
        return workflowPosition;
    }

    public void setWorkflowPosition(WorkflowPosition val) {
        workflowPosition = val;
    }

    public Long getScheduledFor() {
        return scheduledFor;
    }

    public Date getScheduledForAsDate() {
        return scheduledFor == null ? null : Date.from(EventMeta.timestamp2Instant(scheduledFor));
    }

    public void setScheduledFor(Long val) {
        scheduledFor = val;
    }

    public String getAgentRefPath() {
        return agentRefPath;
    }

    public void setAgentRefPath(String val) {
        agentRefPath = val;
    }

    public String getAgentUri() {
        return agentUri;
    }

    public void setAgentUri(String val) {
        agentUri = val;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String val) {
        jobName = val;
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

    public LinkedHashMap<String, String> getArguments() {
        return arguments;
    }

    public void setArguments(LinkedHashMap<String, String> val) {
        arguments = val;
    }

    public LinkedHashMap<String, String> getKeyValues() {
        return keyValues;
    }

    public void setKeyValues(LinkedHashMap<String, String> val) {
        keyValues = val;
    }

    public String getContollerId() {
        return controllerId;
    }

    public void setContollerId(String val) {
        controllerId = val;
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

    public Date getEventIdAsDate() {
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
