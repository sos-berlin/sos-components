package com.sos.joc.workflows.impl;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.auth.rest.SOSShiroFolderPermissions;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.controller.model.fileordersource.FileOrderSource;
import com.sos.controller.model.workflow.Workflow;
import com.sos.controller.model.workflow.WorkflowId;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.deploy.DeployedConfigurationFilter;
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
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            workflows.setWorkflows(getWorkflows(workflowsFilter, new DeployedConfigurationDBLayer(connection), currentstate, folderPermissions,
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
            SOSShiroFolderPermissions folderPermissions, JocError jocError) {
        String controllerId = workflowsFilter.getControllerId();
        List<DeployedContent> contents = getPermanentDeployedContent(workflowsFilter, dbLayer, folderPermissions);
        if (currentstate != null) {
            contents.addAll(getOlderWorkflows(workflowsFilter, currentstate, dbLayer, folderPermissions));
        }

        List<WorkflowId> workflowIds = workflowsFilter.getWorkflowIds();
        if (workflowIds != null && !workflowIds.isEmpty()) {
            workflowsFilter.setFolders(null);
            workflowsFilter.setRegex(null);
        }

        Stream<DeployedContent> contentsStream = contents.parallelStream().distinct();

        boolean withoutFilter = (workflowsFilter.getFolders() == null || workflowsFilter.getFolders().isEmpty()) && (workflowsFilter
                .getWorkflowIds() == null || workflowsFilter.getWorkflowIds().isEmpty());
        if (withoutFilter) {
            Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
            contentsStream = contentsStream.filter(w -> canAdd(w.getPath(), permittedFolders));
        }
        if (workflowsFilter.getRegex() != null && !workflowsFilter.getRegex().isEmpty()) {
            Predicate<String> regex = Pattern.compile(workflowsFilter.getRegex().replaceAll("%", ".*"), Pattern.CASE_INSENSITIVE).asPredicate();
            contentsStream = contentsStream.filter(w -> regex.test(w.getName()) || regex.test(w.getTitle()));
        }
        
        Set<String> workflowNamesWithAddOrders = dbLayer.getAddOrderWorkflows(controllerId);

        Map<String, List<FileOrderSource>> fileOrderSources = (workflowsFilter.getCompact() == Boolean.TRUE) ? null : WorkflowsHelper
                .workflowToFileOrderSources(currentstate, controllerId, contents.stream().parallel().filter(DeployedContent::isCurrentVersion).map(
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

    private static List<DeployedContent> getPermanentDeployedContent(WorkflowsFilter workflowsFilter, DeployedConfigurationDBLayer dbLayer,
            SOSShiroFolderPermissions folderPermissions) {
        DeployedConfigurationFilter dbFilter = new DeployedConfigurationFilter();
        dbFilter.setControllerId(workflowsFilter.getControllerId());
        dbFilter.setObjectTypes(Arrays.asList(DeployType.WORKFLOW.intValue()));

        List<WorkflowId> workflowIds = workflowsFilter.getWorkflowIds();
        if (workflowIds != null && !workflowIds.isEmpty()) {
            workflowsFilter.setFolders(null);
            workflowsFilter.setRegex(null);
        }
        boolean withFolderFilter = workflowsFilter.getFolders() != null && !workflowsFilter.getFolders().isEmpty();
        final Set<Folder> folders = addPermittedFolder(workflowsFilter.getFolders(), folderPermissions);
        List<DeployedContent> contents = null;

        if (workflowIds != null && !workflowIds.isEmpty()) {
            ConcurrentMap<Boolean, Set<WorkflowId>> workflowMap = workflowIds.stream().parallel().filter(w -> canAdd(w.getPath(), folders)).collect(Collectors
                    .groupingByConcurrent(w -> w.getVersionId() != null && !w.getVersionId().isEmpty(), Collectors.toSet()));
            if (workflowMap.containsKey(true)) {  // with versionId
                dbFilter.setWorkflowIds(workflowMap.get(true));
                contents = dbLayer.getDeployedInventoryWithCommitIds(dbFilter);
                if (contents != null && !contents.isEmpty()) {

                    // TODO check if workflows known in controller

                    dbFilter.setWorkflowIds((Set<WorkflowId>) null);
                    dbFilter.setPaths(workflowMap.get(true).parallelStream().map(WorkflowId::getPath).collect(Collectors.toSet()));
                    List<DeployedContent> contents2 = dbLayer.getDeployedInventory(dbFilter);
                    if (contents2 != null && !contents2.isEmpty()) {
                        Set<String> commitIds = contents2.parallelStream().map(c -> c.getPath() + "," + c.getCommitId()).collect(Collectors.toSet());
                        contents = contents.parallelStream().map(c -> {
                            c.setIsCurrentVersion(commitIds.contains(c.getPath() + "," + c.getCommitId()));
                            return c;
                        }).collect(Collectors.toList());
                    }
                }
            }
            if (workflowMap.containsKey(false)) { // without versionId
                dbFilter.setPaths(workflowMap.get(false).stream().parallel().map(WorkflowId::getPath).collect(Collectors.toSet()));

                // TODO check if workflows known in controller

                if (contents == null) {
                    contents = dbLayer.getDeployedInventory(dbFilter);
                } else {
                    contents.addAll(dbLayer.getDeployedInventory(dbFilter));
                }
            }
        } else if (withFolderFilter && (folders == null || folders.isEmpty())) {
            // no folder permissions
        } else if (folders != null && !folders.isEmpty()) {
            dbFilter.setFolders(folders);
            contents = dbLayer.getDeployedInventory(dbFilter);
        } else {
            contents = dbLayer.getDeployedInventory(dbFilter);
        }
        if (contents == null) {
            return Collections.emptyList();
        }
        return contents;
    }

    private static List<DeployedContent> getOlderWorkflows(WorkflowsFilter workflowsFilter, JControllerState currentState,
            DeployedConfigurationDBLayer dbLayer, SOSShiroFolderPermissions folderPermissions) {

        List<WorkflowId> workflowIds = workflowsFilter.getWorkflowIds();
        final Set<Folder> folders = addPermittedFolder(workflowsFilter.getFolders(), folderPermissions);
        List<DeployedContent> contents = null;
        boolean withFolderFilter = workflowsFilter.getFolders() != null && !workflowsFilter.getFolders().isEmpty();

        if (workflowIds != null && !workflowIds.isEmpty()) {
            workflowsFilter.setRegex(null);
            // only permanent info
        } else if (withFolderFilter && (folders == null || folders.isEmpty())) {
            // no folder permissions
        } else {
            
            Set<WorkflowId> wIds = WorkflowsHelper.oldWorkflowIds(currentState).collect(Collectors.toSet());
            if (wIds == null || wIds.isEmpty()) {
                return Collections.emptyList();
            }
            
            DeployedConfigurationFilter dbFilter = new DeployedConfigurationFilter();
            dbFilter.setControllerId(workflowsFilter.getControllerId());
            dbFilter.setObjectTypes(Arrays.asList(DeployType.WORKFLOW.intValue()));
            dbFilter.setWorkflowIds(wIds);

            if (folders != null && !folders.isEmpty()) {
                dbFilter.setFolders(folders);
                contents = dbLayer.getDeployedInventoryWithCommitIds(dbFilter);
            } else {
                contents = dbLayer.getDeployedInventoryWithCommitIds(dbFilter);
            }
        }

        if (contents == null) {
            return Collections.emptyList();
        }

        return contents;
    }

}
