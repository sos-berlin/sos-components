package com.sos.joc.workflow.impl;

import java.sql.Date;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.workflow.Workflow;
import com.sos.joc.model.workflow.WorkflowFilter;
import com.sos.joc.workflow.resource.IWorkflowResource;
import com.sos.js7.order.initiator.db.FilterDailyPlannedOrders;
import com.sos.schema.JsonValidator;

@Path("workflow")
public class WorkflowResourceImpl extends JOCResourceImpl implements IWorkflowResource {

    private static final String API_CALL = "./workflow";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;
    
    
    @Override
    public JOCDefaultResponse postWorkflow(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            JsonValidator.validateFailFast(filterBytes, WorkflowFilter.class);
            WorkflowFilter workflowFilter = Globals.objectMapper.readValue(filterBytes, WorkflowFilter.class);
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, workflowFilter, accessToken, workflowFilter.getJobschedulerId(),
                    getPermissonsJocCockpit(workflowFilter.getJobschedulerId(), accessToken).getOrder().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(connection);
            com.sos.jobscheduler.model.workflow.Workflow item = dbLayer.getDeployedInventory(workflowFilter);
            if (item == null) {
                throw new DBMissingDataException(String.format("Workflow '%s' doesn't exist", workflowFilter.getWorkflowId().getPath()));
            }
            
            FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
            filter.setControllerId(workflowFilter.getJobschedulerId());
            filter.setWorkflow(workflowFilter.getWorkflowId().getPath());
            filter.setDailyPlanDate(formatter.format(Instant.now()));

            
            Workflow workflow = new Workflow();
            workflow.setDeliveryDate(Date.from(Instant.now()));
            workflow.setWorkflow(item);

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsString(workflow));

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
    }

//    public JOCDefaultResponse postWorkflowVolatile(String accessToken, byte[] filterBytes) {
//        try {
//            JsonValidator.validateFailFast(filterBytes, OrderFilter.class);
//            WorkflowFilter workflowFilter = Globals.objectMapper.readValue(filterBytes, WorkflowFilter.class);
//            JOCDefaultResponse jocDefaultResponse = init(API_CALL, workflowFilter, accessToken, workflowFilter.getJobschedulerId(),
//                    getPermissonsJocCockpit(workflowFilter.getJobschedulerId(), accessToken).getOrder().getView().isStatus());
//            if (jocDefaultResponse != null) {
//                return jocDefaultResponse;
//            }
//
//            JControllerState currentState = Proxy.of(workflowFilter.getJobschedulerId()).currentState();
//            // Long surveyDateMillis = currentState.eventId() / 1000;
//            Either<Problem, JWorkflow> response = currentState.idToWorkflow(JWorkflowId.of(workflowFilter.getWorkflowId().getPath(), workflowFilter
//                    .getWorkflowId().getVersionId()));
//            ProblemHelper.throwProblemIfExist(response);
//
//            return JOCDefaultResponse.responseStatus200(response.get().toJson());
//
//        } catch (JocException e) {
//            e.addErrorMetaInfo(getJocError());
//            return JOCDefaultResponse.responseStatusJSError(e);
//        } catch (Exception e) {
//            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
//        }
//    }

}
