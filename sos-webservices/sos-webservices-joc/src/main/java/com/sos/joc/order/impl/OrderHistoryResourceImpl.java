package com.sos.joc.order.impl;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.jobscheduler.db.history.DBItemOrderStep;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.history.HistoryFilter;
import com.sos.joc.db.history.JobHistoryDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Err;
import com.sos.joc.model.common.HistoryState;
import com.sos.joc.model.common.HistoryStateText;
import com.sos.joc.model.job.TaskHistory;
import com.sos.joc.model.job.TaskHistoryItem;
import com.sos.joc.model.order.OrderHistoryFilter;
import com.sos.joc.order.resource.IOrderHistoryResource;
import com.sos.schema.JsonValidator;

@Path("order")
public class OrderHistoryResourceImpl extends JOCResourceImpl implements IOrderHistoryResource {

	private static final String API_CALL = "./order/history";

	@Override
	public JOCDefaultResponse postOrderHistory(String accessToken, byte[] filterBytes) {
		SOSHibernateSession connection = null;
		try {
		    JsonValidator.validateFailFast(filterBytes, OrderHistoryFilter.class);
		    OrderHistoryFilter orderHistoryFilter = Globals.objectMapper.readValue(filterBytes, OrderHistoryFilter.class);
            
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, orderHistoryFilter, accessToken,
					orderHistoryFilter.getJobschedulerId(),
					getPermissonsJocCockpit(orderHistoryFilter.getJobschedulerId(), accessToken).getOrder().getView()
							.isStatus());
			if (jocDefaultResponse != null) {
				return jocDefaultResponse;
			}
			
			checkRequiredParameter("historyId", orderHistoryFilter.getHistoryId());
			
			HistoryFilter historyFilter = new HistoryFilter();
            historyFilter.setSchedulerId(orderHistoryFilter.getJobschedulerId());
            historyFilter.setHistoryIds(Arrays.asList(orderHistoryFilter.getHistoryId()));
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            JobHistoryDBLayer jobHistoryDbLayer = new JobHistoryDBLayer(connection, historyFilter);
            List<DBItemOrderStep> dbOrderStepItems = jobHistoryDbLayer.getOrderSteps();
            
            TaskHistory entity = new TaskHistory();
            entity.setHistory(dbOrderStepItems.stream().map(dbItemOrderStep -> {
                TaskHistoryItem taskHistoryItem = new TaskHistoryItem();
                taskHistoryItem.setJobschedulerId(dbItemOrderStep.getJobSchedulerId());
                taskHistoryItem.setAgentUrl(dbItemOrderStep.getAgentUri());
                taskHistoryItem.setStartTime(dbItemOrderStep.getStartTime());
                taskHistoryItem.setEndTime(dbItemOrderStep.getEndTime());
                taskHistoryItem.setError(setError(dbItemOrderStep));
                taskHistoryItem.setJob(dbItemOrderStep.getJobName());
                taskHistoryItem.setOrderId(dbItemOrderStep.getOrderKey());
                taskHistoryItem.setExitCode(dbItemOrderStep.getReturnCode().intValue());
                taskHistoryItem.setState(setState(dbItemOrderStep));
                taskHistoryItem.setCriticality(dbItemOrderStep.getCriticality());
                taskHistoryItem.setSurveyDate(dbItemOrderStep.getModified());
                taskHistoryItem.setTaskId(dbItemOrderStep.getId());
                taskHistoryItem.setWorkflow(dbItemOrderStep.getWorkflowPath());
                taskHistoryItem.setPosition(dbItemOrderStep.getWorkflowPosition());
                return taskHistoryItem;
            }).collect(Collectors.toList()));
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
	
	private HistoryState setState(DBItemOrderStep dbItemOrderStep) {
        HistoryState state = new HistoryState();
        if (dbItemOrderStep.isSuccessFul()) {
            state.setSeverity(0);
            state.set_text(HistoryStateText.SUCCESSFUL);
        } else if (dbItemOrderStep.isInComplete()) {
            state.setSeverity(1);
            state.set_text(HistoryStateText.INCOMPLETE);
        } else if (dbItemOrderStep.isFailed()) {
            state.setSeverity(2);
            state.set_text(HistoryStateText.FAILED);
        }
        return state;
    }
    
    private Err setError(DBItemOrderStep dbItemOrderStep) {
        if (dbItemOrderStep.getError()) {
            Err error = new Err();
            //TODO maybe use dbItemOrderStep.getErrorState()
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
