package com.sos.joc.workflows.impl;

import java.time.Instant;
import java.util.Arrays;
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

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.controller.model.workflow.Workflow;
import com.sos.controller.model.workflow.WorkflowId;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.inventory.model.instruction.ForkJoin;
import com.sos.inventory.model.instruction.IfElse;
import com.sos.inventory.model.instruction.ImplicitEnd;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.instruction.InstructionType;
import com.sos.inventory.model.instruction.Lock;
import com.sos.inventory.model.instruction.TryCatch;
import com.sos.inventory.model.workflow.Branch;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.deploy.DeployedConfigurationFilter;
import com.sos.joc.db.deploy.items.DeployedContent;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
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

    @Override
    public JOCDefaultResponse postWorkflowsPermanent(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, WorkflowsFilter.class);
            WorkflowsFilter workflowsFilter = Globals.objectMapper.readValue(filterBytes, WorkflowsFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions(workflowsFilter.getControllerId(), getPermissonsJocCockpit(workflowsFilter
                    .getControllerId(), accessToken).getWorkflow().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            List<DeployedContent> contents = getPermanentDeployedContent(workflowsFilter);

            Workflows workflows = new Workflows();
            if (contents != null) {
                Stream<DeployedContent> contentsStream = contents.stream();
                if (workflowsFilter.getRegex() != null && !workflowsFilter.getRegex().isEmpty()) {
                    Predicate<String> regex = Pattern.compile(workflowsFilter.getRegex().replaceAll("%", ".*")).asPredicate();
                    contentsStream = contentsStream.filter(w -> regex.test(w.getPath()));
                }
                workflows.setWorkflows(contentsStream.map(w -> {
                    try {
                        Workflow workflow = Globals.objectMapper.readValue(w.getContent(), Workflow.class);
                        workflow.setPath(w.getPath());
                        workflow.setIsCurrentVersion(null); // TODO
                        List<Instruction> instructions = workflow.getInstructions();
                        if (instructions != null) {
                            instructions.add(createImplicitEndInstruction());
                        } else {
                            instructions = Arrays.asList(createImplicitEndInstruction());
                            workflow.setInstructions(instructions);
                        }
                        return addWorkflowPositions(workflow);
                    } catch (Exception e) {
                        // TODO
                        return null;
                    }
                }).filter(Objects::nonNull).collect(Collectors.toList()));
            }
            workflows.setDeliveryDate(Date.from(Instant.now()));

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsString(workflows));

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
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

//            JControllerState currentState = Proxy.of(workflowsFilter.getControllerId()).currentState();
//            Long surveyDateMillis = currentState.eventId() / 1000;
//            Stream<DeployedContent> contentsStream = getVolatileDeployedContent(workflowsFilter, currentState);

            Workflows workflows = new Workflows();
//            if (workflowsFilter.getRegex() != null && !workflowsFilter.getRegex().isEmpty()) {
//                Predicate<String> regex = Pattern.compile(workflowsFilter.getRegex().replaceAll("%", ".*")).asPredicate();
//                contentsStream = contentsStream.filter(w -> regex.test(w.getPath()));
//            }
//            workflows.setWorkflows(contentsStream.map(c -> {
//                try {
//                    Workflow workflow = Globals.objectMapper.readValue(c.getContent(), Workflow.class);
//                    workflow.setPath(c.getPath());
//                    return workflow;
//                } catch (Exception e) {
//                    // TODO
//                    return null;
//                }
//            }).filter(Objects::nonNull).collect(Collectors.toList()));
//            workflows.setSurveyDate(Date.from(Instant.ofEpochMilli(surveyDateMillis)));
            workflows.setDeliveryDate(Date.from(Instant.now()));

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsString(workflows));

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    private static ImplicitEnd createImplicitEndInstruction() {
        ImplicitEnd i = new ImplicitEnd();
        i.setTYPE(InstructionType.IMPLICIT_END);
        return i;
    }

    private List<DeployedContent> getPermanentDeployedContent(WorkflowsFilter workflowsFilter) {
        SOSHibernateSession connection = null;
        try {
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
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(connection);
            List<DeployedContent> contents = null;

            if (workflowIds != null && !workflowIds.isEmpty()) {
                workflowsFilter.setRegex(null);
                Map<Boolean, Set<WorkflowId>> workflowMap = workflowIds.stream().filter(w -> canAdd(w.getPath(), folders)).collect(Collectors
                        .groupingBy(w -> w.getVersionId() != null, Collectors.toSet()));
                if (workflowMap.containsKey(true)) {
                    dbFilter.setWorkflowIds(workflowMap.get(true));
                    contents = dbLayer.getDeployedInventoryWithCommitIds(dbFilter);
                }
                if (workflowMap.containsKey(false)) {
                    dbFilter.setPaths(workflowMap.get(false).stream().map(WorkflowId::getPath).collect(Collectors.toSet()));
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
            return contents;
        } finally {
            Globals.disconnect(connection);
        }
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
                    
//                    dbFilter.setFolders(null);
//                    Set<WorkflowId> wIds = WorkflowsHelper.oldWorkflowIds(currentState).collect(Collectors.toSet());
//                    dbFilter.setWorkflowIds(wIds);
//                    Map<WorkflowId, String> namePathMap = dbLayer.getNamePathMappingWithCommitIds(dbFilter);
//                    wIds.stream().filter(wId -> folders.contains(Paths.get(namePathMap.get(wId)).getParent().toString().replace('\\','/'))).map(wId -> {
//                        Either<Problem, JWorkflow> e = currentState.idToWorkflow(JWorkflowId.of(wId.getPath(), wId.getVersionId()));
//                        if (e.isRight()) {
//                            return new DeployedContent(namePathMap.get(wId), e.get().withPositions().toJson(), e.get().id().versionId().string(), false);
//                        }
//                        return null;
//                    }).filter(Objects::nonNull);
                } else {
                    contents = dbLayer.getDeployedInventory(dbFilter);
                    
//                    currentState.ordersBy(JOrderPredicates.not(currentState.orderIsInCurrentVersionWorkflow())).map(JOrder::workflowId).distinct().map(w -> {
//                        Either<Problem, JWorkflow> e = currentState.idToWorkflow(w);
//                        if (e.isRight()) {
//                            // TODO nameToPath mapping
//                            return new DeployedContent(w.path().string(), e.get().withPositions().toJson(), e.get().id().versionId().string(), false);
//                        }
//                        return null;
//                    }).filter(Objects::nonNull);
                }
            }
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
        } finally {
            Globals.disconnect(connection);
        }
    }

    private Workflow addWorkflowPositions(Workflow w) {
        if (w == null) {
            return null;
        }
        Object[] o = {};
        setWorkflowPositions(o, w.getInstructions());
        return w;
    }

    private void setWorkflowPositions(Object[] parentPosition, List<Instruction> insts) {
        if (insts != null) {
            for (int i = 0; i < insts.size(); i++) {
                Object[] pos = extendArray(parentPosition, i);
                Instruction inst = insts.get(i);
                inst.setPosition(Arrays.asList(pos));
                switch (inst.getTYPE()) {
                case FORK:
                    ForkJoin f = inst.cast();
                    for (Branch b : f.getBranches()) {
                        setWorkflowPositions(extendArray(pos, "fork+" + b.getId()), b.getWorkflow().getInstructions());
                    }
                    break;
                case IF:
                    IfElse ie = inst.cast();
                    setWorkflowPositions(extendArray(pos, "then"), ie.getThen().getInstructions());
                    if (ie.getElse() != null) {
                        setWorkflowPositions(extendArray(pos, "else"), ie.getElse().getInstructions());
                    }
                    break;
                case TRY:
                    TryCatch tc = inst.cast();
                    setWorkflowPositions(extendArray(pos, "try+0"), tc.getTry().getInstructions());
                    if (tc.getCatch() != null) {
                        setWorkflowPositions(extendArray(pos, "catch+0"), tc.getCatch().getInstructions());
                    }
                    break;
                case LOCK:
                    Lock l = inst.cast();
                    setWorkflowPositions(extendArray(pos, "lock"), l.getLockedWorkflow().getInstructions());
                    break;
                default:
                    break;
                }
            }
        }
    }

    private Object[] extendArray(Object[] position, Object extValue) {
        Object[] pos = Arrays.copyOf(position, position.length + 1);
        pos[position.length] = extValue;
        return pos;
    }

}
