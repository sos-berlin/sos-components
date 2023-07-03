package com.sos.joc.history.helper;

import java.util.Date;

import com.sos.commons.util.SOSDate;
import com.sos.controller.model.event.EventType;
import com.sos.joc.cluster.bean.history.HistoryOrderBean;
import com.sos.joc.cluster.common.JocClusterUtil;
import com.sos.joc.db.history.DBItemHistoryOrder;
import com.sos.joc.db.history.common.HistorySeverity;
import com.sos.joc.model.order.OrderStateText;

public class CachedOrder {

    private final Long id;
    private final String orderId;
    private final Long mainParentId;
    private final Long parentId;
    private final String workflowPath;
    private final String workflowVersionId;
    private final String workflowPosition;
    private final Date startTime;
    private final Date endTime;

    private CachedError lastStepError;
    private Integer state;
    private boolean hasStates;
    private Long currentHistoryOrderStepId;

    // minutes
    private long lastUsage;

    public CachedOrder(DBItemHistoryOrder item) {
        id = item.getId();
        orderId = item.getOrderId();
        mainParentId = item.getMainParentId();
        parentId = item.getParentId();
        workflowPath = item.getWorkflowPath();
        workflowVersionId = item.getWorkflowVersionId();
        workflowPosition = item.getWorkflowPosition();
        state = item.getState();
        hasStates = item.getHasStates();
        currentHistoryOrderStepId = item.getCurrentHistoryOrderStepId();
        startTime = item.getStartTime();
        endTime = item.getEndTime();

        if (item.getError()) {
            setLastStepError(item.getErrorState(), item.getErrorReason(), item.getErrorCode(), item.getErrorText(), item.getErrorReturnCode());
        }
    }

    public HistoryOrderBean convert(EventType eventType, Long eventId, String controllerId) {
        HistoryOrderBean b = new HistoryOrderBean(eventType, eventId, controllerId, id);
        b.setOrderId(orderId);
        b.setMainParentId(mainParentId);
        b.setParentId(parentId);
        b.setWorkflowPath(workflowPath);
        b.setWorkflowName(JocClusterUtil.getBasenameFromPath(b.getWorkflowPath()));
        b.setWorkflowVersionId(workflowVersionId);
        b.setWorkflowPosition(workflowPosition);
        b.setState(state);
        if (state != null) {
            b.setSeverity(HistorySeverity.map2DbSeverity(state));
        }
        b.setCurrentHistoryOrderStepId(currentHistoryOrderStepId);
        b.setStartTime(startTime);
        b.setEndTime(endTime);
        return b;
    }

    public Long getId() {
        return id;
    }

    public String getOrderId() {
        return orderId;
    }

    public Long getMainParentId() {
        return mainParentId;
    }

    public Long getParentId() {
        return parentId;
    }

    public String getWorkflowPath() {
        return workflowPath;
    }

    public String getWorkflowVersionId() {
        return workflowVersionId;
    }

    public String getWorkflowPosition() {
        return workflowPosition;
    }

    public void setState(Integer val) {
        state = val;
    }

    public Integer getState() {
        return state;
    }

    public String getStateAsText() {
        try {
            return state == null ? null : OrderStateText.fromValue(state).name();
        } catch (Throwable e) {
            return null;
        }
    }

    public boolean getHasStates() {
        return hasStates;
    }

    public void setHasStates(boolean val) {
        hasStates = val;
    }

    public Long getCurrentHistoryOrderStepId() {
        return currentHistoryOrderStepId;
    }

    public void setCurrentHistoryOrderStepId(Long val) {
        currentHistoryOrderStepId = val;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public CachedError getLastStepError() {
        return lastStepError;
    }

    public boolean hasLastStepErrorReturnCode() {
        return lastStepError != null && lastStepError.getReturnCode() != null;
    }

    public void setLastStepError(CachedError val) {
        lastStepError = val;
    }

    public void resetLastStepError() {
        lastStepError = null;
    }

    public void setLastStepError(LogEntry le) {
        setLastStepError(le, null);
    }

    public void setLastStepError(LogEntry le, CachedOrderStep cos) {
        if (le.isError()) {
            String msg = cos == null ? le.getErrorText() : String.format("[Job=%s, pos=%s]%s", cos.getJobName(), cos.getWorkflowPosition(), le
                    .getErrorText() == null ? "" : le.getErrorText());
            setLastStepError(le.getErrorState(), le.getErrorReason(), le.getErrorCode(), msg, le.getReturnCode());
        } else {
            lastStepError = null;
        }
    }

    public void setLastUsage() {
        lastUsage = SOSDate.getMinutes(new Date());
    }

    public long getLastUsage() {
        return lastUsage;
    }

    private void setLastStepError(String errorState, String errorReason, String errorCode, String errorText, Integer returnCode) {
        lastStepError = new CachedError(errorState, errorReason, errorCode, errorText);
        lastStepError.setReturnCode(returnCode);
    }

}
