package com.sos.joc.workflow.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.deploy.items.DeployedContent;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.workflow.Workflow;
import com.sos.joc.model.workflow.WorkflowFilter;
import com.sos.joc.workflow.resource.IWorkflowResource;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.workflow.WorkflowPath;
import js7.proxy.javaapi.data.controller.JControllerState;
import js7.proxy.javaapi.data.workflow.JWorkflow;
import js7.proxy.javaapi.data.workflow.JWorkflowId;

@Path("workflow")
public class WorkflowResourceImpl extends JOCResourceImpl implements IWorkflowResource {

    private static final String API_CALL = "./workflow";
    
    
    @Override
    public JOCDefaultResponse postWorkflowPermanent(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, WorkflowFilter.class);
            WorkflowFilter workflowFilter = Globals.objectMapper.readValue(filterBytes, WorkflowFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions(workflowFilter.getControllerId(), getPermissonsJocCockpit(workflowFilter
                    .getControllerId(), accessToken).getWorkflow().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            // TODO folder permissions

            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(connection);
            Workflow entity = new Workflow();
            
            DeployedContent content = dbLayer.getDeployedInventory(workflowFilter.getControllerId(), DeployType.WORKFLOW.intValue(), workflowFilter
                    .getWorkflowId().getPath(), workflowFilter.getWorkflowId().getVersionId());
            if (content != null && content.getContent() != null && !content.getContent().isEmpty()) {
                com.sos.controller.model.workflow.Workflow workflow = Globals.objectMapper.readValue(content.getContent(),
                        com.sos.controller.model.workflow.Workflow.class);
                workflow.setPath(content.getPath());
                if (workflowFilter.getWorkflowId().getVersionId() == null || workflowFilter.getWorkflowId().getVersionId().isEmpty()) {
                    workflow.setIsCurrentVersion(true);
                } else {
                    DeployedContent lastContent = dbLayer.getDeployedInventory(workflowFilter.getControllerId(), DeployType.WORKFLOW.intValue(), workflowFilter
                            .getWorkflowId().getPath());
                    if (lastContent != null && lastContent.getCommitId() != null) {
                        workflow.setIsCurrentVersion(lastContent.getCommitId().equals(content.getCommitId()));
                    }
                }
                workflow = WorkflowsHelper.addWorkflowPositions(workflow);
                entity.setWorkflow(workflow);
            } else {
                throw new DBMissingDataException(String.format("Workflow '%s' doesn't exist", workflowFilter.getWorkflowId().getPath()));
            }
            
            entity.setDeliveryDate(Date.from(Instant.now()));

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsString(entity));

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
    }

    public JOCDefaultResponse postWorkflowVolatile(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL + "/v", filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, WorkflowFilter.class);
            WorkflowFilter workflowFilter = Globals.objectMapper.readValue(filterBytes, WorkflowFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions(workflowFilter.getControllerId(),
                    getPermissonsJocCockpit(workflowFilter.getControllerId(), accessToken).getWorkflow().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            // TODO folder permissions
            // nameToPath mapping if workflowFilter.getWorkflowId().getPath() is a name

            String workflowName = JocInventory.pathToName(workflowFilter.getWorkflowId().getPath());
            JControllerState currentState = Proxy.of(workflowFilter.getControllerId()).currentState();
            Long surveyDateMillis = currentState.eventId() / 1000;
            
            String versionId = workflowFilter.getWorkflowId().getVersionId();
            Either<Problem, JWorkflow> response = null;
            if (versionId == null) {
                response = currentState.pathToWorkflow(WorkflowPath.of(workflowName));
            } else {
                response = currentState.idToWorkflow(JWorkflowId.of(workflowName, versionId));
            }
            ProblemHelper.throwProblemIfExist(response);
            
            com.sos.controller.model.workflow.Workflow workflow = Globals.objectMapper.readValue(response.get().withPositions().toJson(),
                    com.sos.controller.model.workflow.Workflow.class);
            workflow.setIsCurrentVersion(WorkflowsHelper.isCurrentVersion(versionId, currentState));
            workflow.setPath(workflowFilter.getWorkflowId().getPath());
            
            Workflow entity = new Workflow();
            entity.setSurveyDate(Date.from(Instant.ofEpochMilli(surveyDateMillis)));
            entity.setDeliveryDate(Date.from(Instant.now()));
            entity.setWorkflow(workflow);
            
            return JOCDefaultResponse.responseStatus200(entity);

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
}
