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

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;
import com.sos.joc.model.inventory.common.JobCriticality;

@Entity
@Table(name = DBLayer.TABLE_HISTORY_ORDER_STEPS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[CONSTRAINT_HASH]" }) })
@SequenceGenerator(name = DBLayer.TABLE_HISTORY_ORDER_STEPS_SEQUENCE, sequenceName = DBLayer.TABLE_HISTORY_ORDER_STEPS_SEQUENCE, allocationSize = 1)
public class DBItemHistoryOrderStep extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_HISTORY_ORDER_STEPS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[JOBSCHEDULER_ID]", nullable = false)
    private String jobSchedulerId;

    @Column(name = "[ORDER_KEY]", nullable = false)
    private String orderKey;// event TODO redundant?

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

    /** Foreign key - TABLE_SCHEDULER_ORDER_HISTORY.ID */
    @Column(name = "[MAIN_ORDER_ID]", nullable = false)
    private Long mainOrderId;// db

    @Column(name = "[ORDER_ID]", nullable = false)
    private Long orderId;// db

    @Column(name = "[POSITION]", nullable = false)
    private Integer position; // last position of the workflowPosition. e.g.: wp=1#fork_1#3. p=3

    @Column(name = "[RETRY_COUNTER]", nullable = false)
    private Integer retryCounter; // run counter (if rerun)

    @Column(name = "[JOB_NAME]", nullable = false)
    private String jobName;// event

    @Column(name = "[JOB_TITLE]", nullable = true)
    private String jobTitle;// event

    @Column(name = "[CRITICALITY]", nullable = false)
    private Integer criticality;

    @Column(name = "[AGENT_PATH]", nullable = false)
    private String agentPath;// event

    @Column(name = "[AGENT_URI]", nullable = false)
    private String agentUri;// event

    @Column(name = "[START_CAUSE]", nullable = false)
    private String startCause;// event

    @Column(name = "[START_TIME]", nullable = false)
    private Date startTime;// event

    @Column(name = "[START_EVENT_ID]", nullable = false)
    private String startEventId;// event <- started event id

    @Column(name = "[START_PARAMETERS]", nullable = true)
    private String startParameters;

    @Column(name = "[END_TIME]", nullable = true)
    private Date endTime;// event

    @Column(name = "[END_EVENT_ID]", nullable = true)
    private String endEventId;// event <- ended event id

    @Column(name = "[END_PARAMETERS]", nullable = true)
    private String endParameters;

    @Column(name = "[RETURN_CODE]", nullable = false)
    private Integer returnCode;// event

    @Column(name = "[STATE]", nullable = false)
    private String state;// event. planned: completed, stopped, skipped, setback ...

    @Column(name = "[ERROR]", nullable = false)
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
    private String constraintHash; // hash from jobSchedulerId, startEventId for db unique constraint

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

    public String getJobSchedulerId() {
        return jobSchedulerId;
    }

    public void setJobSchedulerId(String val) {
        jobSchedulerId = val;
    }

    public String getOrderKey() {
        return orderKey;
    }

    public void setOrderKey(String val) {
        orderKey = val;
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
        workflowPosition = val;
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

    public Long getMainOrderId() {
        return mainOrderId;
    }

    public void setMainOrderId(Long val) {
        mainOrderId = val;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long val) {
        orderId = val;
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

    @Transient
    public void setCriticality(JobCriticality val) {
        setCriticality(val == null ? JobCriticality.NORMAL.intValue() : val.intValue());
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

    public void setStartEventId(String val) {
        startEventId = val;
    }

    public String getStartEventId() {
        return startEventId;
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

    public void setEndEventId(String val) {
        endEventId = val;
    }

    public String getEndEventId() {
        return endEventId;
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

    public String getState() {
        return state;
    }

    public void setState(String val) {
        state = val;
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
        if (val != null && val.length() > 50) {
            val = val.substring(0, 50);
        }
        errorCode = val;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorText(String val) {
        if (val != null && val.length() > 255) {
            val = val.substring(0, 255);
        }
        errorText = val;
    }

    public String getErrorText() {
        return errorText;
    }

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long val) {
        if (val == null) {
            val = new Long(0);
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
    public boolean isSuccessFul() {
        return endTime != null && !error;
    }

    @Transient
    public boolean isInComplete() {
        return startTime != null && endTime == null;
    }

    @Transient
    public boolean isFailed() {
        return endTime != null && error;
    }
}
