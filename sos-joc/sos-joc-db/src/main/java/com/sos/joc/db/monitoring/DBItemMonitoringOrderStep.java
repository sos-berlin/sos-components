package com.sos.joc.db.monitoring;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

import com.sos.history.JobWarning;
import com.sos.inventory.model.job.JobCriticality;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_MONITORING_ORDER_STEPS)
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

    @Column(name = "[JOB_NAME]", nullable = false)
    private String jobName;

    @Column(name = "[JOB_LABEL]", nullable = false)
    private String jobLabel;

    @Column(name = "[JOB_TITLE]", nullable = true)
    private String jobTitle;

    @Column(name = "[CRITICALITY]", nullable = false)
    private Integer criticality;

    @Column(name = "[AGENT_ID]", nullable = false)
    private String agentId;

    @Column(name = "[AGENT_URI]", nullable = false)
    private String agentUri;

    @Column(name = "[START_CAUSE]", nullable = false)
    private String startCause;

    @Column(name = "[START_TIME]", nullable = false)
    private Date startTime;

    @Column(name = "[START_PARAMETERS]", nullable = true)
    private String startParameters;

    @Column(name = "[END_TIME]", nullable = true)
    private Date endTime;

    @Column(name = "[END_PARAMETERS]", nullable = true)
    private String endParameters;

    @Column(name = "[RETURN_CODE]", nullable = true)
    private Integer returnCode;

    @Column(name = "[SEVERITY]", nullable = false)
    private Integer severity;

    @Column(name = "[ERROR]", nullable = false)
    @Type(type = "numeric_boolean")
    private boolean error;

    @Column(name = "[ERROR_STATE]", nullable = true)
    private String errorState;

    @Column(name = "[ERROR_REASON]", nullable = true)
    private String errorReason;

    @Column(name = "[ERROR_CODE]", nullable = true)
    private String errorCode;

    @Column(name = "[ERROR_TEXT]", nullable = true)
    private String errorText;

    @Column(name = "[WARN]", nullable = false)
    private Integer warn;

    @Column(name = "[WARN_TEXT]", nullable = true)
    private String warnText;

    /** Foreign key - TABLE_HISTORY_LOGS.ID, KEY */
    @Column(name = "[LOG_ID]", nullable = true)
    private Long logId;// db

    @Column(name = "[CREATED]", nullable = false)
    private Date created;

    @Column(name = "[MODIFIED]", nullable = false)
    private Date modified;

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
        workflowPosition = val;
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

    public Integer getCriticality() {
        return criticality;
    }

    @Transient
    public JobCriticality getCriticalityAsEnum() {
        try {
            return JobCriticality.fromValue(criticality);
        } catch (IllegalArgumentException e) {
            return JobCriticality.NORMAL;
        }
    }

    public void setCriticality(Integer val) {
        criticality = val;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String val) {
        agentId = val;
    }

    public String getAgentUri() {
        return agentUri;
    }

    public void setAgentUri(String val) {
        agentUri = val;
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

    public String getStartParameters() {
        return startParameters;
    }

    public void setStartParameters(String val) {
        startParameters = val;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date val) {
        endTime = val;
    }

    public String getEndParameters() {
        return endParameters;
    }

    public void setEndParameters(String val) {
        endParameters = val;
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

    @Transient
    public static String normalizeErrorCode(String val) {
        return normalizeValue(val, 50);
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorText(String val) {
        errorText = normalizeErrorText(val);
    }

    @Transient
    public static String normalizeErrorText(String val) {
        return normalizeValue(val, 500);
    }

    public String getErrorText() {
        return errorText;
    }

    public void setWarn(Integer val) {
        if (val == null) {
            val = JobWarning.NONE.intValue();
        }
        warn = val;
    }

    @Transient
    public void setWarn(JobWarning val) {
        setWarn(val == null ? null : val.intValue());
    }

    public Integer getWarn() {
        return warn;
    }

    @Transient
    public JobWarning getWarnAsEnum() {
        try {
            return JobWarning.fromValue(warn);
        } catch (IllegalArgumentException e) {
            return JobWarning.NONE;
        }
    }

    public void setWarnText(String val) {
        warnText = normalizeWarnText(val);
    }

    @Transient
    public static String normalizeWarnText(String val) {
        return normalizeValue(val, 500);
    }

    public String getWarnText() {
        return warnText;
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
}
