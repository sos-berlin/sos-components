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
@Table(name = DBLayer.TABLE_SCHEDULER_ORDER_HISTORY)
@SequenceGenerator(name = DBLayer.TABLE_SCHEDULER_ORDER_HISTORY_SEQUENCE, sequenceName = DBLayer.TABLE_SCHEDULER_ORDER_HISTORY_SEQUENCE, allocationSize = 1)
public class DBItemSchedulerOrderHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Primary key */
    private Long id;// db id

    private String schedulerId;
    private String orderKey;// event
    private String workflowPosition; // event
    private Long retryCounter; // run counter (if rerun)
    /** Foreign key - TABLE_SCHEDULER_ORDER_HISTORY.ID, KEY */
    private Long mainParentId;// db
    private Long parentId;// db
    private String parentOrderKey;// db
    /** Others */
    private boolean hasChildren;
    private String name;// TODO
    private String title;// TODO
    private String workflowPath;// event
    private String workflowVersion;// event
    private String workflowFolder;// extracted from workflowPath
    private String workflowName;// extracted from workflowPath
    private String workflowTitle;// TODO
    private String startCause;// event. implemented: unknown(period),fork. planned: file trigger, setback, unskip, unstop ...
    private Date startTimePlanned;// event
    private Date startTime;
    private String startWorkflowPosition; // event
    private String startEventId;// event <- order added event id
    private String startParameters;
    private Long currentStepId; // db
    private Date endTime;
    private String endWorkflowPosition; // event
    private Long endStepId; // db. TABLE_SCHEDULER_ORDER_STEP_HISTORY.ID
    private String endEventId;// event <- order finisched event id
    private String state;// event. planned: planned, completed, cancelled, suspended...
    private String stateText;// TODO
    private boolean error;// TODO
    private Long errorStepId; // db. TABLE_SCHEDULER_ORDER_STEP_HISTORY.ID
    private String errorCode;// TODO
    private String errorText;
    private String constraintHash; // hash from schedulerId, startEventId for db unique constraint

    private Date created;
    private Date modified;

    public DBItemSchedulerOrderHistory() {
    }

    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_SCHEDULER_ORDER_HISTORY_SEQUENCE)
    @Column(name = "`ID`", nullable = false)
    public Long getId() {
        return id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_SCHEDULER_ORDER_HISTORY_SEQUENCE)
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
    @Column(name = "`MAIN_PARENT_ID`", nullable = false)
    public Long getMainParentId() {
        return mainParentId;
    }

    @Column(name = "`MAIN_PARENT_ID`", nullable = false)
    public void setMainParentId(Long val) {
        mainParentId = val;
    }

    @Column(name = "`PARENT_ID`", nullable = false)
    public Long getParentId() {
        return parentId;
    }

    @Column(name = "`PARENT_ID`", nullable = false)
    public void setParentId(Long val) {
        parentId = val;
    }

    @Column(name = "`PARENT_ORDER_KEY`", nullable = false)
    public String getParentOrderKey() {
        return parentOrderKey;
    }

    @Column(name = "`PARENT_ORDER_KEY`", nullable = false)
    public void setParentOrderKey(String val) {
        if (val == null) {
            val = DBLayer.DEFAULT_KEY;
        }
        parentOrderKey = val;
    }

    /** Others */
    @Column(name = "`HAS_CHILDREN`", nullable = false)
    @Type(type = "numeric_boolean")
    public void setHasChildren(boolean val) {
        hasChildren = val;
    }

    @Column(name = "`HAS_CHILDREN`", nullable = false)
    @Type(type = "numeric_boolean")
    public boolean getHasChildren() {
        return hasChildren;
    }

    @Column(name = "`NAME`", nullable = false)
    public String getName() {
        return name;
    }

    @Column(name = "`NAME`", nullable = false)
    public void setName(String val) {
        name = val;
    }

    @Column(name = "`TITLE`", nullable = true)
    public String getTitle() {
        return title;
    }

    @Column(name = "`TITLE`", nullable = true)
    public void setTitle(String val) {
        title = val;
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

    @Column(name = "`WORKFLOW_FOLDER`", nullable = false)
    public String getWorkflowFolder() {
        return workflowFolder;
    }

    @Column(name = "`WORKFLOW_FOLDER`", nullable = false)
    public void setWorkflowFolder(String val) {
        workflowFolder = val;
    }

    @Column(name = "`WORKFLOW_NAME`", nullable = false)
    public String getWorkflowName() {
        return workflowName;
    }

    @Column(name = "`WORKFLOW_NAME`", nullable = false)
    public void setWorkflowName(String val) {
        workflowName = val;
    }

    @Column(name = "`WORKFLOW_TITLE`", nullable = true)
    public String getWorkflowTitle() {
        return workflowTitle;
    }

    @Column(name = "`WORKFLOW_TITLE`", nullable = true)
    public void setWorkflowTitle(String val) {
        workflowTitle = val;
    }

    @Column(name = "`START_CAUSE`", nullable = false)
    public String getStartCause() {
        return startCause;
    }

    @Column(name = "`START_CAUSE`", nullable = false)
    public void setStartCause(String val) {
        startCause = val;
    }

    @Column(name = "`START_TIME_PLANNED`", nullable = true)
    public Date getStartTimePlanned() {
        return startTimePlanned;
    }

    @Column(name = "`START_TIME_PLANNED`", nullable = true)
    public void setStartTimePlanned(Date val) {
        startTimePlanned = val;
    }

    @Column(name = "`START_TIME`", nullable = false)
    public Date getStartTime() {
        return startTime;
    }

    @Column(name = "`START_TIME`", nullable = false)
    public void setStartTime(Date val) {
        startTime = val;
    }

    @Column(name = "`START_WORKFLOW_POSITION`", nullable = false)
    public String getStartWorkflowPosition() {
        return startWorkflowPosition;
    }

    @Column(name = "`START_WORKFLOW_POSITION`", nullable = false)
    public void setStartWorkflowPosition(String val) {
        startWorkflowPosition = val;
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

    @Column(name = "`CURRENT_STEP_ID`", nullable = false)
    public Long getCurrentStepId() {
        return currentStepId;
    }

    @Column(name = "`CURRENT_STEP_ID`", nullable = false)
    public void setCurrentStepId(Long val) {
        currentStepId = val;
    }

    @Column(name = "`END_TIME`", nullable = true)
    public Date getEndTime() {
        return endTime;
    }

    @Column(name = "`END_TIME`", nullable = true)
    public void setEndTime(Date val) {
        endTime = val;
    }

    @Column(name = "`END_WORKFLOW_POSITION`", nullable = true)
    public String getEndWorkflowPosition() {
        return endWorkflowPosition;
    }

    @Column(name = "`END_WORKFLOW_POSITION`", nullable = true)
    public void setEndWorkflowPosition(String val) {
        endWorkflowPosition = val;
    }

    @Column(name = "`END_STEP_ID`", nullable = false)
    public void setEndStepId(Long val) {
        endStepId = val;
    }

    @Column(name = "`END_STEP_ID`", nullable = false)
    public Long getEndStepId() {
        return endStepId;
    }

    @Column(name = "`END_EVENT_ID`", nullable = true)
    public void setEndEventId(String val) {
        endEventId = val;
    }

    @Column(name = "`END_EVENT_ID`", nullable = true)
    public String getEndEventId() {
        return endEventId;
    }

    @Column(name = "`STATE`", nullable = false)
    public String getState() {
        return state;
    }

    @Column(name = "`STATE`", nullable = false)
    public void setState(String val) {
        state = val;
    }

    @Column(name = "`STATE_TEXT`", nullable = true)
    public String getStateText() {
        return stateText;
    }

    @Column(name = "`STATE_TEXT`", nullable = true)
    public void setStateText(String val) {
        stateText = val;
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

    @Column(name = "`ERROR_STEP_ID`", nullable = false)
    public void setErrorStepId(Long val) {
        errorStepId = val;
    }

    @Column(name = "`ERROR_STEP_ID`", nullable = false)
    public Long getErrorStepId() {
        return errorStepId;
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
        if (o == null || !(o instanceof DBItemSchedulerOrderHistory)) {
            return false;
        }
        DBItemSchedulerOrderHistory item = (DBItemSchedulerOrderHistory) o;
        if (!getId().equals(item.getId())) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return getId() == null ? new Long(0).hashCode() : getId().hashCode();
    }
}
