package com.sos.joc.classes.history;

import com.sos.commons.util.SOSString;
import com.sos.joc.db.history.DBItemHistoryOrder;
import com.sos.joc.db.history.DBItemHistoryOrderState;
import com.sos.joc.db.history.DBItemHistoryOrderStep;
import com.sos.joc.db.history.common.HistorySeverity;
import com.sos.joc.model.common.Err;
import com.sos.joc.model.common.HistoryOrderState;
import com.sos.joc.model.common.HistoryState;
import com.sos.joc.model.common.HistoryStateText;
import com.sos.joc.model.job.TaskHistoryItem;
import com.sos.joc.model.order.OrderHistoryItem;
import com.sos.joc.model.order.OrderHistoryStateItem;
import com.sos.joc.model.order.OrderStateText;

public class HistoryMapper {

    public static OrderHistoryItem map2OrderHistoryItem(DBItemHistoryOrder item) {
        OrderHistoryItem history = new OrderHistoryItem();
        history.setControllerId(item.getJobSchedulerId());
        history.setEndTime(item.getEndTime());
        history.setHistoryId(item.getId());
        history.setOrderId(item.getOrderKey());
        history.setPlannedTime(item.getStartTimePlanned());
        history.setStartTime(item.getStartTime());
        history.setState(getState(item.getSeverity()));
        history.setOrderState(getOrderState(item.getStateAsEnum()));
        history.setSurveyDate(item.getModified());
        history.setWorkflow(item.getWorkflowPath());
        history.setPosition(getWorkflowPosition(item));
        history.setSequence(getSequence(item));
        return history;
    }

    public static TaskHistoryItem map2TaskHistoryItem(DBItemHistoryOrderStep item) {
        TaskHistoryItem history = new TaskHistoryItem();
        history.setControllerId(item.getJobSchedulerId());
        history.setAgentUrl(item.getAgentUri());
        history.setStartTime(item.getStartTime());
        history.setEndTime(item.getEndTime());
        history.setError(setError(item));
        history.setJob(item.getJobName());
        history.setOrderId(item.getOrderKey());
        history.setExitCode(item.getReturnCode());
        history.setState(getState(item.getSeverity()));
        history.setCriticality(item.getCriticalityAsEnum().value().toLowerCase());
        history.setSurveyDate(item.getModified());
        history.setTaskId(item.getId());
        history.setWorkflow(item.getWorkflowPath());
        history.setPosition(item.getWorkflowPosition());
        history.setSequence(item.getPosition());
        history.setRetryCounter(item.getRetryCounter());
        return history;
    }

    public static OrderHistoryStateItem map2OrderHistoryStateItem(DBItemHistoryOrderState item) {
        OrderHistoryStateItem history = new OrderHistoryStateItem();
        history.setStateTime(item.getStateTime());
        history.setStateText(item.getStateText());
        history.setState(getOrderState(item.getStateAsEnum()));
        return history;
    }

    private static Err setError(DBItemHistoryOrderStep step) {
        if (step.getError()) {
            Err error = new Err();
            // TODO maybe use step.getErrorState()
            error.setCode(step.getErrorCode());
            if (step.getErrorText() != null && step.getErrorText().isEmpty()) {
                error.setMessage(step.getErrorText());
            } else {
                error.setMessage(step.getErrorReason());
            }
            return error;
        }
        return null;
    }

    private static String getWorkflowPosition(DBItemHistoryOrder item) {
        if (SOSString.isEmpty(item.getStartWorkflowPosition()) || item.getStartWorkflowPosition().equals("0")) {
            return null;
        }
        return item.getStartWorkflowPosition();
    }

    private static Integer getSequence(DBItemHistoryOrder item) {
        return HistoryPosition.getLast(item.getStartWorkflowPosition());
    }

    private static HistoryState getState(Integer severity) {
        HistoryState state = new HistoryState();
        state.setSeverity(severity);

        switch (severity.intValue()) {
        case HistorySeverity.SUCCESSFUL:
            state.set_text(HistoryStateText.SUCCESSFUL);
            break;
        case HistorySeverity.INCOMPLETE:
            state.set_text(HistoryStateText.INCOMPLETE);
            break;
        case HistorySeverity.FAILED:
            state.set_text(HistoryStateText.FAILED);
            break;
        }
        return state;
    }

    private static HistoryOrderState getOrderState(OrderStateText st) {
        HistoryOrderState state = new HistoryOrderState();
        state.set_text(st);

        switch (state.get_text()) {
        case FINISHED:
            state.setSeverity(HistorySeverity.SUCCESSFUL);
            break;
        case PLANNED:
        case PENDING:
        case RUNNING:
        case WAITING:
        case RESUMED:
        case SUSPENDMARKED:
        case RESUMEMARKED:
            state.setSeverity(HistorySeverity.INCOMPLETE);
            break;
        case SUSPENDED:
        case FAILED:
        case BLOCKED:
        case BROKEN:
        case CANCELLED:
        case UNKNOWN:
            state.setSeverity(HistorySeverity.FAILED);
            break;
        }
        return state;
    }

}
