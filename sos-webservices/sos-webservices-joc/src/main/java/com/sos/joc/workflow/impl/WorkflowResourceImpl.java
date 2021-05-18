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
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.deploy.items.DeployedContent;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.workflow.Workflow;
import com.sos.joc.model.workflow.WorkflowFilter;
import com.sos.joc.workflow.resource.IWorkflowResource;
import com.sos.schema.JsonValidator;

import js7.data_for_java.controller.JControllerState;

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
            JOCDefaultResponse jocDefaultResponse = initPermissions(controllerId, getControllerPermissions(controllerId, accessToken).getWorkflows()
                    .getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            String workflowPath = workflowFilter.getWorkflowId().getPath();
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
                checkFolderPermissions(content.getPath(), folderPermissions.getListOfFolders());
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
                if (workflow.getIsCurrentVersion()) {
                    workflow.setFileOrderSources(WorkflowsHelper.workflowToFileOrderSources(currentstate, controllerId, content.getPath(), dbLayer));
                }
                entity.setWorkflow(WorkflowsHelper.addWorkflowPositions(workflow));
            } else {
                throw new DBMissingDataException(String.format("Workflow '%s' doesn't exist", workflowPath));
            }

            entity.setDeliveryDate(Date.from(Instant.now()));

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
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
