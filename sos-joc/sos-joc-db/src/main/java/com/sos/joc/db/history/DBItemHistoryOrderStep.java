package com.sos.joc.db.history;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Type;

import com.sos.inventory.model.job.JobCriticality;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.common.HistoryConstants;
import com.sos.joc.db.history.common.HistorySeverity;
import com.sos.joc.model.order.OrderStateText;

@Entity
@Table(name = DBLayer.TABLE_HISTORY_ORDER_STEPS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[CONSTRAINT_HASH]" }) })
@SequenceGenerator(name = DBLayer.TABLE_HISTORY_ORDER_STEPS_SEQUENCE, sequenceName = DBLayer.TABLE_HISTORY_ORDER_STEPS_SEQUENCE, allocationSize = 1)
public class DBItemHistoryOrderStep extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_HISTORY_ORDER_STEPS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[CONTROLLER_ID]", nullable = false)
    private String controllerId;

    @Column(name = "[ORDER_ID]", nullable = false)
    private String orderId;// event TODO redundant?

    @Column(name = "[WORKFLOW_PATH]", nullable = false)
    private String workflowPath;// event

    @Column(name = "[WORKFLOW_VERSION_ID]", nullable = false)
    private String workflowVersionId; // event

    @Column(name = "[WORKFLOW_POSITION]", nullable = false)
    private String workflowPosition; // event

    @Column(name = "[WORKFLOW_FOLDER]", nullable = false)
    private String workflowFolder;// extracted from workflowPath

    @Column(name = "[WORKFLOW_NAME]", nullable = false)
    private String workflowName;// extracted from workflowPath

    /** Foreign key - TABLE_HISTORY_ORDERS.ID */
    @Column(name = "[HO_MAIN_PARENT_ID]", nullable = false)
    private Long historyOrderMainParentId;// db

    @Column(name = "[HO_ID]", nullable = false)
    private Long historyOrderId;// db

    @Column(name = "[POSITION]", nullable = false)
    private Integer position; // last position of the workflowPosition. e.g.: wp=1#fork_1#3. p=3

    @Column(name = "[RETRY_COUNTER]", nullable = false)
    private Integer retryCounter; // run counter (if rerun)

    @Column(name = "[JOB_NAME]", nullable = false)
    private String jobName;// event

    @Column(name = "[JOB_LABEL]", nullable = false)
    private String jobLabel;

    @Column(name = "[JOB_TITLE]", nullable = true)
    private String jobTitle;// event

    @Column(name = "[CRITICALITY]", nullable = false)
    private Integer criticality;

    @Column(name = "[AGENT_ID]", nullable = false)
    private String agentId;// event

    @Column(name = "[AGENT_URI]", nullable = false)
    private String agentUri;// event

    @Column(name = "[START_CAUSE]", nullable = false)
    private String startCause;// event

    @Column(name = "[START_TIME]", nullable = false)
    private Date startTime;// event

    @Column(name = "[START_EVENT_ID]", nullable = false)
    private Long startEventId;// event <- started event id

    @Column(name = "[START_VARIABLES]", nullable = true)
    private String startVariables;

    @Column(name = "[END_TIME]", nullable = true)
    private Date endTime;// event

    @Column(name = "[END_EVENT_ID]", nullable = true)
    private Long endEventId;// event <- ended event id

    @Column(name = "[END_VARIABLES]", nullable = true)
    private String endVariables;

    @Column(name = "[RETURN_CODE]", nullable = true)
    private Integer returnCode;// event

    @Column(name = "[SEVERITY]", nullable = false)
    private Integer severity;

    @Column(name = "[ERROR]", nullable = false)
    @Type(type = "numeric_boolean")
    private boolean error;

    @Column(name = "[ERROR_STATE]", nullable = true)
    private String errorState;// event. outcome type

    @Column(name = "[ERROR_REASON]", nullable = true)
    private String errorReason;// event. outcome reason type

    @Column(name = "[ERROR_CODE]", nullable = true)
    private String errorCode;// TODO

    @Column(name = "[ERROR_TEXT]", nullable = true)
    private String errorText;

    /** Foreign key - TABLE_HISTORY_LOGS.ID, KEY */
    @Column(name = "[LOG_ID]", nullable = false)
    private Long logId;// db

    @Column(name = "[CONSTRAINT_HASH]", nullable = false)
    private String constraintHash; // hash from controllerId, startEventId for db unique constraint

    @Column(name = "[CREATED]", nullable = false)
    private Date created;

    @Column(name = "[MODIFIED]", nullable = false)
    private Date modified;

    public DBItemHistoryOrderStep() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public String getControllerId() {
        return controllerId;
    }

    public void setControllerId(String val) {
        controllerId = val;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String val) {
        orderId = val;
    }

    public String getWorkflowPath() {
        return workflowPath;
    }

    public void setWorkflowPath(String val) {
        workflowPath = val;
    }

    public String getWorkflowVersionId() {
        return workflowVersionId;
    }

    public void setWorkflowVersionId(String val) {
        workflowVersionId = val;
    }

    public String getWorkflowPosition() {
        return workflowPosition;
    }

    public void setWorkflowPosition(String val) {
        workflowPosition = normalizeWorkflowPosition(val);
    }

    public String getWorkflowFolder() {
        return workflowFolder;
    }

    public void setWorkflowFolder(String val) {
        workflowFolder = val;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String val) {
        workflowName = val;
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

    public Integer getRetryCounter() {
        return retryCounter;
    }

    public void setRetryCounter(Integer val) {
        retryCounter = val;
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

    public void setStartEventId(Long val) {
        startEventId = val;
    }

    public Long getStartEventId() {
        return startEventId;
    }

    public String getStartVariables() {
        return startVariables;
    }

    public void setStartVariables(String val) {
        startVariables = normalizeValue(val, HistoryConstants.MAX_LEN_START_VARIABLES);
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date val) {
        endTime = val;
    }

    public void setEndEventId(Long val) {
        endEventId = val;
    }

    public Long getEndEventId() {
        return endEventId;
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
        if (val == null) {
            val = Long.valueOf(0);
        }
        logId = val;
    }

    public String getConstraintHash() {
        return constraintHash;
    }

    public void setConstraintHash(String val) {
        constraintHash = val;
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
    public JobCriticality getCriticalityAsEnum() {
        try {
            return JobCriticality.fromValue(criticality);
        } catch (IllegalArgumentException e) {
            return JobCriticality.NORMAL;
        }
    }

    @Transient
    public void setCriticality(JobCriticality val) {
        setCriticality(val == null ? JobCriticality.NORMAL.intValue() : val.intValue());
    }

    @Transient
    public void setSeverity(OrderStateText val) {
        setSeverity(HistorySeverity.map2DbSeverity(val));
    }
}
