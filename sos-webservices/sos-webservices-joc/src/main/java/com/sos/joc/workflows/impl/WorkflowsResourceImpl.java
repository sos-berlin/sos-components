package com.sos.joc.workflows.impl;

import java.time.Instant;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.controller.model.fileordersource.FileOrderSource;
import com.sos.controller.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.deploy.items.DeployedContent;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.workflow.Workflows;
import com.sos.joc.model.workflow.WorkflowsFilter;
import com.sos.joc.workflows.resource.IWorkflowsResource;
import com.sos.schema.JsonValidator;

import js7.data_for_java.controller.JControllerState;

@Path("workflows")
public class WorkflowsResourceImpl extends JOCResourceImpl implements IWorkflowsResource {

    private static final String API_CALL = "./workflows";
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowsResourceImpl.class);

    @Override
    public JOCDefaultResponse postWorkflowsPermanent(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, WorkflowsFilter.class);
            WorkflowsFilter workflowsFilter = Globals.objectMapper.readValue(filterBytes, WorkflowsFilter.class);
            String controllerId = workflowsFilter.getControllerId();
            JOCDefaultResponse jocDefaultResponse = initPermissions(controllerId, getControllerPermissions(controllerId, accessToken).getWorkflows()
                    .getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            Workflows workflows = new Workflows();
            workflows.setSurveyDate(Date.from(Instant.now()));
            final JControllerState currentstate = getCurrentState(controllerId);
            if (currentstate != null) {
                workflows.setSurveyDate(Date.from(currentstate.instant()));
            }
            final Set<Folder> folders = folderPermissions.getPermittedFolders(workflowsFilter.getFolders());
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            workflows.setWorkflows(getWorkflows(workflowsFilter, new DeployedConfigurationDBLayer(connection), currentstate, folders,
                    getJocError()));
            workflows.setDeliveryDate(Date.from(Instant.now()));

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(workflows));

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
    }

    public static List<Workflow> getWorkflows(WorkflowsFilter workflowsFilter, DeployedConfigurationDBLayer dbLayer, JControllerState currentstate,
            Set<Folder> permittedFolders, JocError jocError) {
        
        List<DeployedContent> contents = WorkflowsHelper.getDeployedContents(workflowsFilter, dbLayer, currentstate, permittedFolders);
        Stream<DeployedContent> contentsStream = WorkflowsHelper.getDeployedContentsStream(workflowsFilter, contents, permittedFolders);
        
        String controllerId = workflowsFilter.getControllerId();
        Set<String> workflowNamesWithAddOrders = dbLayer.getAddOrderWorkflows(controllerId);
        boolean withStatesFilter = workflowsFilter.getStates() != null && !workflowsFilter.getStates().isEmpty();

        Map<String, List<FileOrderSource>> fileOrderSources = (workflowsFilter.getCompact() == Boolean.TRUE) ? null : WorkflowsHelper
                .workflowToFileOrderSources(currentstate, controllerId, contents.parallelStream().filter(DeployedContent::isCurrentVersion).map(
                        w -> JocInventory.pathToName(w.getPath())).collect(Collectors.toSet()), dbLayer);
        return contentsStream.parallel().map(w -> {
            try {
                if (w.getContent() == null || w.getContent().isEmpty()) {
                    throw new DBMissingDataException("doesn't exist");
                }
                Workflow workflow = Globals.objectMapper.readValue(w.getContent(), Workflow.class);
                workflow.setPath(w.getPath());
                workflow.setVersionId(w.getCommitId());
                workflow.setIsCurrentVersion(w.isCurrentVersion());
                workflow.setVersionDate(w.getCreated());
                workflow.setState(WorkflowsHelper.getState(currentstate, workflow));
                if (withStatesFilter && !workflowsFilter.getStates().contains(workflow.getState().get_text())) {
                    return null;
                }
                workflow.setSuspended(WorkflowsHelper.getSuspended(workflow.getState()));
                if (workflow.getIsCurrentVersion() && fileOrderSources != null) {
                    workflow.setFileOrderSources(fileOrderSources.get(JocInventory.pathToName(w.getPath())));
                }
                if (workflowNamesWithAddOrders.contains(w.getName())) {
                    workflow.setHasAddOrderDependencies(true);
                }
                workflow = WorkflowsHelper.addWorkflowPositionsAndForkListVariablesAndExpectedNoticeBoards(workflow);
                if (workflowsFilter.getCompact() == Boolean.TRUE) {
                    workflow.setFileOrderSources(null);
                    //workflow.setForkListVariables(null);
                    workflow.setInstructions(null);
                    workflow.setJobResourceNames(null);
                    workflow.setJobs(null);
                    //workflow.setOrderPreparation(null);
                } else if (workflow.getOrderPreparation() != null) {
                    workflow.setOrderPreparation(WorkflowsHelper.removeFinals(workflow));
                }
                return workflow;
            } catch (Exception e) {
                if (jocError != null && !jocError.getMetaInfo().isEmpty()) {
                    LOGGER.info(jocError.printMetaInfo());
                    jocError.clearMetaInfo();
                }
                LOGGER.error(String.format("[%s] %s", w.getPath(), e.toString()));
                return null;
            }
        }).filter(Objects::nonNull).sorted(Comparator.comparing(Workflow::getVersionDate).reversed()).collect(Collectors.toList());
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
