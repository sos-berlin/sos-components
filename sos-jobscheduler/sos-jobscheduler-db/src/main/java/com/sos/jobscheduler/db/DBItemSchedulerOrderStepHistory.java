package com.sos.jobscheduler.db;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

@Entity
@Table(name = DBLayer.TABLE_SCHEDULER_ORDER_STEP_HISTORY)
@SequenceGenerator(name = DBLayer.TABLE_SCHEDULER_ORDER_STEP_HISTORY_SEQUENCE, sequenceName = DBLayer.TABLE_SCHEDULER_ORDER_STEP_HISTORY_SEQUENCE, allocationSize = 1)
public class DBItemSchedulerOrderStepHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Primary key */
    private Long id;// db

    private String schedulerId;
    private String orderKey;// event TODO redundant?
    private String workflowPosition; // event
    private Long retryCounter; // run counter (if rerun)
    /** Foreign key - TABLE_SCHEDULER_ORDER_HISTORY.ID */
    private Long mainOrderHistoryId;// db
    private Long orderHistoryId;// db
    /** Others */
    private Long position; // last position of the workflowPosition. e.g.: wp=1#fork_1#3. p=3
    private String workflowPath;// event
    private String workflowVersion;// event
    private String jobPath;// event
    private String jobFolder;// event
    private String jobName;// event
    private String jobTitle;// event
    private String agentUri;// event
    private String startCause;// event
    private Date startTime;// event
    private String startEventId;// event <- started event id
    private String startParameters;
    private Date endTime;// event
    private String endEventId;// event <- ended event id
    private String endParameters;
    private Long returnCode;// event
    private String state;// event. planned: completed, stopped, skipped, setback ...
    private boolean error;// TODO
    private String errorCode;// TODO
    private String errorText;
    private String constraintHash; // hash from schedulerId,orderKey,startEventId for db unique constraint

    private Date created;
    private Date modified;

    public DBItemSchedulerOrderStepHistory() {
    }

    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_SCHEDULER_ORDER_STEP_HISTORY_SEQUENCE)
    @Column(name = "`ID`", nullable = false)
    public Long getId() {
        return id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_SCHEDULER_ORDER_STEP_HISTORY_SEQUENCE)
    @Column(name = "`ID`", nullable = false)
    public void setId(Long val) {
        id = val;
    }

    @Column(name = "`SCHEDULER_ID`", nullable = false)
    public String getSchedulerId() {
        return schedulerId;
    }

    @Column(name = "`SCHEDULER_ID`", nullable = false)
    public void setSchedulerId(String val) {
        schedulerId = val;
    }

    @Column(name = "`ORDER_KEY`", nullable = false)
    public String getOrderKey() {
        return orderKey;
    }

    @Column(name = "`ORDER_KEY`", nullable = false)
    public void setOrderKey(String val) {
        orderKey = val;
    }

    @Column(name = "`WORKFLOW_POSITION`", nullable = false)
    public String getWorkflowPosition() {
        return workflowPosition;
    }

    @Column(name = "`WORKFLOW_POSITION`", nullable = false)
    public void setWorkflowPosition(String val) {
        workflowPosition = val;
    }

    @Column(name = "`RETRY_COUNTER`", nullable = false)
    public Long getRetryCounter() {
        return retryCounter;
    }

    @Column(name = "`RETRY_COUNTER`", nullable = false)
    public void setRetryCounter(Long val) {
        retryCounter = val;
    }

    /** Foreign key */
    @Column(name = "`MAIN_ORDER_HISTORY_ID`", nullable = false)
    public Long getMainOrderHistoryId() {
        return mainOrderHistoryId;
    }

    @Column(name = "`MAIN_ORDER_HISTORY_ID`", nullable = false)
    public void setMainOrderHistoryId(Long val) {
        mainOrderHistoryId = val;
    }

    @Column(name = "`ORDER_HISTORY_ID`", nullable = false)
    public Long getOrderHistoryId() {
        return orderHistoryId;
    }

    @Column(name = "`ORDER_HISTORY_ID`", nullable = false)
    public void setOrderHistoryId(Long val) {
        orderHistoryId = val;
    }

    /** Others */
    @Column(name = "`POSITION`", nullable = false)
    public Long getPosition() {
        return position;
    }

    @Column(name = "`POSITION`", nullable = false)
    public void setPosition(Long val) {
        position = val;
    }

    @Column(name = "`WORKFLOW_PATH`", nullable = false)
    public String getWorkflowPath() {
        return workflowPath;
    }

    @Column(name = "`WORKFLOW_PATH`", nullable = false)
    public void setWorkflowPath(String val) {
        workflowPath = val;
    }

    @Column(name = "`WORKFLOW_VERSION`", nullable = false)
    public String getWorkflowVersion() {
        return workflowVersion;
    }

    @Column(name = "`WORKFLOW_VERSION`", nullable = false)
    public void setWorkflowVersion(String val) {
        workflowVersion = val;
    }

    @Column(name = "`JOB_PATH`", nullable = false)
    public String getJobPath() {
        return jobPath;
    }

    @Column(name = "`JOB_PATH`", nullable = false)
    public void setJobPath(String val) {
        jobPath = val;
    }

    @Column(name = "`JOB_FOLDER`", nullable = false)
    public String getJobFolder() {
        return jobFolder;
    }

    @Column(name = "`JOB_FOLDER`", nullable = false)
    public void setJobFolder(String val) {
        jobFolder = val;
    }

    @Column(name = "`JOB_NAME`", nullable = false)
    public String getJobName() {
        return jobName;
    }

    @Column(name = "`JOB_NAME`", nullable = false)
    public void setJobName(String val) {
        jobName = val;
    }

    @Column(name = "`JOB_TITLE`", nullable = true)
    public String getJobTitle() {
        return jobTitle;
    }

    @Column(name = "`JOB_TITLE`", nullable = true)
    public void setJobTitle(String val) {
        jobTitle = val;
    }

    @Column(name = "`AGENT_URI`", nullable = false)
    public String getAgentUri() {
        return agentUri;
    }

    @Column(name = "`AGENT_URI`", nullable = false)
    public void setAgentUri(String val) {
        agentUri = val;
    }

    @Column(name = "`START_CAUSE`", nullable = false)
    public String getStartCause() {
        return startCause;
    }

    @Column(name = "`START_CAUSE`", nullable = false)
    public void setStartCause(String val) {
        startCause = val;
    }

    @Column(name = "`START_TIME`", nullable = false)
    public Date getStartTime() {
        return startTime;
    }

    @Column(name = "`START_TIME`", nullable = false)
    public void setStartTime(Date val) {
        startTime = val;
    }

    @Column(name = "`START_EVENT_ID`", nullable = false)
    public void setStartEventId(String val) {
        startEventId = val;
    }

    @Column(name = "`START_EVENT_ID`", nullable = false)
    public String getStartEventId() {
        return startEventId;
    }

    @Column(name = "`START_PARAMETERS`", nullable = true)
    public String getStartParameters() {
        return startParameters;
    }

    @Column(name = "`START_PARAMETERS`", nullable = true)
    public void setStartParameters(String val) {
        startParameters = val;
    }

    @Column(name = "`END_TIME`", nullable = true)
    public Date getEndTime() {
        return endTime;
    }

    @Column(name = "`END_TIME`", nullable = true)
    public void setEndTime(Date val) {
        endTime = val;
    }

    @Column(name = "`END_EVENT_ID`", nullable = true)
    public void setEndEventId(String val) {
        endEventId = val;
    }

    @Column(name = "`END_EVENT_ID`", nullable = true)
    public String getEndEventId() {
        return endEventId;
    }

    @Column(name = "`END_PARAMETERS`", nullable = true)
    public String getEndParameters() {
        return endParameters;
    }

    @Column(name = "`END_PARAMETERS`", nullable = true)
    public void setEndParameters(String val) {
        endParameters = val;
    }

    @Column(name = "`RETURN_CODE`", nullable = false)
    public Long getReturnCode() {
        return returnCode;
    }

    @Column(name = "`RETURN_CODE`", nullable = false)
    public void setReturnCode(Long val) {
        returnCode = val;
    }

    @Column(name = "`STATE`", nullable = false)
    public String getState() {
        return state;
    }

    @Column(name = "`STATE`", nullable = false)
    public void setState(String val) {
        state = val;
    }

    @Column(name = "`ERROR`", nullable = false)
    @Type(type = "numeric_boolean")
    public void setError(boolean val) {
        error = val;
    }

    @Column(name = "`ERROR`", nullable = false)
    @Type(type = "numeric_boolean")
    public boolean getError() {
        return error;
    }

    @Column(name = "`ERROR_CODE`", nullable = true)
    public void setErrorCode(String val) {
        errorCode = val;
    }

    @Column(name = "`ERROR_CODE`", nullable = true)
    public String getErrorCode() {
        return errorCode;
    }

    @Column(name = "`ERROR_TEXT`", nullable = true)
    public void setErrorText(String val) {
        errorText = val;
    }

    @Column(name = "`ERROR_TEXT`", nullable = true)
    public String getErrorText() {
        return errorText;
    }

    @Column(name = "`CONSTRAINT_HASH`", nullable = false)
    public String getConstraintHash() {
        return constraintHash;
    }

    @Column(name = "`CONSTRAINT_HASH`", nullable = false)
    public void setConstraintHash(String val) {
        constraintHash = val;
    }

    @Column(name = "`CREATED`", nullable = false)
    public void setCreated(Date val) {
        created = val;
    }

    @Column(name = "`CREATED`", nullable = false)
    public Date getCreated() {
        return created;
    }

    @Column(name = "`MODIFIED`", nullable = false)
    public void setModified(Date val) {
        modified = val;
    }

    @Column(name = "`MODIFIED`", nullable = false)
    public Date getModified() {
        return modified;
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof DBItemSchedulerOrderStepHistory)) {
            return false;
        }
        DBItemSchedulerOrderStepHistory item = (DBItemSchedulerOrderStepHistory) o;
        if (!getId().equals(item.getId())) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return getId() == null ? new Long(0).hashCode() : getId().hashCode();
    }
}
