package com.sos.js7.history.helper;

import com.sos.joc.db.history.DBItemHistoryOrderStep;
import java.util.Date;

public class CachedOrderStep {

    private final Long id;
    private final Long mainOrderId;
    private final Long orderId;
    private final String orderKey;
    private final String jobName;
    private final String agentTimezone;
    private final String agentPath;
    private final String agentUri;
    private final String workflowPosition;
    private Long returnCode;
    private Date endTime;
    private CachedOrderStepError error;
    private Boolean lastStdEndsWithNewLine;
    private Date created;

    public CachedOrderStep(DBItemHistoryOrderStep item, String timezone) {
        id = item.getId();
        mainOrderId = item.getMainOrderId();
        orderId = item.getOrderId();
        orderKey = item.getOrderKey();
        jobName = item.getJobName();
        agentTimezone = timezone;
        agentPath = item.getAgentPath();
        agentUri = item.getAgentUri();
        workflowPosition = item.getWorkflowPosition();
        returnCode = item.getReturnCode();
        endTime = item.getEndTime();
        created = new Date();
    }

    public Long getId() {
        return id;
    }

    public Long getMainOrderId() {
        return mainOrderId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getOrderKey() {
        return orderKey;
    }

    public String getJobName() {
        return jobName;
    }

    public String getAgentTimezone() {
        return agentTimezone;
    }

    public String getAgentPath() {
        return agentPath;
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

    public void setReturnCode(Long val) {
        returnCode = val;
    }

    public Long getReturnCode() {
        return returnCode;
    }

    public void setLastStdEndsWithNewLine(boolean val) {
        lastStdEndsWithNewLine = Boolean.valueOf(val);
    }

    public Boolean isLastStdEndsWithNewLine() {
        return lastStdEndsWithNewLine;
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
