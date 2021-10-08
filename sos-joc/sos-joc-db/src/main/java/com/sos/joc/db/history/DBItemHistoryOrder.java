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

import com.sos.commons.util.SOSString;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.common.HistoryConstants;
import com.sos.joc.db.history.common.HistorySeverity;
import com.sos.joc.model.order.OrderStateText;

@Entity
@Table(name = DBLayer.TABLE_HISTORY_ORDERS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[CONSTRAINT_HASH]" }) })
@SequenceGenerator(name = DBLayer.TABLE_HISTORY_ORDERS_SEQUENCE, sequenceName = DBLayer.TABLE_HISTORY_ORDERS_SEQUENCE, allocationSize = 1)
public class DBItemHistoryOrder extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_HISTORY_ORDERS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[CONTROLLER_ID]", nullable = false)
    private String controllerId;

    @Column(name = "[ORDER_ID]", nullable = false)
    private String orderId;// event

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

    @Column(name = "[WORKFLOW_TITLE]", nullable = true)
    private String workflowTitle;// TODO

    /** Foreign key - TABLE_HISTORY_ORDERS.ID */
    @Column(name = "[MAIN_PARENT_ID]", nullable = false)
    private Long mainParentId;// db

    @Column(name = "[PARENT_ID]", nullable = false)
    private Long parentId;// db

    @Column(name = "[PARENT_ORDER_ID]", nullable = false)
    private String parentOrderId;// db

    @Column(name = "[HAS_CHILDREN]", nullable = false)
    @Type(type = "numeric_boolean")
    private boolean hasChildren;

    @Column(name = "[RETRY_COUNTER]", nullable = false)
    private Integer retryCounter; // run counter (if rerun)

    @Column(name = "[NAME]", nullable = false)
    private String name;// TODO

    @Column(name = "[START_CAUSE]", nullable = false)
    private String startCause;// event. implemented: unknown(period),fork. planned: file trigger, setback, unskip, unstop ...

    @Column(name = "[START_TIME_SCHEDULED]", nullable = true)
    private Date startTimeScheduled;// event

    @Column(name = "[START_TIME]", nullable = false)
    private Date startTime;

    @Column(name = "[START_WORKFLOW_POSITION]", nullable = false)
    private String startWorkflowPosition; // event

    @Column(name = "[START_EVENT_ID]", nullable = false)
    private Long startEventId;// event <- order added event id

    @Column(name = "[START_VARIABLES]", nullable = true)
    private String startVariables;

    @Column(name = "[CURRENT_HOS_ID]", nullable = false)
    private Long currentHistoryOrderStepId; // db

    @Column(name = "[END_TIME]", nullable = true)
    private Date endTime;

    @Column(name = "[END_WORKFLOW_POSITION]", nullable = true)
    private String endWorkflowPosition; // event

    @Column(name = "[END_EVENT_ID]", nullable = true)
    private Long endEventId;// event <- order finisched event id

    @Column(name = "[END_HOS_ID]", nullable = false)
    private Long endHistoryOrderStepId; // db. TABLE_HISTORY_ORDER_STEPS.ID

    @Column(name = "[SEVERITY]", nullable = false)
    private Integer severity;

    @Column(name = "[STATE]", nullable = false)
    private Integer state;

    @Column(name = "[STATE_TIME]", nullable = false)
    private Date stateTime;

    @Column(name = "[STATE_TEXT]", nullable = true)
    private String stateText;

    @Column(name = "[HAS_STATES]", nullable = false)
    @Type(type = "numeric_boolean")
    private boolean hasStates;

    @Column(name = "[ERROR]", nullable = false)
    @Type(type = "numeric_boolean")
    private boolean error;// TODO

    @Column(name = "[ERROR_STATE]", nullable = true)
    private String errorState;// event. outcome type

    @Column(name = "[ERROR_REASON]", nullable = true)
    private String errorReason;// event. outcome reason type

    @Column(name = "[ERROR_RETURN_CODE]", nullable = true)
    private Integer errorReturnCode; // event. outcome returnCode (type failed)

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

    public DBItemHistoryOrder() {
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

    public String getWorkflowTitle() {
        return workflowTitle;
    }

    public void setWorkflowTitle(String val) {
        workflowTitle = val;
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

    public String getParentOrderId() {
        return parentOrderId;
    }

    public void setParentOrderId(String val) {
        if (val == null) {
            val = DBLayer.DEFAULT_KEY;
        }
        parentOrderId = val;
    }

    public void setHasChildren(boolean val) {
        hasChildren = val;
    }

    public boolean getHasChildren() {
        return hasChildren;
    }

    public Integer getRetryCounter() {
        return retryCounter;
    }

    public void setRetryCounter(Integer val) {
        retryCounter = val;
    }

    public String getName() {
        return name;
    }

    public void setName(String val) {
        name = val;
    }

    public String getStartCause() {
        return startCause;
    }

    public void setStartCause(String val) {
        startCause = val;
    }

    public Date getStartTimeScheduled() {
        return startTimeScheduled;
    }

    public void setStartTimeScheduled(Date val) {
        startTimeScheduled = val;
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

    public Long getCurrentHistoryOrderStepId() {
        return currentHistoryOrderStepId;
    }

    public void setCurrentHistoryOrderStepId(Long val) {
        currentHistoryOrderStepId = val;
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
        if (SOSString.isEmpty(val)) {
            val = null;
        }
        endWorkflowPosition = val;
    }

    public void setEndEventId(Long val) {
        endEventId = val;
    }

    public Long getEndEventId() {
        return endEventId;
    }

    public void setEndHistoryOrderStepId(Long val) {
        endHistoryOrderStepId = val;
    }

    public Long getEndHistoryOrderStepId() {
        return endHistoryOrderStepId;
    }

    public Integer getSeverity() {
        return severity;
    }

    public void setSeverity(Integer val) {
        severity = val;
    }

    @Transient
    public void setSeverity(OrderStateText val) {
        setSeverity(HistorySeverity.map2DbSeverity(val));
    }

    public Integer getState() {
        return state;
    }

    @Transient
    public OrderStateText getStateAsEnum() {
        try {
            return OrderStateText.fromValue(state);
        } catch (Throwable e) {
            return OrderStateText.UNKNOWN;
        }
    }

    public void setState(Integer val) {
        state = val;
    }

    @Transient
    public void setState(OrderStateText val) {
        setState(val == null ? null : val.intValue());
    }

    public Date getStateTime() {
        return stateTime;
    }

    public void setStateTime(Date val) {
        stateTime = val;
    }

    public String getStateText() {
        return stateText;
    }

    public void setStateText(String val) {
        stateText = val;
    }

    public void setHasStates(boolean val) {
        hasStates = val;
    }

    public boolean getHasStates() {
        return hasStates;
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

    public void setErrorReturnCode(Integer val) {
        errorReturnCode = val;
    }

    public Integer getErrorReturnCode() {
        return errorReturnCode;
    }

    public void setErrorCode(String val) {
        errorCode = normalizeErrorCode(val);
    }

    @Transient
    public static String normalizeErrorCode(String val) {
        return normalizeValue(val, HistoryConstants.MAX_LEN_ERROR_CODE);
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorText(String val) {
        errorText = normalizeErrorText(val);
    }

    @Transient
    public static String normalizeErrorText(String val) {
        return normalizeValue(val, HistoryConstants.MAX_LEN_ERROR_TEXT);
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

}
