package com.sos.joc.db.monitoring;

import java.util.Date;

import org.hibernate.annotations.Proxy;
import org.hibernate.type.NumericBooleanConverter;

import com.sos.inventory.model.job.JobCriticality;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.common.HistoryConstants;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@SuppressWarnings("deprecation")
@Entity
@Table(name = DBLayer.TABLE_MON_ORDER_STEPS)
@Proxy(lazy = false)
public class DBItemMonitoringOrderStep extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[HISTORY_ID]", nullable = false)
    private Long historyId;

    @Column(name = "[WORKFLOW_POSITION]", nullable = false)
    private String workflowPosition;

    /** Foreign key - TABLE_HISTORY_ORDERS.ID */
    @Column(name = "[HO_MAIN_PARENT_ID]", nullable = false)
    private Long historyOrderMainParentId;

    @Column(name = "[HO_ID]", nullable = false)
    private Long historyOrderId;

    @Column(name = "[POSITION]", nullable = false)
    private Integer position; // last position of the workflowPosition. e.g.: wp=1#fork_1#3. p=3

    // prefix JOB_ - because notification sets the variables based on the column names
    @Column(name = "[JOB_NAME]", nullable = false)
    private String jobName;

    @Column(name = "[JOB_LABEL]", nullable = false)
    private String jobLabel;

    @Column(name = "[JOB_TITLE]", nullable = true)
    private String jobTitle;

    @Column(name = "[JOB_NOTIFICATION]", nullable = true)
    private String jobNotification;

    @Column(name = "[JOB_CRITICALITY]", nullable = false)
    private Integer jobCriticality;

    @Column(name = "[AGENT_ID]", nullable = false)
    private String agentId;

    @Column(name = "[AGENT_NAME]", nullable = true)
    private String agentName;

    @Column(name = "[AGENT_URI]", nullable = false)
    private String agentUri;

    @Column(name = "[SUBAGENT_CLUSTER_ID]", nullable = true)
    private String subagentClusterId;

    @Column(name = "[START_CAUSE]", nullable = false)
    private String startCause;

    @Column(name = "[START_TIME]", nullable = false)
    private Date startTime;

    @Column(name = "[START_VARIABLES]", nullable = true)
    private String startVariables;

    @Column(name = "[END_TIME]", nullable = true)
    private Date endTime;

    @Column(name = "[END_VARIABLES]", nullable = true)
    private String endVariables;

    @Column(name = "[RETURN_CODE]", nullable = true)
    private Integer returnCode;

    @Column(name = "[SEVERITY]", nullable = false)
    private Integer severity;

    @Column(name = "[ERROR]", nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
    private boolean error;

    @Column(name = "[ERROR_STATE]", nullable = true)
    private String errorState;

    @Column(name = "[ERROR_REASON]", nullable = true)
    private String errorReason;

    @Column(name = "[ERROR_CODE]", nullable = true)
    private String errorCode;

    @Column(name = "[ERROR_TEXT]", nullable = true)
    private String errorText;

    /** Foreign key - TABLE_HISTORY_LOGS.ID, KEY */
    @Column(name = "[LOG_ID]", nullable = true)
    private Long logId;// db

    @Column(name = "[CREATED]", nullable = false)
    private Date created;

    @Column(name = "[MODIFIED]", nullable = false)
    private Date modified;

    @Transient
    private String tags;

    public DBItemMonitoringOrderStep() {
    }

    public Long getHistoryId() {
        return historyId;
    }

    public void setHistoryId(Long val) {
        historyId = val;
    }

    public String getWorkflowPosition() {
        return workflowPosition;
    }

    public void setWorkflowPosition(String val) {
        workflowPosition = normalizeWorkflowPosition(val);
    }

    public Long getHistoryOrderMainParentId() {
        return historyOrderMainParentId;
    }

    public void setHistoryOrderMainParentId(Long val) {
        historyOrderMainParentId = val;
    }

    public Long getHistoryOrderId() {
        return historyOrderId;
    }

    public void setHistoryOrderId(Long val) {
        historyOrderId = val;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer val) {
        position = val;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String val) {
        if (val == null) {
            val = DBLayer.DEFAULT_KEY;
        }
        jobName = val;
    }

    public String getJobLabel() {
        return jobLabel;
    }

    public void setJobLabel(String val) {
        if (val == null) {
            val = DBLayer.DEFAULT_KEY;
        }
        jobLabel = val;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String val) {
        jobTitle = val;
    }

    public String getJobNotification() {
        return jobNotification;
    }

    public void setJobNotification(String val) {
        jobNotification = normalizeValue(val, HistoryConstants.MAX_LEN_NOTIFICATION);
    }

    public Integer getJobCriticality() {
        return jobCriticality;
    }

    public void setJobCriticality(Integer val) {
        jobCriticality = val;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String val) {
        agentId = val;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String val) {
        agentName = val;
    }

    public String getAgentUri() {
        return agentUri;
    }

    public void setAgentUri(String val) {
        agentUri = val;
    }

    public String getSubagentClusterId() {
        return subagentClusterId;
    }

    public void setSubagentClusterId(String val) {
        subagentClusterId = val;
    }

    public String getStartCause() {
        return startCause;
    }

    public void setStartCause(String val) {
        startCause = val;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date val) {
        startTime = val;
    }

    public String getStartVariables() {
        return startVariables;
    }

    public void setStartVariables(String val) {
        startVariables = val;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date val) {
        endTime = val;
    }

    public String getEndVariables() {
        return endVariables;
    }

    public void setEndVariables(String val) {
        endVariables = val;
    }

    public Integer getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(Integer val) {
        returnCode = val;
    }

    public Integer getSeverity() {
        return severity;
    }

    public void setSeverity(Integer val) {
        severity = val;
    }

    public void setError(boolean val) {
        error = val;
    }

    public boolean getError() {
        return error;
    }

    public void setErrorState(String val) {
        errorState = val;
    }

    public String getErrorState() {
        return errorState;
    }

    public void setErrorReason(String val) {
        errorReason = val;
    }

    public String getErrorReason() {
        return errorReason;
    }

    public void setErrorCode(String val) {
        errorCode = normalizeErrorCode(val);
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorText(String val) {
        errorText = normalizeErrorText(val);
    }

    public String getErrorText() {
        return errorText;
    }

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long val) {
        logId = val;
    }

    public void setCreated(Date val) {
        created = val;
    }

    public Date getCreated() {
        return created;
    }

    public void setModified(Date val) {
        modified = val;
    }

    public Date getModified() {
        return modified;
    }

    @Transient
    public static String normalizeErrorCode(String val) {
        return normalizeValue(val, HistoryConstants.MAX_LEN_ERROR_CODE);
    }

    @Transient
    public static String normalizeErrorText(String val) {
        return normalizeValue(val, HistoryConstants.MAX_LEN_ERROR_TEXT);
    }

    @Transient
    public static String normalizeWorkflowPosition(String val) {
        return normalizeValue(val, HistoryConstants.MAX_LEN_WORKFLOW_POSITION);
    }

    @Transient
    public JobCriticality getJobCriticalityAsEnum() {
        try {
            return JobCriticality.fromValue(jobCriticality);
        } catch (IllegalArgumentException e) {
            return JobCriticality.NORMAL;
        }
    }

    @Transient
    public String getTags() {
        return tags;
    }

    @Transient
    public void setTags(String val) {
        tags = val;
    }
}
