package com.sos.jobscheduler.db.history;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.sos.jobscheduler.db.DBLayer;

@Entity
@Table(name = DBLayer.HISTORY_TABLE_ORDER_STEPS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[CONSTRAINT_HASH]" }) })
@SequenceGenerator(name = DBLayer.HISTORY_TABLE_ORDER_STEPS_SEQUENCE, sequenceName = DBLayer.HISTORY_TABLE_ORDER_STEPS_SEQUENCE, allocationSize = 1)
public class DBItemOrderStep implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.HISTORY_TABLE_ORDER_STEPS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[MASTER_ID]", nullable = false)
    private String masterId;

    @Column(name = "[ORDER_KEY]", nullable = false)
    private String orderKey;// event TODO redundant?

    @Column(name = "[WORKFLOW_POSITION]", nullable = false)
    private String workflowPosition; // event

    @Column(name = "[RETRY_COUNTER]", nullable = false)
    private Long retryCounter; // run counter (if rerun)

    /** Foreign key - TABLE_SCHEDULER_ORDER_HISTORY.ID */
    @Column(name = "[MAIN_ORDER_ID]", nullable = false)
    private Long mainOrderId;// db

    @Column(name = "[ORDER_ID]", nullable = false)
    private Long orderId;// db

    /** Others */
    @Column(name = "[POSITION]", nullable = false)
    private Long position; // last position of the workflowPosition. e.g.: wp=1#fork_1#3. p=3

    @Column(name = "[WORKFLOW_PATH]", nullable = false)
    private String workflowPath;// event

    @Column(name = "[WORKFLOW_VERSION]", nullable = false)
    private String workflowVersion;// event

    @Column(name = "[JOB_NAME]", nullable = false)
    private String jobName;// event

    @Column(name = "[JOB_TITLE]", nullable = true)
    private String jobTitle;// event

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
    private Long returnCode;// event

    @Column(name = "[STATUS]", nullable = false)
    private String status;// event. planned: completed, stopped, skipped, setback ...

    @Column(name = "[ERROR]", nullable = false)
    private boolean error;// TODO

    @Column(name = "[ERROR_CODE]", nullable = true)
    private String errorCode;// TODO

    @Column(name = "[ERROR_TEXT]", nullable = true)
    private String errorText;

    @Column(name = "[CONSTRAINT_HASH]", nullable = false)
    private String constraintHash; // hash from masterId, startEventId for db unique constraint

    @Column(name = "[CREATED]", nullable = false)
    private Date created;

    @Column(name = "[MODIFIED]", nullable = false)
    private Date modified;

    public DBItemOrderStep() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public String getMasterId() {
        return masterId;
    }

    public void setMasterId(String val) {
        masterId = val;
    }

    public String getOrderKey() {
        return orderKey;
    }

    public void setOrderKey(String val) {
        orderKey = val;
    }

    public String getWorkflowPosition() {
        return workflowPosition;
    }

    public void setWorkflowPosition(String val) {
        workflowPosition = val;
    }

    public Long getRetryCounter() {
        return retryCounter;
    }

    public void setRetryCounter(Long val) {
        retryCounter = val;
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

    public Long getPosition() {
        return position;
    }

    public void setPosition(Long val) {
        position = val;
    }

    public String getWorkflowPath() {
        return workflowPath;
    }

    public void setWorkflowPath(String val) {
        workflowPath = val;
    }

    public String getWorkflowVersion() {
        return workflowVersion;
    }

    public void setWorkflowVersion(String val) {
        workflowVersion = val;
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

    public Long getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(Long val) {
        returnCode = val;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String val) {
        status = val;
    }

    public void setError(boolean val) {
        error = val;
    }

    public boolean getError() {
        return error;
    }

    public void setErrorCode(String val) {
        errorCode = val;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorText(String val) {
        errorText = val;
    }

    public String getErrorText() {
        return errorText;
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

    public boolean equals(Object o) {
        if (o == null || !(o instanceof DBItemOrderStep)) {
            return false;
        }
        DBItemOrderStep item = (DBItemOrderStep) o;
        if (!getId().equals(item.getId())) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return getId() == null ? new Long(0).hashCode() : getId().hashCode();
    }
}
