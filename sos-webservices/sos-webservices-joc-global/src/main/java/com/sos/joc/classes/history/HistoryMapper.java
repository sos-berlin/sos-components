package com.sos.joc.classes.history;

import java.io.IOException;

import com.sos.commons.util.SOSString;
import com.sos.inventory.model.common.Variables;
import com.sos.joc.Globals;
import com.sos.joc.classes.OrdersHelper;
import com.sos.joc.db.history.DBItemHistoryOrder;
import com.sos.joc.db.history.DBItemHistoryOrderState;
import com.sos.joc.db.history.DBItemHistoryOrderStep;
import com.sos.joc.db.history.common.HistorySeverity;
import com.sos.joc.model.common.Err;
import com.sos.joc.model.common.HistoryState;
import com.sos.joc.model.common.HistoryStateText;
import com.sos.joc.model.job.TaskHistoryItem;
import com.sos.joc.model.order.OrderHistoryItem;
import com.sos.joc.model.order.OrderHistoryStateItem;
import com.sos.joc.model.order.OrderState;
import com.sos.joc.model.order.OrderStateText;

public class HistoryMapper {

    public static OrderHistoryItem map2OrderHistoryItem(DBItemHistoryOrder item) {
        OrderHistoryItem history = new OrderHistoryItem();
        history.setControllerId(item.getControllerId());
        history.setEndTime(item.getEndTime());
        history.setHistoryId(item.getId());
        history.setOrderId(item.getOrderId());
        history.setPlannedTime(item.getStartTimePlanned());
        history.setStartTime(item.getStartTime());
        history.setState(getState(item.getSeverity()));
        history.setOrderState(OrdersHelper.getState(item.getStateAsEnum()));
        history.setSurveyDate(item.getModified());
        history.setWorkflow(item.getWorkflowPath());
        history.setPosition(getWorkflowPosition(item));
        history.setSequence(getSequence(item));
        history.setArguments(getVariables(item.getStartParameters()));
        return history;
    }

    public static TaskHistoryItem map2TaskHistoryItem(DBItemHistoryOrderStep item) {
        TaskHistoryItem history = new TaskHistoryItem();
        history.setControllerId(item.getControllerId());
        history.setAgentUrl(item.getAgentUri());
        history.setStartTime(item.getStartTime());
        history.setEndTime(item.getEndTime());
        history.setError(setError(item));
        history.setJob(item.getJobName());
        history.setOrderId(item.getOrderId());
        history.setExitCode(item.getReturnCode());
        history.setState(getState(item.getSeverity()));
        history.setCriticality(item.getCriticalityAsEnum().value().toLowerCase());
        history.setSurveyDate(item.getModified());
        history.setTaskId(item.getId());
        history.setWorkflow(item.getWorkflowPath());
        history.setPosition(item.getWorkflowPosition());
        history.setSequence(item.getPosition());
        history.setRetryCounter(item.getRetryCounter());
        history.setArguments(getVariables(item.getStartParameters()));
        return history;
    }

    public static OrderHistoryStateItem map2OrderHistoryStateItem(DBItemHistoryOrderState item) {
        OrderHistoryStateItem history = new OrderHistoryStateItem();
        history.setStateTime(item.getStateTime());
        history.setStateText(item.getStateText());
        history.setState(OrdersHelper.getState(item.getStateAsEnum()));
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

    private static Variables getVariables(String json) {
        if (!SOSString.isEmpty(json)) {
            try {
                return Globals.objectMapper.readValue(json, Variables.class);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    public static HistoryState getState(Integer historySeverity) {
        HistoryState state = new HistoryState();

        OrderState os;
        switch (historySeverity.intValue()) {
        case HistorySeverity.SUCCESSFUL:
            state.set_text(HistoryStateText.SUCCESSFUL);

            os = OrdersHelper.getState(OrderStateText.FINISHED);
            state.setSeverity(os.getSeverity());
            break;
        case HistorySeverity.INCOMPLETE:
            state.set_text(HistoryStateText.INCOMPLETE);

            os = OrdersHelper.getState(OrderStateText.INPROGRESS);
            state.setSeverity(os.getSeverity());
            break;
        case HistorySeverity.FAILED:
            state.set_text(HistoryStateText.FAILED);

            os = OrdersHelper.getState(OrderStateText.FAILED);
            state.setSeverity(os.getSeverity());
            break;
        }
        return state;
    }
}
