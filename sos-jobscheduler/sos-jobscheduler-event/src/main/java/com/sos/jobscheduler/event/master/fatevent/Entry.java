package com.sos.jobscheduler.event.master.fatevent;

import java.util.LinkedHashMap;

public class Entry {

    private Long eventId;
    private Long timestamp;
    private String key;
    private String type;
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

    public String getType() {
        return type;
    }

    public void setType(String val) {
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

}
