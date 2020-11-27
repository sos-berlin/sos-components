package com.sos.joc.classes.history;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.joc.db.history.DBItemHistoryOrder;
import com.sos.joc.db.history.DBItemHistoryOrderState;
import com.sos.joc.db.history.DBItemHistoryOrderStep;
import com.sos.joc.model.common.Err;
import com.sos.joc.model.common.HistoryOrderState;
import com.sos.joc.model.common.HistoryState;
import com.sos.joc.model.common.HistoryStateText;
import com.sos.joc.model.job.TaskHistoryItem;
import com.sos.joc.model.order.OrderHistoryItem;
import com.sos.joc.model.order.OrderHistoryStateItem;
import com.sos.joc.model.order.OrderStateText;

public class HistoryMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryMapper.class);

    public static OrderHistoryItem map2OrderHistoryItem(DBItemHistoryOrder item) {
        OrderHistoryItem history = new OrderHistoryItem();
        history.setControllerId(item.getJobSchedulerId());
        history.setEndTime(item.getEndTime());
        history.setHistoryId(item.getId());
        history.setOrderId(item.getOrderKey());
        history.setPlannedTime(item.getStartTimePlanned());
        history.setStartTime(item.getStartTime());
        history.setPosition(getWorkflowPosition(item));
        history.setState(getState(item));
        history.setOrderState(getOrderState(item.getStateAsEnum()));
        history.setSurveyDate(item.getModified());
        history.setWorkflow(item.getWorkflowPath());
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
        history.setState(getState(item));
        history.setCriticality(item.getCriticalityAsEnum().value().toLowerCase());
        history.setSurveyDate(item.getModified());
        history.setTaskId(item.getId());
        history.setWorkflow(item.getWorkflowPath());
        history.setPosition(item.getPosition().toString());
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
        if (!SOSString.isEmpty(item.getStartWorkflowPosition())) {
            return item.getStartWorkflowPosition();
        } else if (!SOSString.isEmpty(item.getEndWorkflowPosition())) {
            return item.getEndWorkflowPosition();
        }
        return item.getWorkflowPosition();
    }

    private static HistoryState getState(DBItemHistoryOrder item) {
        HistoryState state = new HistoryState();
        if (item.isSuccessFul()) {
            state.setSeverity(0);
            state.set_text(HistoryStateText.SUCCESSFUL);
        } else if (item.isInComplete()) {
            state.setSeverity(1);
            state.set_text(HistoryStateText.INCOMPLETE);
        } else if (item.isFailed()) {
            state.setSeverity(2);
            state.set_text(HistoryStateText.FAILED);
        }
        return state;
    }

    private static HistoryState getState(DBItemHistoryOrderStep step) {
        HistoryState state = new HistoryState();
        if (step.isSuccessFul()) {
            state.setSeverity(0);
            state.set_text(HistoryStateText.SUCCESSFUL);
        } else if (step.isInComplete()) {
            state.setSeverity(1);
            state.set_text(HistoryStateText.INCOMPLETE);
        } else if (step.isFailed()) {
            state.setSeverity(2);
            state.set_text(HistoryStateText.FAILED);
        }
        return state;
    }

    private static HistoryOrderState getOrderState(OrderStateText st) {
        HistoryOrderState state = new HistoryOrderState();
        try {
            state.set_text(st);
            switch (state.get_text()) {
            case FINISHED:
                state.setSeverity(0);
                break;
            case PLANNED:
            case PENDING:
            case RUNNING:
            case WAITING:
            case RESUMED:
            case SUSPENDMARKED:
            case RESUMEMARKED:
                state.setSeverity(1);
                break;
            case SUSPENDED:
            case FAILED:
            case BLOCKED:
            case CANCELLED:
            case UNKNOWN:
                state.setSeverity(2);
                break;
            }
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
            state.setSeverity(2);
            state.set_text(OrderStateText.UNKNOWN);
        }

        return state;
    }

}
