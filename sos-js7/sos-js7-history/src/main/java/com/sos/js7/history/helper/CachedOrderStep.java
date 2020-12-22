package com.sos.js7.history.helper;

import com.sos.commons.util.SOSString;
import com.sos.joc.db.history.DBItemHistoryOrderStep;
import java.util.Date;

public class CachedOrderStep {

    private final Long id;
    private final Long historyOrderMainParentId;
    private final Long historyOrderId;
    private final String orderId;
    private final String jobName;
    private final String agentTimezone;
    private final String agentId;
    private final String agentUri;
    private final String workflowPosition;
    private Integer returnCode;
    private Date endTime;
    private CachedOrderStepError error;
    private Boolean lastStdEndsWithNewLine;
    private StringBuilder stdError;
    private Date created;

    public CachedOrderStep(DBItemHistoryOrderStep item, String timezone) {
        id = item.getId();
        historyOrderMainParentId = item.getHistoryOrderMainParentId();
        historyOrderId = item.getHistoryOrderId();
        orderId = item.getOrderId();
        jobName = item.getJobName();
        agentTimezone = timezone;
        agentId = item.getAgentId();
        agentUri = item.getAgentUri();
        workflowPosition = item.getWorkflowPosition();
        returnCode = item.getReturnCode();
        endTime = item.getEndTime();
        created = new Date();
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

    public String getAgentUri() {
        return agentUri;
    }

    public String getWorkflowPosition() {
        return workflowPosition;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date val) {
        endTime = val;
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

    public Date getCreated() {
        return created;
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
