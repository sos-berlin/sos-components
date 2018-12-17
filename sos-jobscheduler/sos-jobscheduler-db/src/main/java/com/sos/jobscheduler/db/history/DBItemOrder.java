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

import org.hibernate.annotations.Type;

import com.sos.jobscheduler.db.DBLayer;

@Entity
@Table(name = DBLayer.HISTORY_TABLE_ORDERS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[CONSTRAINT_HASH]" }) })
@SequenceGenerator(name = DBLayer.HISTORY_TABLE_ORDERS_SEQUENCE, sequenceName = DBLayer.HISTORY_TABLE_ORDERS_SEQUENCE, allocationSize = 1)
public class DBItemOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.HISTORY_TABLE_ORDERS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[MASTER_ID]", nullable = false)
    private String masterId;

    @Column(name = "[ORDER_KEY]", nullable = false)
    private String orderKey;// event

    @Column(name = "[WORKFLOW_POSITION]", nullable = false)
    private String workflowPosition; // event

    @Column(name = "[RETRY_COUNTER]", nullable = false)
    private Long retryCounter; // run counter (if rerun)

    /** Foreign key - TABLE_HISTORY_ORDERS.ID, KEY */
    @Column(name = "[MAIN_PARENT_ID]", nullable = false)
    private Long mainParentId;// db

    @Column(name = "[PARENT_ID]", nullable = false)
    private Long parentId;// db

    @Column(name = "[PARENT_ORDER_KEY]", nullable = false)
    private String parentOrderKey;// db

    /** Others */
    @Column(name = "[HAS_CHILDREN]", nullable = false)
    private boolean hasChildren;

    @Column(name = "[NAME]", nullable = false)
    private String name;// TODO

    @Column(name = "[TITLE]", nullable = true)
    private String title;// TODO

    @Column(name = "[WORKFLOW_PATH]", nullable = false)
    private String workflowPath;// event

    @Column(name = "[WORKFLOW_VERSION]", nullable = false)
    private String workflowVersion;// event

    @Column(name = "[WORKFLOW_FOLDER]", nullable = false)
    private String workflowFolder;// extracted from workflowPath

    @Column(name = "[WORKFLOW_NAME]", nullable = false)
    private String workflowName;// extracted from workflowPath

    @Column(name = "[WORKFLOW_TITLE]", nullable = true)
    private String workflowTitle;// TODO

    @Column(name = "[START_CAUSE]", nullable = false)
    private String startCause;// event. implemented: unknown(period),fork. planned: file trigger, setback, unskip, unstop ...

    @Column(name = "[START_TIME_PLANNED]", nullable = true)
    private Date startTimePlanned;// event

    @Column(name = "[START_TIME]", nullable = false)
    private Date startTime;

    @Column(name = "[START_WORKFLOW_POSITION]", nullable = false)
    private String startWorkflowPosition; // event

    @Column(name = "[START_EVENT_ID]", nullable = false)
    private String startEventId;// event <- order added event id

    @Column(name = "[START_PARAMETERS]", nullable = true)
    private String startParameters;

    @Column(name = "[CURRENT_STEP_ID]", nullable = false)
    private Long currentStepId; // db

    @Column(name = "[END_TIME]", nullable = true)
    private Date endTime;

    @Column(name = "[END_WORKFLOW_POSITION]", nullable = true)
    private String endWorkflowPosition; // event

    @Column(name = "[END_STEP_ID]", nullable = false)
    private Long endStepId; // db. TABLE_HISTORY_ORDER_STEPS.ID

    @Column(name = "[END_EVENT_ID]", nullable = true)
    private String endEventId;// event <- order finisched event id

    @Column(name = "[STATUS]", nullable = false)
    private String status;// event. planned: planned, completed, cancelled, suspended...

    @Column(name = "[STATE_TEXT]", nullable = true)
    private String stateText;// TODO

    @Column(name = "[ERROR]", nullable = false)
    @Type(type = "numeric_boolean")
    private boolean error;// TODO

    @Column(name = "[ERROR_STEP_ID]", nullable = false)
    private Long errorStepId; // db. HISTORY_TABLE_ORDER_STEPS.ID

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

    public DBItemOrder() {
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

    public Long getMainParentId() {
        return mainParentId;
    }

    public void setMainParentId(Long val) {
        mainParentId = val;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long val) {
        parentId = val;
    }

    public String getParentOrderKey() {
        return parentOrderKey;
    }

    public void setParentOrderKey(String val) {
        if (val == null) {
            val = DBLayer.DEFAULT_KEY;
        }
        parentOrderKey = val;
    }

    public void setHasChildren(boolean val) {
        hasChildren = val;
    }

    public boolean getHasChildren() {
        return hasChildren;
    }

    public String getName() {
        return name;
    }

    public void setName(String val) {
        name = val;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String val) {
        title = val;
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

    public String getWorkflowTitle() {
        return workflowTitle;
    }

    public void setWorkflowTitle(String val) {
        workflowTitle = val;
    }

    public String getStartCause() {
        return startCause;
    }

    public void setStartCause(String val) {
        startCause = val;
    }

    public Date getStartTimePlanned() {
        return startTimePlanned;
    }

    public void setStartTimePlanned(Date val) {
        startTimePlanned = val;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date val) {
        startTime = val;
    }

    public String getStartWorkflowPosition() {
        return startWorkflowPosition;
    }

    public void setStartWorkflowPosition(String val) {
        startWorkflowPosition = val;
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

    public Long getCurrentStepId() {
        return currentStepId;
    }

    public void setCurrentStepId(Long val) {
        currentStepId = val;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date val) {
        endTime = val;
    }

    public String getEndWorkflowPosition() {
        return endWorkflowPosition;
    }

    public void setEndWorkflowPosition(String val) {
        endWorkflowPosition = val;
    }

    public void setEndStepId(Long val) {
        endStepId = val;
    }

    public Long getEndStepId() {
        return endStepId;
    }

    public void setEndEventId(String val) {
        endEventId = val;
    }

    public String getEndEventId() {
        return endEventId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String val) {
        status = val;
    }

    public String getStateText() {
        return stateText;
    }

    public void setStateText(String val) {
        stateText = val;
    }

    public void setError(boolean val) {
        error = val;
    }

    public boolean getError() {
        return error;
    }

    public void setErrorStepId(Long val) {
        errorStepId = val;
    }

    public Long getErrorStepId() {
        return errorStepId;
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
        if (o == null || !(o instanceof DBItemOrder)) {
            return false;
        }
        DBItemOrder item = (DBItemOrder) o;
        if (!getId().equals(item.getId())) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return getId() == null ? new Long(0).hashCode() : getId().hashCode();
    }
}
