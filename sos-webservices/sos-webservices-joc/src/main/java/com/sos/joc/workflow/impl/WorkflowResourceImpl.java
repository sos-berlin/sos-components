package com.sos.joc.workflow.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.controller.model.workflow.WorkflowId;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.workflow.WorkflowPaths;
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
                entity.setSurveyDate(Date.from(currentstate.instant()));
//                WorkflowControlState controlState = JavaConverters.asJava(currentstate.asScala().pathToWorkflowControlState_()).get(WorkflowPath.of(workflowPath));
//                if (controlState != null) {
//                    LOGGER.info(JavaConverters.asJava(controlState.attachedToAgents()).toString());
//                    LOGGER.info(controlState.workflowControl().suspended() + "");
//                    LOGGER.info(JavaConverters.asJava(currentstate.repo().idToCheckedWorkflow(JWorkflowId.of(JocInventory.pathToName(workflowPath), versionId))
//                            .get().asScala().nameToJob()).values().stream().map(j -> j.agentPath()).distinct().collect(Collectors.toList()).toString());
//                }
//                LOGGER.info(currentstate.repo().idToCheckedWorkflow(JWorkflowId.of(JocInventory.pathToName(workflowPath), versionId)).get().withPositions().toJson());
            }
            
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(connection);

            DeployedContent content = dbLayer.getDeployedInventory(controllerId, DeployType.WORKFLOW.intValue(), workflowPath, versionId);
            if (content != null && content.getContent() != null && !content.getContent().isEmpty()) {
                com.sos.controller.model.workflow.Workflow workflow = Globals.objectMapper.readValue(content.getContent(),
                        com.sos.controller.model.workflow.Workflow.class);
                String path = WorkflowPaths.getPath(content.getName());
                checkFolderPermissions(path, folderPermissions.getListOfFolders());
                workflow.setPath(path);
                workflow.setVersionDate(content.getCreated());
                workflow.setVersionId(content.getCommitId());
                workflow.setState(WorkflowsHelper.getState(currentstate, workflow));
                workflow.setSuspended(WorkflowsHelper.getSuspended(workflow.getState()));
                
                if (versionId == null || versionId.isEmpty()) {
                    workflow.setIsCurrentVersion(true);
                } else {
                    DeployedContent lastContent = dbLayer.getDeployedInventory(controllerId, DeployType.WORKFLOW.intValue(), workflowPath);
                    if (lastContent != null && lastContent.getCommitId() != null) {
                        workflow.setIsCurrentVersion(lastContent.getCommitId().equals(content.getCommitId()));
                    }
                }
                if (workflow.getIsCurrentVersion() && workflowFilter.getCompact() != Boolean.TRUE) {
                    workflow.setFileOrderSources(WorkflowsHelper.workflowToFileOrderSources(currentstate, controllerId, content.getName(), dbLayer));
                }
                List<WorkflowId> wIds = dbLayer.getAddOrderWorkflowsByWorkflow(JocInventory.pathToName(workflow.getPath()), controllerId);
                if (wIds != null && !wIds.isEmpty()) {
                    workflow.setHasAddOrderDependencies(true); 
                }
                
                Set<String> skippedLabels = WorkflowsHelper.getSkippedLabels(currentstate, content.getName(), workflowFilter
                        .getCompact() == Boolean.TRUE);
                workflow = WorkflowsHelper.addWorkflowPositionsAndForkListVariablesAndExpectedNoticeBoards(workflow, skippedLabels);
       
                if (workflowFilter.getCompact() == Boolean.TRUE) {
                    workflow.setFileOrderSources(null);
                    //workflow.setForkListVariables(null);
                    workflow.setInstructions(null);
                    workflow.setJobResourceNames(null);
                    workflow.setJobs(null);
                    //workflow.setOrderPreparation(null);
                } else if (workflow.getOrderPreparation() != null) {
                    workflow.setOrderPreparation(WorkflowsHelper.removeFinals(workflow));
                }
                entity.setWorkflow(workflow);
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

    private static JControllerState getCurrentState(String controllerId) {
        JControllerState currentstate = null;
        try {
            currentstate = Proxy.of(controllerId).currentState();
        } catch (Exception e) {
            LOGGER.warn(e.toString());
        }
        return currentstate;
    }

}
