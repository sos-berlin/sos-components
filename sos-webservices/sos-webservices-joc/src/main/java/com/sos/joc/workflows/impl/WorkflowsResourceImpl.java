package com.sos.joc.workflows.impl;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.controller.model.common.SyncStateText;
import com.sos.controller.model.fileordersource.FileOrderSource;
import com.sos.controller.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.WorkflowConverter;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.workflow.WorkflowRefs;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.deploy.items.DeployedContent;
import com.sos.joc.db.inventory.InventoryNotesDBLayer;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.workflow.Workflows;
import com.sos.joc.model.workflow.WorkflowsFilter;
import com.sos.joc.model.workflow.search.InstructionStateText;
import com.sos.joc.workflows.resource.IWorkflowsResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.workflow.position.JPosition;

@Path("workflows")
public class WorkflowsResourceImpl extends JOCResourceImpl implements IWorkflowsResource {

    private static final String API_CALL = "./workflows";
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowsResourceImpl.class);

    @Override
    public JOCDefaultResponse postWorkflowsPermanent(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            filterBytes = initLogging(API_CALL, filterBytes, accessToken, CategoryType.CONTROLLER);
            JsonValidator.validateFailFast(filterBytes, WorkflowsFilter.class);
            WorkflowsFilter workflowsFilter = Globals.objectMapper.readValue(filterBytes, WorkflowsFilter.class);
            String controllerId = workflowsFilter.getControllerId();
            JOCDefaultResponse jocDefaultResponse = initPermissions(controllerId, getBasicControllerPermissions(controllerId, accessToken)
                    .getWorkflows().getView());
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
            if (WorkflowsHelper.withWorkflowTagsDisplayed()) {
                List<Workflow> ws = getWorkflows(workflowsFilter, new DeployedConfigurationDBLayer(connection), currentstate, folders, getJocError());
                Map<String, LinkedHashSet<String>> wTags = WorkflowsHelper.getMapOfTagsPerWorkflow(connection, ws.stream().map(Workflow::getPath).map(
                        JocInventory::pathToName));
                if (!wTags.isEmpty()) {
                    workflows.setWorkflows(ws.stream().peek(w -> w.setWorkflowTags(wTags.get(JocInventory.pathToName(w.getPath())))).collect(
                            Collectors.toList()));
                } else {
                    workflows.setWorkflows(ws);
                }
            } else {
                workflows.setWorkflows(getWorkflows(workflowsFilter, new DeployedConfigurationDBLayer(connection), currentstate, folders,
                        getJocError()));
            }
            workflows.setDeliveryDate(Date.from(Instant.now()));

            return responseStatus200(Globals.objectMapper.writeValueAsBytes(workflows));

        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(connection);
        }
    }

    public static List<Workflow> getWorkflows(WorkflowsFilter workflowsFilter, DeployedConfigurationDBLayer dbLayer, JControllerState currentstate,
            Set<Folder> permittedFolders, JocError jocError) {
        boolean compact = workflowsFilter.getCompact() == Boolean.TRUE;
        String controllerId = workflowsFilter.getControllerId();
        
        List<DeployedContent> contents = WorkflowsHelper.getDeployedContents(workflowsFilter, dbLayer, currentstate, permittedFolders).collect(
                Collectors.toList());

        Map<String, List<FileOrderSource>> fileOrderSources = (compact) ? null : WorkflowsHelper.workflowToFileOrderSources(currentstate,
                controllerId, contents.stream().filter(DeployedContent::isCurrentVersion).map(DeployedContent::getPath).map(JocInventory::pathToName)
                        .collect(Collectors.toSet()), dbLayer);
        
        Set<String> workflowNotes = new InventoryNotesDBLayer(dbLayer.getSession()).hasNote(ConfigurationType.WORKFLOW.intValue());
        
        // TODO should be permantly stored and updated by events
        //Set<String> workflowNamesWithAddOrders = dbLayer.getAddOrderWorkflows(controllerId);
        Set<String> workflowNamesWithAddOrders = WorkflowRefs.getWorkflowNamesWithAddOrders(controllerId);
        boolean withStatesFilter = withStatesFilter(workflowsFilter.getStates());
        if (workflowsFilter.getInstructionStates() == null) {
            workflowsFilter.setInstructionStates(Collections.emptyList());
        }
        boolean withSkippedInstructionStateFilter = workflowsFilter.getInstructionStates().contains(InstructionStateText.SKIPPED);
        boolean withStoppedInstructionStateFilter = workflowsFilter.getInstructionStates().contains(InstructionStateText.STOPPED);

        return WorkflowsHelper.getDeployedContentsStream(workflowsFilter, dbLayer, currentstate, contents,
                permittedFolders).map(w -> {
            try {
                if (w.getContent() == null || w.getContent().isEmpty()) {
                    throw new DBMissingDataException("doesn't exist");
                }
                Workflow workflow = WorkflowConverter.convertInventoryWorkflow(w.getContent(), Workflow.class);
                workflow.setPath(w.getPath());
                workflow.setVersionId(w.getCommitId());
                workflow.setIsCurrentVersion(w.isCurrentVersion());
                workflow.setVersionDate(w.getCreated());
                workflow.setHasNote(workflowNotes.contains(w.getName()) ? true : null);
                WorkflowsHelper.setStateAndSuspended(currentstate, workflow);
                if (withStatesFilter && !workflowsFilter.getStates().contains(workflow.getState().get_text())) {
                    return null;
                }
                if (withSkippedInstructionStateFilter && withStoppedInstructionStateFilter) {
                    if (workflow.getNumOfSkippedInstructions() + workflow.getNumOfStoppedInstructions() == 0) {
                        return null;
                    }
                } else if (withSkippedInstructionStateFilter) {
                    if (workflow.getNumOfSkippedInstructions() == 0) {
                        return null;
                    }
                } else if (withStoppedInstructionStateFilter) {
                    if (workflow.getNumOfStoppedInstructions() == 0) {
                        return null;
                    }
                }
                if (workflow.getIsCurrentVersion() && fileOrderSources != null) {
                    workflow.setFileOrderSources(fileOrderSources.get(w.getName()));
                }
                if (workflowNamesWithAddOrders.contains(w.getName())) {
                    workflow.setHasAddOrderDependencies(true);
                }
                Set<String> skippedLabels = WorkflowsHelper.getSkippedLabels(currentstate, w.getName(), compact);
                Set<JPosition> stoppedPositions = WorkflowsHelper.getStoppedPositions(currentstate, w.getName(), workflow.getVersionId(), compact);
                workflow = WorkflowsHelper.addWorkflowPositionsAndForkListVariablesAndExpectedNoticeBoards(workflow, skippedLabels, stoppedPositions);

                if (compact) {
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
    
    private static boolean withStatesFilter(Collection<SyncStateText> filterStates) {
        boolean withStatesFilter = filterStates != null && !filterStates.isEmpty();
        if (withStatesFilter) {
            EnumSet<SyncStateText> allStates = EnumSet.allOf(SyncStateText.class);
            allStates.remove(SyncStateText.NOT_DEPLOYED);
            allStates.remove(SyncStateText.UNKNOWN);
            withStatesFilter = allStates.size() != filterStates.stream().distinct().mapToInt(e -> 1).sum();
        }
        return withStatesFilter;
    }

}
