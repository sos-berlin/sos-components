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
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.controller.model.workflow.Workflow;
import com.sos.controller.model.workflow.WorkflowId;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
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
            
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(connection);

            List<DeployedContent> contents = getPermanentDeployedContent(workflowsFilter, dbLayer);
            if (currentstate != null) {
                workflows.setSurveyDate(Date.from(Instant.ofEpochMilli(currentstate.eventId() / 1000)));
                contents.addAll(getOlderWorkflows(workflowsFilter, currentstate, dbLayer));
            }
            
            List<WorkflowId> workflowIds = workflowsFilter.getWorkflowIds();
            if (workflowIds != null && !workflowIds.isEmpty()) {
                workflowsFilter.setFolders(null);
                workflowsFilter.setRegex(null);
            }

            Stream<DeployedContent> contentsStream = contents.stream().sorted(Comparator.comparing(DeployedContent::getCreated).reversed())
                    .distinct();
            
            boolean withoutFilter = (workflowsFilter.getFolders() == null || workflowsFilter.getFolders().isEmpty()) && (workflowsFilter
                    .getWorkflowIds() == null || workflowsFilter.getWorkflowIds().isEmpty());
            if (withoutFilter) {
                Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
                contentsStream.filter(w -> canAdd(w.getPath(), permittedFolders));
            }
            if (workflowsFilter.getRegex() != null && !workflowsFilter.getRegex().isEmpty()) {
                Predicate<String> regex = Pattern.compile(workflowsFilter.getRegex().replaceAll("%", ".*")).asPredicate();
                contentsStream = contentsStream.filter(w -> regex.test(w.getPath()));
            }
            JocError jocError = getJocError();
            workflows.setWorkflows(contentsStream.map(w -> {
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
                    return WorkflowsHelper.addWorkflowPositions(workflow);
                } catch (Exception e) {
                    if (jocError != null && !jocError.getMetaInfo().isEmpty()) {
                        LOGGER.info(jocError.printMetaInfo());
                        jocError.clearMetaInfo();
                    }
                    LOGGER.error(String.format("[%s] %s", w.getPath(), e.toString()));
                    return null;
                }
            }).filter(Objects::nonNull).collect(Collectors.toList()));
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

    
    private JControllerState getCurrentState(String controllerId) {
        JControllerState currentstate = null;
        try {
            currentstate = Proxy.of(controllerId).currentState();
        } catch (Exception e) {
            LOGGER.warn(e.toString());
        }
        return currentstate;
    }

    private List<DeployedContent> getPermanentDeployedContent(WorkflowsFilter workflowsFilter, DeployedConfigurationDBLayer dbLayer) {
        DeployedConfigurationFilter dbFilter = new DeployedConfigurationFilter();
        dbFilter.setControllerId(workflowsFilter.getControllerId());
        dbFilter.setObjectTypes(Arrays.asList(DeployType.WORKFLOW.intValue()));

        List<WorkflowId> workflowIds = workflowsFilter.getWorkflowIds();
        if (workflowIds != null && !workflowIds.isEmpty()) {
            workflowsFilter.setFolders(null);
            workflowsFilter.setRegex(null);
        }
        boolean withFolderFilter = workflowsFilter.getFolders() != null && !workflowsFilter.getFolders().isEmpty();
        final Set<Folder> folders = addPermittedFolder(workflowsFilter.getFolders());
        List<DeployedContent> contents = null;

        if (workflowIds != null && !workflowIds.isEmpty()) {
            Map<Boolean, Set<WorkflowId>> workflowMap = workflowIds.stream().filter(w -> canAdd(w.getPath(), folders)).collect(Collectors.groupingBy(
                    w -> w.getVersionId() != null && !w.getVersionId().isEmpty(), Collectors.toSet()));
            if (workflowMap.containsKey(true)) {  // with versionId
                dbFilter.setWorkflowIds(workflowMap.get(true));
                contents = dbLayer.getDeployedInventoryWithCommitIds(dbFilter);
                if (contents != null && !contents.isEmpty()) {

                    // TODO check if workflows known in controller

                    dbFilter.setWorkflowIds((Set<WorkflowId>) null);
                    dbFilter.setPaths(workflowMap.get(true).stream().map(WorkflowId::getPath).collect(Collectors.toSet()));
                    List<DeployedContent> contents2 = dbLayer.getDeployedInventory(dbFilter);
                    if (contents2 != null && !contents2.isEmpty()) {
                        Set<String> commitIds = contents2.stream().map(c -> c.getPath() + "," + c.getCommitId()).collect(Collectors.toSet());
                        contents = contents.stream().map(c -> {
                            c.setIsCurrentVersion(commitIds.contains(c.getPath() + "," + c.getCommitId()));
                            return c;
                        }).collect(Collectors.toList());
                    }
                }
            }
            if (workflowMap.containsKey(false)) { // without versionId
                dbFilter.setPaths(workflowMap.get(false).stream().map(WorkflowId::getPath).collect(Collectors.toSet()));

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
    
    private List<DeployedContent> getOlderWorkflows(WorkflowsFilter workflowsFilter, JControllerState currentState,
            DeployedConfigurationDBLayer dbLayer) {

        Set<WorkflowId> wIds = WorkflowsHelper.oldWorkflowIds(currentState).collect(Collectors.toSet());
        if (wIds == null || wIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<WorkflowId> workflowIds = workflowsFilter.getWorkflowIds();
        final Set<Folder> folders = addPermittedFolder(workflowsFilter.getFolders());
        List<DeployedContent> contents = null;
        boolean withFolderFilter = workflowsFilter.getFolders() != null && !workflowsFilter.getFolders().isEmpty();

        if (workflowIds != null && !workflowIds.isEmpty()) {
            workflowsFilter.setRegex(null);
            // only permanent info
        }
        if (withFolderFilter && (folders == null || folders.isEmpty())) {
            // no folder permissions
        } else {

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
