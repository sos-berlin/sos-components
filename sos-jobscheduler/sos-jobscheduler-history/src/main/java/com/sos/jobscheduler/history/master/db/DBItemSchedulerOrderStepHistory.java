package com.sos.jobscheduler.history.master.db;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;

@Entity
@Table(name = DBLayer.TABLE_SCHEDULER_ORDER_STEP_HISTORY)
@SequenceGenerator(name = DBLayer.TABLE_SCHEDULER_ORDER_STEP_HISTORY_SEQUENCE, sequenceName = DBLayer.TABLE_SCHEDULER_ORDER_STEP_HISTORY_SEQUENCE, allocationSize = 1)
public class DBItemSchedulerOrderStepHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Primary key */
    private Long id;// db
    /** Identifier */
    private String schedulerId;
    private String orderKey;// event TODO redundant?
    private String workflowPosition; // event
    private Long retryCounter; // run counter (if rerun)
    /** Foreign key - TABLE_SCHEDULER_ORDER_HISTORY.ID */
    private Long orderHistoryId;// db
    /** Others */
    private String jobPath;// event
    private String jobFolder;// event
    private String jobName;// event
    private String jobTitle;// event
    private String agentUri;// event
    private String startCause;// event
    private Date startTime;// event
    private String startParameters;// event
    private Date endTime;// event
    private String endParameters;// event
    private Long returnCode;// event
    private String state;// event. planned: completed, stopped, skipped, setback ...
    private boolean error;// TODO
    private String errorCode;// TODO
    private String errorText;
    private byte[] log;// TODO extra table?

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

    /** Identifier */
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
    @Column(name = "`ORDER_HISTORY_ID`", nullable = false)
    public Long getOrderHistoryId() {
        return orderHistoryId;
    }

    @Column(name = "`ORDER_HISTORY_ID`", nullable = false)
    public void setOrderHistoryId(Long val) {
        orderHistoryId = val;
    }

    /** Others */
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

    /** @Lob
     * @Column(name = "`LOG`", nullable = true) public byte[] getLog() { return log; }
     * 
     * @Lob
     * @Column(name = "`LOG`", nullable = true) public void setLog(byte[] val) { log = val; } */

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`CREATED`", nullable = false)
    public void setCreated(Date val) {
        created = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`CREATED`", nullable = false)
    public Date getCreated() {
        return created;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`MODIFIED`", nullable = false)
    public void setModified(Date val) {
        modified = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`MODIFIED`", nullable = false)
    public Date getModified() {
        return modified;
    }
}
