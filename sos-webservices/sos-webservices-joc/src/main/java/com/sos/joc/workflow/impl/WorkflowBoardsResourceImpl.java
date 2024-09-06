package com.sos.joc.workflow.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.controller.model.workflow.WorkflowIdAndTags;
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
import com.sos.joc.db.deploy.items.WorkflowBoards;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.workflow.Workflow;
import com.sos.joc.model.workflow.WorkflowFilter;
import com.sos.joc.model.workflow.WorkflowsFilter;
import com.sos.joc.workflow.resource.IWorkflowBoardsResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.workflow.position.JPosition;

@Path("workflow")
public class WorkflowBoardsResourceImpl extends JOCResourceImpl implements IWorkflowBoardsResource {

    private static final String API_CALL = "./workflow/dependencies";
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowBoardsResourceImpl.class);

    @Override
    public JOCDefaultResponse postWorkflowDependencies(String accessToken, byte[] filterBytes) {
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
            boolean compact = workflowFilter.getCompact() == Boolean.TRUE;
            
            Workflow entity = new Workflow();
            entity.setSurveyDate(Date.from(Instant.now()));
            final JControllerState currentstate = getCurrentState(controllerId);
            if (currentstate != null) {
                entity.setSurveyDate(Date.from(currentstate.instant()));
            }
            
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(connection);
            boolean withWorkflowTagsDisplayed = WorkflowsHelper.withWorkflowTagsDisplayed();

            DeployedContent content = dbLayer.getDeployedInventory(controllerId, DeployType.WORKFLOW.intValue(), workflowPath, versionId);
            if (content != null && content.getContent() != null && !content.getContent().isEmpty()) {
                com.sos.controller.model.workflow.WorkflowDeps workflow = Globals.objectMapper.readValue(content.getContent(),
                        com.sos.controller.model.workflow.WorkflowDeps.class);
                String path = WorkflowPaths.getPath(content.getName()); 
                checkFolderPermissions(path, folderPermissions.getListOfFolders());
                workflow.setPath(path);
                workflow.setVersionDate(content.getCreated());
                workflow.setVersionId(content.getCommitId());
                WorkflowsHelper.setStateAndSuspended(currentstate, workflow);
                
                if (versionId == null || versionId.isEmpty()) {
                    workflow.setIsCurrentVersion(true);
                } else {
                    DeployedContent lastContent = dbLayer.getDeployedInventory(controllerId, DeployType.WORKFLOW.intValue(), workflowPath);
                    if (lastContent != null && lastContent.getCommitId() != null) {
                        workflow.setIsCurrentVersion(lastContent.getCommitId().equals(content.getCommitId()));
                    }
                }
                if (workflow.getIsCurrentVersion() && !compact) {
                    workflow.setFileOrderSources(WorkflowsHelper.workflowToFileOrderSources(currentstate, controllerId, content.getName(), dbLayer));
                }
                
                Set<String> skippedLabels = WorkflowsHelper.getSkippedLabels(currentstate, content.getName(), compact);
                Set<JPosition> stoppedPositions = WorkflowsHelper.getStoppedPositions(currentstate, content.getName(), workflow.getVersionId(),
                        compact);
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
                
                if (withWorkflowTagsDisplayed) {
                    Map<String, LinkedHashSet<String>> wTags = WorkflowsHelper.getMapOfTagsPerWorkflow(connection, Stream.of(content.getName()));
                    workflow.setWorkflowTags(wTags.get(content.getName()));
                }
                
//                JocError jocError = getJocError();
                WorkflowsFilter f = new WorkflowsFilter();
                f.setControllerId(controllerId);
                f.setCompact(true);
//                final Set<Folder> folders = folderPermissions.getListOfFolders();
                Set<String> expectedAndConsumeNoticeBoards = Stream.concat(workflow.getExpectedNoticeBoards().getAdditionalProperties().keySet().stream(),
                        workflow.getConsumeNoticeBoards().getAdditionalProperties().keySet().stream()).collect(Collectors.toSet());
                Set<String> postNoticeBoards = workflow.getPostNoticeBoards().getAdditionalProperties().keySet().stream().collect(Collectors.toSet());
                workflow.getPostNoticeBoards().getAdditionalProperties().clear();
                workflow.getExpectedNoticeBoards().getAdditionalProperties().clear();
                workflow.getConsumeNoticeBoards().getAdditionalProperties().clear();
                
                List<WorkflowBoards> allWorkflowIdsWithBoards = dbLayer.getUsedWorkflowsByNoticeBoards(controllerId);
//                Set<WorkflowId> workflowsWithNotices = new HashSet<>();
                Map<String, Set<WorkflowIdAndTags>> workflowsWithPostNotices = new HashMap<>();
                Map<String, Set<WorkflowIdAndTags>> workflowsWithExpectedNotices = new HashMap<>();
                Map<String, Set<WorkflowIdAndTags>> workflowsWithConsumeNotices = new HashMap<>();
                
                
                for (WorkflowBoards wb : allWorkflowIdsWithBoards) {
                    List<String> pNotices = wb.getPostNotices();
                    List<String> eNotices = wb.getExpectNotices();
                    List<String> cNotices = wb.getConsumeNotices();
                    WorkflowIdAndTags wId = new WorkflowIdAndTags(null, wb.getPath(), wb.getVersionId());
                    if (pNotices != null && !pNotices.isEmpty()) {
                        pNotices.retainAll(expectedAndConsumeNoticeBoards);
//                        if (!pNotices.isEmpty()) {
                            for (String pNotice : pNotices) {
                                workflowsWithPostNotices.putIfAbsent(pNotice, new HashSet<>());
                                workflowsWithPostNotices.get(pNotice).add(wId);
                            }
//                            workflowsWithNotices.add(wId);
//                        }
                    }
                    if (eNotices != null && !eNotices.isEmpty()) {
                        eNotices.retainAll(postNoticeBoards);
//                        if (!eNotices.isEmpty()) {
                            for (String eNotice : eNotices) {
                                workflowsWithExpectedNotices.putIfAbsent(eNotice, new HashSet<>());
                                workflowsWithExpectedNotices.get(eNotice).add(wId);
                            }
//                            workflowsWithNotices.add(wId);
//                        }
                    }
                    if (cNotices != null && !cNotices.isEmpty()) {
                        cNotices.retainAll(postNoticeBoards);
//                        if (!cNotices.isEmpty()) {
                            for (String cNotice : cNotices) {
                                workflowsWithConsumeNotices.putIfAbsent(cNotice, new HashSet<>());
                                workflowsWithConsumeNotices.get(cNotice).add(wId);
                            }
//                            workflowsWithNotices.add(wId);
//                        }
                    }
                }
                
//                f.setWorkflowIds(new ArrayList<>(workflowsWithNotices));
//                workflowsWithNotices.clear();
                
//                if (f.getWorkflowIds() != null && !f.getWorkflowIds().isEmpty()) {
//                    List<com.sos.controller.model.workflow.Workflow> workflows = WorkflowsResourceImpl.getWorkflows(f, dbLayer, currentstate, folders,
//                            jocError);
//                }
                
                for (String boardName : postNoticeBoards) {
                    
                    Set<WorkflowIdAndTags> wIds = null;
                    
//                    f.setWorkflowIds(new ArrayList<>(workflowsWithExpectedNotices.getOrDefault(boardName, Collections.emptySet())));
                    wIds = workflowsWithExpectedNotices.get(boardName);
                    if (wIds != null && !wIds.isEmpty()) {
                        //workflow.getExpectedNoticeBoards().setAdditionalProperty(boardName, getWorkflowFromWorkflowId(wIds));
                        workflow.getExpectedNoticeBoards().setAdditionalProperty(boardName, new ArrayList<>(wIds));
                    }
//                    f.setWorkflowIds(dbLayer.getUsedWorkflowsByExpectedNoticeBoard(JocInventory.pathToName(boardName), controllerId));
//                    if (f.getWorkflowIds() != null && !f.getWorkflowIds().isEmpty()) {
//                        workflow.getExpectedNoticeBoards().setAdditionalProperty(boardName, WorkflowsResourceImpl.getWorkflows(f, dbLayer,
//                                currentstate, folders, jocError));
//                    }
//                    f.setWorkflowIds(new ArrayList<>(workflowsWithConsumeNotices.getOrDefault(boardName, Collections.emptySet())));
                    wIds = workflowsWithConsumeNotices.get(boardName);
                    if (wIds != null && !wIds.isEmpty()) {
                        //workflow.getConsumeNoticeBoards().setAdditionalProperty(boardName, getWorkflowFromWorkflowId(wIds));
                        workflow.getConsumeNoticeBoards().setAdditionalProperty(boardName, new ArrayList<>(wIds));
                    }
//                    f.setWorkflowIds(dbLayer.getUsedWorkflowsByConsumeNoticeBoard(JocInventory.pathToName(boardName), controllerId));
//                    if (f.getWorkflowIds() != null && !f.getWorkflowIds().isEmpty()) {
//                        workflow.getConsumeNoticeBoards().setAdditionalProperty(boardName, WorkflowsResourceImpl.getWorkflows(f, dbLayer,
//                                currentstate, folders, jocError));
//                    }
                }
                for (String boardName : expectedAndConsumeNoticeBoards) {
//                    f.setWorkflowIds(new ArrayList<>(workflowsWithPostNotices.getOrDefault(boardName, Collections.emptySet())));
                    Set<WorkflowIdAndTags> wIds = workflowsWithPostNotices.get(boardName);
                    if (wIds != null && !wIds.isEmpty()) {
                        //workflow.getPostNoticeBoards().setAdditionalProperty(boardName, getWorkflowFromWorkflowId(wIds));
                        workflow.getPostNoticeBoards().setAdditionalProperty(boardName, new ArrayList<>(wIds));
                    }
//                    f.setWorkflowIds(dbLayer.getUsedWorkflowsByPostNoticeBoard(JocInventory.pathToName(boardName), controllerId));
//                    if (f.getWorkflowIds() != null && !f.getWorkflowIds().isEmpty()) {
//                        workflow.getPostNoticeBoards().setAdditionalProperty(boardName, WorkflowsResourceImpl.getWorkflows(f, dbLayer, currentstate,
//                                folders, jocError));
//                    }
                }
//                f.setWorkflowIds(dbLayer.getAddOrderWorkflowsByWorkflow(JocInventory.pathToName(workflow.getPath()), controllerId));
                List<WorkflowIdAndTags> wIds2 = dbLayer.getAddOrderWorkflowsByWorkflow(JocInventory.pathToName(workflow.getPath()), controllerId);
                if (wIds2 != null && !wIds2.isEmpty()) {
                    if (withWorkflowTagsDisplayed) {
                        Map<String, LinkedHashSet<String>> wTags = WorkflowsHelper.getMapOfTagsPerWorkflow(connection, wIds2.stream().map(
                                WorkflowIdAndTags::getPath).map(JocInventory::pathToName));
                        if (!wTags.isEmpty()) {
                            wIds2 = wIds2.stream().peek(w -> w.setWorkflowTags(wTags.get(JocInventory.pathToName(w.getPath())))).collect(Collectors
                                    .toList());
                        }
                    }
//                    if (compact) {
                        //workflow.setAddOrderFromWorkflows(getWorkflowFromWorkflowId(wIds2));
                        workflow.setAddOrderFromWorkflows(wIds2);
//                    } else {
//                        workflow.setAddOrderFromWorkflows(WorkflowsResourceImpl.getWorkflows(f, dbLayer, currentstate, folders, jocError));
//                    }
                } else {
                    workflow.setAddOrderFromWorkflows(Collections.emptyList());
                }
                if (workflow.getAddOrderToWorkflows() != null && !workflow.getAddOrderToWorkflows().isEmpty()) {
//                    f.setWorkflowIds(dbLayer.getWorkflowsIds(workflow.getAddOrderToWorkflows().stream().map(w -> w.getPath()).distinct().collect(Collectors
//                            .toList()), controllerId));
                    wIds2 = dbLayer.getWorkflowsIds(workflow.getAddOrderToWorkflows().stream().map(w -> w.getPath()).distinct().collect(Collectors
                          .toList()), controllerId);
                    if (wIds2 != null && !wIds2.isEmpty()) {
                        if (withWorkflowTagsDisplayed) {
                            Map<String, LinkedHashSet<String>> wTags = WorkflowsHelper.getMapOfTagsPerWorkflow(connection, wIds2.stream().map(
                                    WorkflowIdAndTags::getPath).map(JocInventory::pathToName));
                            if (!wTags.isEmpty()) {
                                wIds2 = wIds2.stream().peek(w -> w.setWorkflowTags(wTags.get(JocInventory.pathToName(w.getPath())))).collect(Collectors
                                        .toList());
                            }
                        }
//                        if (compact) {
                            //workflow.setAddOrderToWorkflows(getWorkflowFromWorkflowId(wIds2));
                            workflow.setAddOrderToWorkflows(wIds2);
//                        } else {
//                            workflow.setAddOrderToWorkflows(WorkflowsResourceImpl.getWorkflows(f, dbLayer, currentstate, folders, jocError));
//                        }
                    }
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
    
//    private static List<com.sos.controller.model.workflow.Workflow> getWorkflowFromWorkflowId(Collection<WorkflowId> workflowIds) {
//        return workflowIds.stream().map(wId -> {
//            com.sos.controller.model.workflow.Workflow w = new com.sos.controller.model.workflow.Workflow();
//            w.setVersionId(wId.getVersionId());
//            w.setPath(wId.getPath());
//            w.setIsCurrentVersion(null);
//            w.setTimeZone(null);
//            w.setVersion(null);
//            w.setSuspended(null);
//            return w;
//        }).distinct().collect(Collectors.toList());
//    }

}
