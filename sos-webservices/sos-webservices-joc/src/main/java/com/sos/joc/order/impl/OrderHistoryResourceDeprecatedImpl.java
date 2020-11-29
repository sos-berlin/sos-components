package com.sos.joc.order.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.db.history.DBItemHistoryOrderStep;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.history.JobHistoryDBLayer;
import com.sos.joc.db.history.common.HistorySeverity;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Err;
import com.sos.joc.model.common.HistoryState;
import com.sos.joc.model.common.HistoryStateText;
import com.sos.joc.model.job.TaskHistory;
import com.sos.joc.model.job.TaskHistoryItem;
import com.sos.joc.model.order.OrderHistoryFilter;
import com.sos.joc.order.resource.IOrderHistoryResourceDeprecated;
import com.sos.schema.JsonValidator;

/** will be replaced by OrderHistoryResourceImpl */
@Path("order")
public class OrderHistoryResourceDeprecatedImpl extends JOCResourceImpl implements IOrderHistoryResourceDeprecated {

    private static final String API_CALL = "./order/history";

    @Override
    public JOCDefaultResponse postOrderHistory(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, OrderHistoryFilter.class);
            OrderHistoryFilter orderHistoryFilter = Globals.objectMapper.readValue(filterBytes, OrderHistoryFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions(orderHistoryFilter.getControllerId(), getPermissonsJocCockpit(orderHistoryFilter
                    .getControllerId(), accessToken).getOrder().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            checkRequiredParameter("historyId", orderHistoryFilter.getHistoryId());

            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            JobHistoryDBLayer jobHistoryDbLayer = new JobHistoryDBLayer(connection);
            List<DBItemHistoryOrderStep> dbOrderStepItems = jobHistoryDbLayer.getOrderSteps(orderHistoryFilter.getHistoryId());

            TaskHistory entity = new TaskHistory();
            if (dbOrderStepItems != null) {
                entity.setHistory(dbOrderStepItems.stream().map(dbItemOrderStep -> {
                    TaskHistoryItem taskHistoryItem = new TaskHistoryItem();
                    taskHistoryItem.setControllerId(dbItemOrderStep.getJobSchedulerId());
                    taskHistoryItem.setAgentUrl(dbItemOrderStep.getAgentUri());
                    taskHistoryItem.setStartTime(dbItemOrderStep.getStartTime());
                    taskHistoryItem.setEndTime(dbItemOrderStep.getEndTime());
                    taskHistoryItem.setError(setError(dbItemOrderStep));
                    taskHistoryItem.setJob(dbItemOrderStep.getJobName());
                    taskHistoryItem.setOrderId(dbItemOrderStep.getOrderKey());
                    taskHistoryItem.setExitCode(dbItemOrderStep.getReturnCode());
                    taskHistoryItem.setState(getState(dbItemOrderStep.getSeverity()));
                    taskHistoryItem.setCriticality(dbItemOrderStep.getCriticalityAsEnum().value().toLowerCase());
                    taskHistoryItem.setSurveyDate(dbItemOrderStep.getModified());
                    taskHistoryItem.setTaskId(dbItemOrderStep.getId());
                    taskHistoryItem.setWorkflow(dbItemOrderStep.getWorkflowPath());
                    taskHistoryItem.setPosition(dbItemOrderStep.getWorkflowPosition());
                    return taskHistoryItem;
                }).collect(Collectors.toList()));
            }
            entity.setDeliveryDate(Date.from(Instant.now()));

            return JOCDefaultResponse.responseStatus200(entity);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
    }

    private HistoryState getState(Integer severity) {
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

    private Err setError(DBItemHistoryOrderStep dbItemOrderStep) {
        if (dbItemOrderStep.getError()) {
            Err error = new Err();
            // TODO maybe use dbItemOrderStep.getErrorState()
            error.setCode(dbItemOrderStep.getErrorCode());
            if (dbItemOrderStep.getErrorText() != null && dbItemOrderStep.getErrorText().isEmpty()) {
                error.setMessage(dbItemOrderStep.getErrorText());
            } else {
                error.setMessage(dbItemOrderStep.getErrorReason());
            }
            return error;
        }
        return null;
    }
}
