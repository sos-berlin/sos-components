package com.sos.joc.workflows.impl;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.controller.model.workflow.Workflow;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.WorkflowsHelper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.deploy.DeployedConfigurationFilter;
import com.sos.joc.db.deploy.items.DeployedContent;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.workflow.WorkflowId;
import com.sos.joc.model.workflow.Workflows;
import com.sos.joc.model.workflow.WorkflowsFilter;
import com.sos.joc.workflows.resource.IWorkflowsResource;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.workflow.WorkflowPath;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.workflow.JWorkflow;
import js7.data_for_java.workflow.JWorkflowId;

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
            JOCDefaultResponse jocDefaultResponse = initPermissions(controllerId, getPermissonsJocCockpit(controllerId, accessToken).getWorkflow()
                    .getView().isStatus());
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

            Stream<DeployedContent> contentsStream = contents.stream();
            if (workflowsFilter.getRegex() != null && !workflowsFilter.getRegex().isEmpty()) {
                Predicate<String> regex = Pattern.compile(workflowsFilter.getRegex().replaceAll("%", ".*")).asPredicate();
                contentsStream = contentsStream.filter(w -> regex.test(w.getPath()));
            }
            workflows.setWorkflows(contentsStream.map(w -> {
                try {
                    Workflow workflow = Globals.objectMapper.readValue(w.getContent(), Workflow.class);
                    workflow.setPath(w.getPath());
                    workflow.setIsCurrentVersion(w.isCurrentVersion());
                    workflow.setState(WorkflowsHelper.getState(currentstate, workflow));
                    return WorkflowsHelper.addWorkflowPositions(workflow);
                } catch (Exception e) {
                    // TODO
                    return null;
                }
            }).filter(Objects::nonNull).distinct().collect(Collectors.toList()));
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

    @Override
    public JOCDefaultResponse postWorkflowsVolatile(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL + "/v", filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, WorkflowsFilter.class);
            WorkflowsFilter workflowsFilter = Globals.objectMapper.readValue(filterBytes, WorkflowsFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions(workflowsFilter.getControllerId(), getPermissonsJocCockpit(workflowsFilter
                    .getControllerId(), accessToken).getWorkflow().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            JControllerState currentState = Proxy.of(workflowsFilter.getControllerId()).currentState();
            Long surveyDateMillis = currentState.eventId() / 1000;
            Stream<DeployedContent> contentsStream = getVolatileDeployedContent(workflowsFilter, currentState);

            boolean withRegex = workflowsFilter.getRegex() != null && !workflowsFilter.getRegex().isEmpty();
            Predicate<String> regex = withRegex ? Pattern.compile(workflowsFilter.getRegex().replaceAll("%", ".*")).asPredicate() : s -> true;

            Workflows workflows = new Workflows();
            workflows.setWorkflows(contentsStream.map(c -> {
                try {
                    Workflow workflow = Globals.objectMapper.readValue(c.getContent(), Workflow.class);
                    workflow.setPath(c.getPath());
                    workflow.setIsCurrentVersion(c.isCurrentVersion());
                    return workflow;
                } catch (Exception e) {
                    // TODO
                    return null;
                }
            }).filter(Objects::nonNull).filter(w -> regex.test(w.getPath())).collect(Collectors.toList()));
            workflows.setSurveyDate(Date.from(Instant.ofEpochMilli(surveyDateMillis)));
            workflows.setDeliveryDate(Date.from(Instant.now()));

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(workflows));

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

    private Stream<DeployedContent> getVolatileDeployedContent(WorkflowsFilter workflowsFilter, JControllerState currentState)
            throws SOSHibernateException {
        SOSHibernateSession connection = null;
        try {

            List<WorkflowId> workflowIds = workflowsFilter.getWorkflowIds();
            final Set<Folder> folders = addPermittedFolder(workflowsFilter.getFolders());
            List<DeployedContent> contents = null;

            if (workflowIds != null && !workflowIds.isEmpty()) {
                workflowsFilter.setRegex(null);
                return workflowIds.stream().filter(w -> canAdd(w.getPath(), folders)).map(w -> {
                    Either<Problem, JWorkflow> e = null;
                    Boolean isCurrentVersion = null;
                    if (w.getVersionId() != null) {
                        e = currentState.idToWorkflow(JWorkflowId.of(JocInventory.pathToName(w.getPath()), w.getVersionId()));
                    } else {
                        e = currentState.pathToWorkflow(WorkflowPath.of(JocInventory.pathToName(w.getPath())));
                        isCurrentVersion = true;
                    }
                    if (e != null && e.isRight()) {
                        if (isCurrentVersion == null) {
                            Either<Problem, JWorkflow> e2 = currentState.pathToWorkflow(WorkflowPath.of(JocInventory.pathToName(w.getPath())));
                            isCurrentVersion = e2.get().id().versionId().equals(e.get().id().versionId());
                        }
                        return new DeployedContent(w.getPath(), e.get().withPositions().toJson(), w.getVersionId(), isCurrentVersion);
                    }
                    return null;
                }).filter(Objects::nonNull);

            } else {
                DeployedConfigurationFilter dbFilter = new DeployedConfigurationFilter();
                dbFilter.setControllerId(workflowsFilter.getControllerId());
                dbFilter.setObjectTypes(Arrays.asList(DeployType.WORKFLOW.intValue()));

                boolean withFolderFilter = workflowsFilter.getFolders() != null && !workflowsFilter.getFolders().isEmpty();
                connection = Globals.createSosHibernateStatelessConnection(API_CALL + "/v");
                DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(connection);

                if (withFolderFilter && (folders == null || folders.isEmpty())) {
                    // no folder permissions
                } else if (folders != null && !folders.isEmpty()) {
                    dbFilter.setFolders(folders);
                    contents = dbLayer.getDeployedInventory(dbFilter);

                    Set<WorkflowId> wIds = WorkflowsHelper.oldWorkflowIds(currentState).collect(Collectors.toSet());
                    if (wIds != null && !wIds.isEmpty()) {
                        dbFilter.setWorkflowIds(wIds);
                        Map<WorkflowId, String> namePathMap = dbLayer.getNamePathMappingWithCommitIds(dbFilter);
                        Stream<DeployedContent> oldWorkflows = wIds.stream().filter(wId -> namePathMap.containsKey(wId)).map(wId -> {
                            Either<Problem, JWorkflow> e = currentState.idToWorkflow(JWorkflowId.of(wId.getPath(), wId.getVersionId()));
                            if (e.isRight() && namePathMap.get(wId) != null) {
                                return new DeployedContent(namePathMap.get(wId), e.get().withPositions().toJson(), e.get().id().versionId().string(),
                                        false);
                            }
                            return null;
                        }).filter(Objects::nonNull);

                        if (contents == null) {
                            return oldWorkflows;
                        } else {
                            return Stream.concat(addWorkflowPositions(contents, currentState), oldWorkflows);
                        }
                    }
                    return addWorkflowPositions(contents, currentState);
                } else {
                    contents = dbLayer.getDeployedInventory(dbFilter);

                    Set<WorkflowId> wIds = WorkflowsHelper.oldWorkflowIds(currentState).collect(Collectors.toSet());
                    if (wIds != null && !wIds.isEmpty()) {
                        dbFilter.setWorkflowIds(wIds);
                        Map<WorkflowId, String> namePathMap = dbLayer.getNamePathMappingWithCommitIds(dbFilter);
                        Stream<DeployedContent> oldWorkflows = wIds.stream().map(wId -> {
                            Either<Problem, JWorkflow> e = currentState.idToWorkflow(JWorkflowId.of(wId.getPath(), wId.getVersionId()));
                            if (e.isRight() && namePathMap.get(wId) != null) {
                                return new DeployedContent(namePathMap.get(wId), e.get().withPositions().toJson(), e.get().id().versionId().string(),
                                        false);
                            }
                            return null;
                        }).filter(Objects::nonNull);

                        if (contents == null) {
                            return oldWorkflows;
                        } else {
                            return Stream.concat(addWorkflowPositions(contents, currentState), oldWorkflows);
                        }
                    }
                    return addWorkflowPositions(contents, currentState);
                }
            }

            return Stream.empty();
        } finally {
            Globals.disconnect(connection);
        }
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

    private Stream<DeployedContent> addWorkflowPositions(Collection<DeployedContent> contents, JControllerState currentState) {
        if (contents != null && !contents.isEmpty()) {
            return contents.stream().map(w -> {
                Either<Problem, JWorkflow> e = currentState.pathToWorkflow(WorkflowPath.of(JocInventory.pathToName(w.getPath())));
                if (e.isRight()) {
                    w.setContent(e.get().withPositions().toJson());
                    return w;
                }
                return null;
            }).filter(Objects::nonNull);
        }
        return Stream.empty();
    }

}
