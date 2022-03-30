package com.sos.joc.history.helper;

import java.util.Date;

import com.sos.commons.util.SOSString;
import com.sos.controller.model.event.EventType;
import com.sos.joc.classes.history.HistoryMapper;
import com.sos.joc.cluster.bean.history.HistoryOrderStepBean;
import com.sos.joc.db.history.DBItemHistoryOrderStep;

public class CachedOrderStep {

    private final Long id;
    private final Long historyOrderMainParentId;
    private final Long historyOrderId;
    private final String orderId;
    private final String jobName;
    private final String agentTimezone;
    private final String agentId;
    private final String agentName;
    private final String agentUri;
    private final String subagentClusterId;
    private final String workflowPosition;
    private Integer severity;
    private Integer returnCode;
    private Date startTime;
    private Date endTime;
    private CachedOrderStepError error;
    private Boolean lastStdEndsWithNewLine;
    private StringBuilder stdError;

    public CachedOrderStep(DBItemHistoryOrderStep item, String timezone) {
        this.id = item.getId();
        this.historyOrderMainParentId = item.getHistoryOrderMainParentId();
        this.historyOrderId = item.getHistoryOrderId();
        this.orderId = item.getOrderId();
        this.jobName = item.getJobName();
        this.agentTimezone = timezone;
        this.agentId = item.getAgentId();
        this.agentName = item.getAgentName();
        this.agentUri = item.getAgentUri();
        this.subagentClusterId = item.getSubagentClusterId();
        this.workflowPosition = item.getWorkflowPosition();
        this.severity = item.getSeverity();
        this.returnCode = item.getReturnCode();
        this.startTime = item.getStartTime();
        this.endTime = item.getEndTime();
    }

    public HistoryOrderStepBean convert(EventType eventType, Long eventId, String controllerId, String workflowPath) {
        HistoryOrderStepBean b = new HistoryOrderStepBean(eventType, eventId, controllerId, id);
        b.setHistoryOrderMainParentId(historyOrderMainParentId);
        b.setHistoryOrderId(historyOrderId);
        b.setOrderId(orderId);
        b.setJobName(jobName);
        b.setAgentId(agentId);
        b.setAgentName(agentName);
        b.setAgentUri(agentUri);
        b.setSubagentClusterId(subagentClusterId);
        b.setWorkflowPosition(workflowPosition);
        b.setWorkflowPath(workflowPath);
        b.setSeverity(severity);
        b.setReturnCode(returnCode);
        b.setStartTime(startTime);
        b.setEndTime(endTime);
        return b;
    }

    public Long getId() {
        return id;
    }

    public Long getHistoryOrderMainParentId() {
        return historyOrderMainParentId;
    }

    public Long getHistoryOrderId() {
        return historyOrderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getJobName() {
        return jobName;
    }

    public String getAgentTimezone() {
        return agentTimezone;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getAgentName() {
        return agentName;
    }

    public String getAgentUri() {
        return agentUri;
    }

    public String getSubagentClusterId() {
        return subagentClusterId;
    }

    public String getWorkflowPosition() {
        return workflowPosition;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date val) {
        endTime = val;
    }

    public Integer getSeverity() {
        return severity;
    }

    public void setSeverity(Integer val) {
        severity = val;
    }

    public String getSeverityAsText() {
        try {
            return severity == null ? null : HistoryMapper.getState(severity).get_text().name();
        } catch (Throwable e) {
            return null;
        }
    }

    public void setReturnCode(Integer val) {
        returnCode = val;
    }

    public Integer getReturnCode() {
        return returnCode;
    }

    public void setLastStdEndsWithNewLine(boolean val) {
        lastStdEndsWithNewLine = Boolean.valueOf(val);
    }

    public Boolean isLastStdEndsWithNewLine() {
        return lastStdEndsWithNewLine;
    }

    public void setStdError(String val) {
        if (!SOSString.isEmpty(val)) {
            if (stdError == null) {
                stdError = new StringBuilder();
            }
            stdError.append(val);
        }
    }

    public String getStdErr() {
        return stdError == null ? null : stdError.toString().trim();
    }

    public CachedOrderStepError getError() {
        return error;
    }

    public void setError(String errorState, String errorReason, String errorCode, String errorText) {
        error = new CachedOrderStepError(errorState, errorReason, errorCode, errorText);
    }

    public class CachedOrderStepError {

        private final String state;
        private final String reason;
        private final String code;
        private final String text;

        public CachedOrderStepError(String errorState, String errorReason, String errorCode, String errorText) {
            state = errorState;
            reason = errorReason;
            code = errorCode;
            text = errorText;
        }

        public String getState() {
            return state;
        }

        public String getReason() {
            return reason;
        }

        public String getCode() {
            return code;
        }

        public String getText() {
            return text;
        }
    }
}
