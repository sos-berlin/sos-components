package com.sos.joc.workflow.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.WorkflowsHelper;
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
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.workflow.JWorkflow;
import js7.data_for_java.workflow.JWorkflowId;

@Path("workflow")
public class WorkflowResourceImpl extends JOCResourceImpl implements IWorkflowResource {

    private static final String API_CALL = "./workflow";
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowResourceImpl.class);

    @Override
    public JOCDefaultResponse postWorkflowPermanent(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, WorkflowFilter.class);
            WorkflowFilter workflowFilter = Globals.objectMapper.readValue(filterBytes, WorkflowFilter.class);
            String controllerId = workflowFilter.getControllerId();
            JOCDefaultResponse jocDefaultResponse = initPermissions(controllerId, getPermissonsJocCockpit(controllerId, accessToken).getWorkflow()
                    .getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            String workflowPath = workflowFilter.getWorkflowId().getPath();
            checkFolderPermissions(workflowPath, folderPermissions.getListOfFolders());
            String versionId = workflowFilter.getWorkflowId().getVersionId();
            
            Workflow entity = new Workflow();
            entity.setSurveyDate(Date.from(Instant.now()));
            final JControllerState currentstate = getCurrentState(controllerId);
            if (currentstate != null) {
                entity.setSurveyDate(Date.from(Instant.ofEpochMilli(currentstate.eventId() / 1000)));
            }
            
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(connection);

            DeployedContent content = dbLayer.getDeployedInventory(controllerId, DeployType.WORKFLOW.intValue(), workflowPath, versionId);
            if (content != null && content.getContent() != null && !content.getContent().isEmpty()) {
                com.sos.controller.model.workflow.Workflow workflow = Globals.objectMapper.readValue(content.getContent(),
                        com.sos.controller.model.workflow.Workflow.class);
                workflow.setPath(content.getPath());
                workflow.setVersionDate(content.getCreated());
                workflow.setState(WorkflowsHelper.getState(currentstate, workflow));
                
                if (versionId == null || versionId.isEmpty()) {
                    workflow.setIsCurrentVersion(true);
                } else {
                    DeployedContent lastContent = dbLayer.getDeployedInventory(controllerId, DeployType.WORKFLOW.intValue(), workflowPath);
                    if (lastContent != null && lastContent.getCommitId() != null) {
                        workflow.setIsCurrentVersion(lastContent.getCommitId().equals(content.getCommitId()));
                    }
                }
                entity.setWorkflow(WorkflowsHelper.addWorkflowPositions(workflow));
            } else {
                throw new DBMissingDataException(String.format("Workflow '%s' doesn't exist", workflowPath));
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
            JOCDefaultResponse jocDefaultResponse = initPermissions(workflowFilter.getControllerId(), getPermissonsJocCockpit(workflowFilter
                    .getControllerId(), accessToken).getWorkflow().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            // nameToPath mapping if workflowFilter.getWorkflowId().getPath() is a name
            String workflowPath = workflowFilter.getWorkflowId().getPath();
            if (workflowPath.contains("/")) {
                checkFolderPermissions(workflowPath, folderPermissions.getListOfFolders());
            }

            String workflowName = JocInventory.pathToName(workflowPath);
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
            workflow.setPath(workflowPath);

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
    
    private JControllerState getCurrentState(String controllerId) {
        JControllerState currentstate = null;
        try {
            currentstate = Proxy.of(controllerId).currentState();
        } catch (Exception e) {
            LOGGER.warn(e.toString());
        }
        return currentstate;
    }

}
