package com.sos.joc.db.monitoring;

import java.util.Date;

import org.hibernate.annotations.Proxy;
import org.hibernate.type.NumericBooleanConverter;

import com.sos.commons.util.SOSString;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.common.HistoryConstants;
import com.sos.joc.model.order.OrderStateText;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@SuppressWarnings("deprecation")
@Entity
@Table(name = DBLayer.TABLE_MON_ORDERS)
@Proxy(lazy = false)
public class DBItemMonitoringOrder extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[HISTORY_ID]", nullable = false)
    private Long historyId;

    @Column(name = "[CONTROLLER_ID]", nullable = false)
    private String controllerId;

    @Column(name = "[ORDER_ID]", nullable = false)
    private String orderId;

    @Column(name = "[WORKFLOW_PATH]", nullable = false)
    private String workflowPath;

    @Column(name = "[WORKFLOW_VERSION_ID]", nullable = false)
    private String workflowVersionId;

    @Column(name = "[WORKFLOW_POSITION]", nullable = false)
    private String workflowPosition;

    @Column(name = "[WORKFLOW_FOLDER]", nullable = false)
    private String workflowFolder;

    @Column(name = "[WORKFLOW_NAME]", nullable = false)
    private String workflowName;

    @Column(name = "[WORKFLOW_TITLE]", nullable = true)
    private String workflowTitle;// TODO

    /** Foreign key - TABLE_HISTORY_ORDERS.ID */
    @Column(name = "[MAIN_PARENT_ID]", nullable = false)
    private Long mainParentId;

    @Column(name = "[PARENT_ID]", nullable = false)
    private Long parentId;

    @Column(name = "[PARENT_ORDER_ID]", nullable = false)
    private String parentOrderId;

    @Column(name = "[HAS_CHILDREN]", nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
    private boolean hasChildren;

    @Column(name = "[NAME]", nullable = false)
    private String name;

    @Column(name = "[START_CAUSE]", nullable = false)
    private String startCause;

    @Column(name = "[START_TIME_SCHEDULED]", nullable = true)
    private Date startTimeScheduled;

    @Column(name = "[START_TIME]", nullable = false)
    private Date startTime;

    @Column(name = "[START_WORKFLOW_POSITION]", nullable = false)
    private String startWorkflowPosition;

    @Column(name = "[START_VARIABLES]", nullable = true)
    private String startVariables;

    @Column(name = "[CURRENT_HOS_ID]", nullable = false)
    private Long currentHistoryOrderStepId;

    @Column(name = "[END_TIME]", nullable = true)
    private Date endTime;

    @Column(name = "[END_WORKFLOW_POSITION]", nullable = true)
    private String endWorkflowPosition;

    @Column(name = "[END_HOS_ID]", nullable = false)
    private Long endHistoryOrderStepId;

    @Column(name = "[END_RETURN_CODE]", nullable = true)
    private Integer endReturnCode; // event. outcome returnCode (finish instruction)

    @Column(name = "[END_MESSAGE]", nullable = true)
    private String endMessage; // event. outcome returnCode (finish instruction)

    @Column(name = "[SEVERITY]", nullable = false)
    private Integer severity;

    @Column(name = "[STATE]", nullable = false)
    private Integer state;

    @Column(name = "[STATE_TIME]", nullable = false)
    private Date stateTime;

    @Column(name = "[ERROR]", nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
    private boolean error;

    @Column(name = "[ERROR_STATE]", nullable = true)
    private String errorState;

    @Column(name = "[ERROR_REASON]", nullable = true)
    private String errorReason;

    @Column(name = "[ERROR_RETURN_CODE]", nullable = true)
    private Integer errorReturnCode;

    @Column(name = "[ERROR_CODE]", nullable = true)
    private String errorCode;

    @Column(name = "[ERROR_TEXT]", nullable = true)
    private String errorText;

    /** Foreign key - TABLE_HISTORY_LOGS.ID, KEY */
    @Column(name = "[LOG_ID]", nullable = true)
    private Long logId;

    @Column(name = "[CREATED]", nullable = false)
    private Date created;

    @Column(name = "[MODIFIED]", nullable = false)
    private Date modified;

    public DBItemMonitoringOrder() {
    }

    public Long getHistoryId() {
        return historyId;
    }

    public void setHistoryId(Long val) {
        historyId = val;
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
        startWorkflowPosition = normalizeWorkflowPosition(val);
    }

    public String getStartVariables() {
        return startVariables;
    }

    public void setStartVariables(String val) {
        startVariables = val;
    }

    public Long getCurrentHistoryOrderStepId() {
        return currentHistoryOrderStepId;
    }

    public void setCurrentHistoryOrderStepId(Long val) {
        if (val == null) {
            val = Long.valueOf(0);
        }
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
        endWorkflowPosition = normalizeWorkflowPosition(val);
    }

    public void setEndHistoryOrderStepId(Long val) {
        if (val == null) {
            val = Long.valueOf(0);
        }
        endHistoryOrderStepId = val;
    }

    public Long getEndHistoryOrderStepId() {
        return endHistoryOrderStepId;
    }

    public void setEndReturnCode(Integer val) {
        endReturnCode = val;
    }

    public Integer getEndReturnCode() {
        return endReturnCode;
    }

    public void setEndMessage(String val) {
        endMessage = normalizeEndMessage(val);
    }

    public String getEndMessage() {
        return endMessage;
    }

    public Integer getSeverity() {
        return severity;
    }

    public void setSeverity(Integer val) {
        severity = val;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer val) {
        state = val;
    }

    public Date getStateTime() {
        return stateTime;
    }

    public void setStateTime(Date val) {
        stateTime = val;
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
    public static String normalizeEndMessage(String val) {
        return normalizeValue(val, HistoryConstants.MAX_LEN_END_MESSAGE);
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
    public OrderStateText getStateAsEnum() {
        try {
            return OrderStateText.fromValue(state);
        } catch (Throwable e) {
            return OrderStateText.UNKNOWN;
        }
    }

}
