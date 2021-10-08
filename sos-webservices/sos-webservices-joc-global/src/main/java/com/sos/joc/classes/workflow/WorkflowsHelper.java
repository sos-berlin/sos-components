package com.sos.joc.classes.workflow;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.controller.model.common.SyncState;
import com.sos.controller.model.common.SyncStateText;
import com.sos.controller.model.fileordersource.FileOrderSource;
import com.sos.controller.model.workflow.BoardWorkflows;
import com.sos.controller.model.workflow.Workflow;
import com.sos.controller.model.workflow.WorkflowDeps;
import com.sos.controller.model.workflow.WorkflowId;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.inventory.model.instruction.AddOrder;
import com.sos.inventory.model.instruction.ExpectNotice;
import com.sos.inventory.model.instruction.ForkJoin;
import com.sos.inventory.model.instruction.ForkList;
import com.sos.inventory.model.instruction.IfElse;
import com.sos.inventory.model.instruction.ImplicitEnd;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.instruction.InstructionType;
import com.sos.inventory.model.instruction.Lock;
import com.sos.inventory.model.instruction.PostNotice;
import com.sos.inventory.model.instruction.TryCatch;
import com.sos.inventory.model.workflow.Branch;
import com.sos.inventory.model.workflow.Parameters;
import com.sos.inventory.model.workflow.Requirements;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.common.SyncStateHelper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.deploy.DeployedConfigurationFilter;
import com.sos.joc.db.deploy.items.DeployedContent;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.order.OrderStateText;
import com.sos.joc.model.workflow.WorkflowIdsFilter;
import com.sos.joc.model.workflow.WorkflowsFilter;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.item.VersionedItemId;
import js7.data.order.Order;
import js7.data.workflow.WorkflowPath;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JOrder;
import js7.data_for_java.order.JOrderPredicates;
import js7.data_for_java.workflow.JWorkflow;
import js7.data_for_java.workflow.JWorkflowId;
import js7.data_for_java.workflow.position.JPosition;
import scala.Function1;

public class WorkflowsHelper {

    public static boolean isCurrentVersion(String versionId, JControllerState currentState) {
        if (versionId == null || versionId.isEmpty()) {
            return true;
        }
        return currentState.ordersBy(currentState.orderIsInCurrentVersionWorkflow()).parallel().anyMatch(o -> o.workflowId().versionId().string()
                .equals(versionId));
    }

    public static boolean isCurrentVersion(WorkflowId workflowId, JControllerState currentState) {
        if (workflowId == null) {
            return true;
        }
        return isCurrentVersion(workflowId.getVersionId(), currentState);
    }

    public static Stream<String> currentVersions(JControllerState currentState) {
        return currentState.ordersBy(currentState.orderIsInCurrentVersionWorkflow()).parallel().map(o -> o.workflowId().versionId().string());
    }

    public static Stream<JWorkflowId> currentJWorkflowIds(JControllerState currentState) {
        return currentState.ordersBy(currentState.orderIsInCurrentVersionWorkflow()).parallel().map(JOrder::workflowId);
    }

    public static Stream<WorkflowId> currentWorkflowIds(JControllerState currentState) {
        return currentState.ordersBy(currentState.orderIsInCurrentVersionWorkflow()).parallel().map(o -> new WorkflowId(o.workflowId().path()
                .string(), o.workflowId().versionId().string()));
    }

    public static Stream<String> oldVersions(JControllerState currentState) {
        return currentState.ordersBy(JOrderPredicates.not(currentState.orderIsInCurrentVersionWorkflow())).parallel().map(o -> o.workflowId()
                .versionId().string());
    }

    public static Stream<WorkflowId> oldWorkflowIds(JControllerState currentState) {
        return currentState.ordersBy(JOrderPredicates.not(currentState.orderIsInCurrentVersionWorkflow())).parallel().map(o -> new WorkflowId(o
                .workflowId().path().string(), o.workflowId().versionId().string()));
    }

    public static Stream<JWorkflowId> oldJWorkflowIds(JControllerState currentState) {
        return currentState.ordersBy(JOrderPredicates.not(currentState.orderIsInCurrentVersionWorkflow())).parallel().map(JOrder::workflowId);
    }

    public static ImplicitEnd createImplicitEndInstruction() {
        ImplicitEnd i = new ImplicitEnd();
        i.setTYPE(InstructionType.IMPLICIT_END);
        return i;
    }
    
    public static <T extends Workflow> T addWorkflowPositionsAndForkListVariablesAndExpectedNoticeBoards(T w) {
        if (w == null) {
            return null;
        }
        List<Instruction> instructions = w.getInstructions();
        Set<String> expectedNoticeBoards = new LinkedHashSet<>();
        Set<String> postNoticeBoards = new LinkedHashSet<>();
        Set<String> workflowNamesFromAddOrders = new LinkedHashSet<>();
        if (instructions != null) {
            instructions.add(createImplicitEndInstruction());
            w.setForkListVariables(new LinkedHashSet<>());
        } else {
            w.setInstructions(Collections.singletonList(createImplicitEndInstruction()));
        }
        Object[] o = {};
        setWorkflowPositionsAndForkListVariables(o, w.getInstructions(), w.getForkListVariables(), expectedNoticeBoards, postNoticeBoards,
                workflowNamesFromAddOrders);
        if (w.getForkListVariables() == null || w.getForkListVariables().isEmpty()) {
            w.setForkListVariables(null);
        }
        if (w instanceof WorkflowDeps) {
            setInitialDeps((WorkflowDeps) w, expectedNoticeBoards, postNoticeBoards, workflowNamesFromAddOrders);
        } else {
            if (w.getHasAddOrderDependencies() != Boolean.TRUE) {
                w.setHasAddOrderDependencies(!workflowNamesFromAddOrders.isEmpty());
            }
            w.setHasExpectedNoticeBoards(!expectedNoticeBoards.isEmpty());
            w.setHasPostNoticeBoards(!postNoticeBoards.isEmpty());
        }
        return w;
    }
    
    public static Requirements removeFinals(Workflow workflow) {
        Requirements r = workflow.getOrderPreparation();
        if (r.getParameters() != null && r.getParameters().getAdditionalProperties() != null) {
            Parameters params = r.getParameters();
            Set<String> finalKeys = params.getAdditionalProperties().entrySet().stream().filter(e -> e.getValue().getFinal() != null).map(
                    Map.Entry::getKey).collect(Collectors.toSet());
            for (String key : finalKeys) {
                params.removeAdditionalProperty(key);
            }
            r.setParameters(params);
        }
        return r;
    }
    
    public static Set<VersionedItemId<WorkflowPath>> getWorkflowIdsFromFolders(String controllerId, List<Folder> folders, JControllerState currentstate,
            Set<Folder> permittedFolders) {

        WorkflowsFilter workflowsFilter = new WorkflowsFilter();
        workflowsFilter.setControllerId(controllerId);
        workflowsFilter.setFolders(folders);
        SOSHibernateSession connection = null;
        try {
            connection = Globals.createSosHibernateStatelessConnection("getWorkflowIdsFromFolder");
            List<DeployedContent> contents = WorkflowsHelper.getDeployedContents(workflowsFilter, new DeployedConfigurationDBLayer(connection),
                    currentstate, permittedFolders);
            return contents.parallelStream().map(w -> currentstate.repo().idToWorkflow(JWorkflowId.of(w.getName(), w.getCommitId()))).filter(
                    Either::isRight).map(Either::get).map(JWorkflow::id).map(JWorkflowId::asScala).collect(Collectors.toSet());
        } finally {
            Globals.disconnect(connection);
        }
    }
    
    public static List<DeployedContent> getDeployedContents(WorkflowsFilter workflowsFilter, DeployedConfigurationDBLayer dbLayer,
            JControllerState currentstate, Set<Folder> permittedFolders) {

        List<DeployedContent> contents = getPermanentDeployedContent(workflowsFilter, dbLayer, permittedFolders);
        if (currentstate != null) {
            contents.addAll(getOlderWorkflows(workflowsFilter, currentstate, dbLayer, permittedFolders));
        }

        List<WorkflowId> workflowIds = workflowsFilter.getWorkflowIds();
        if (workflowIds != null && !workflowIds.isEmpty()) {
            workflowsFilter.setFolders(null);
            workflowsFilter.setRegex(null);
        }
        
        return contents;
    }
    
    public static Stream<DeployedContent> getDeployedContentsStream(WorkflowsFilter workflowsFilter, List<DeployedContent> contents,
            Set<Folder> permittedFolders) {

        Stream<DeployedContent> contentsStream = contents.parallelStream().distinct();
        boolean withoutFilter = (workflowsFilter.getFolders() == null || workflowsFilter.getFolders().isEmpty()) && (workflowsFilter
                .getWorkflowIds() == null || workflowsFilter.getWorkflowIds().isEmpty());
        if (withoutFilter) {
            contentsStream = contentsStream.filter(w -> JOCResourceImpl.canAdd(w.getPath(), permittedFolders));
        }
        if (workflowsFilter.getRegex() != null && !workflowsFilter.getRegex().isEmpty()) {
            Predicate<String> regex = Pattern.compile(workflowsFilter.getRegex().replaceAll("%", ".*"), Pattern.CASE_INSENSITIVE).asPredicate();
            contentsStream = contentsStream.filter(w -> regex.test(w.getName()) || regex.test(w.getTitle()));
        }

        return contentsStream;
    }
    
    private static List<DeployedContent> getPermanentDeployedContent(WorkflowsFilter workflowsFilter, DeployedConfigurationDBLayer dbLayer,
            Set<Folder> permittedFolders) {
        DeployedConfigurationFilter dbFilter = new DeployedConfigurationFilter();
        dbFilter.setControllerId(workflowsFilter.getControllerId());
        dbFilter.setObjectTypes(Collections.singleton(DeployType.WORKFLOW.intValue()));

        List<WorkflowId> workflowIds = workflowsFilter.getWorkflowIds();
        if (workflowIds != null && !workflowIds.isEmpty()) {
            workflowsFilter.setFolders(null);
            workflowsFilter.setRegex(null);
        }
        boolean withFolderFilter = workflowsFilter.getFolders() != null && !workflowsFilter.getFolders().isEmpty();
        List<DeployedContent> contents = null;

        if (workflowIds != null && !workflowIds.isEmpty()) {
            ConcurrentMap<Boolean, Set<WorkflowId>> workflowMap = workflowIds.stream().parallel().filter(w -> JOCResourceImpl.canAdd(w.getPath(),
                    permittedFolders)).collect(Collectors.groupingByConcurrent(w -> w.getVersionId() != null && !w.getVersionId().isEmpty(),
                            Collectors.toSet()));
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
        } else if (withFolderFilter && (permittedFolders == null || permittedFolders.isEmpty())) {
            // no folder permissions
        } else if (permittedFolders != null && !permittedFolders.isEmpty()) {
            dbFilter.setFolders(permittedFolders);
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
            DeployedConfigurationDBLayer dbLayer, Set<Folder> permittedFolders) {

        List<WorkflowId> workflowIds = workflowsFilter.getWorkflowIds();
        List<DeployedContent> contents = null;
        boolean withFolderFilter = workflowsFilter.getFolders() != null && !workflowsFilter.getFolders().isEmpty();

        if (workflowIds != null && !workflowIds.isEmpty()) {
            workflowsFilter.setRegex(null);
            // only permanent info
        } else if (withFolderFilter && (permittedFolders == null || permittedFolders.isEmpty())) {
            // no folder permissions
        } else {
            
            Set<WorkflowId> wIds = WorkflowsHelper.oldWorkflowIds(currentState).collect(Collectors.toSet());
            if (wIds == null || wIds.isEmpty()) {
                return Collections.emptyList();
            }
            
            DeployedConfigurationFilter dbFilter = new DeployedConfigurationFilter();
            dbFilter.setControllerId(workflowsFilter.getControllerId());
            dbFilter.setObjectTypes(Collections.singleton(DeployType.WORKFLOW.intValue()));
            dbFilter.setWorkflowIds(wIds);

            if (permittedFolders != null && !permittedFolders.isEmpty()) {
                dbFilter.setFolders(permittedFolders);
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
    
    private static void setInitialDeps(WorkflowDeps w, Set<String> expectedNoticeBoards, Set<String> postNoticeBoards,
            Set<String> workflowNamesFromAddOrders) {
        if (w.getExpectedNoticeBoards() == null) {
            w.setExpectedNoticeBoards(new BoardWorkflows());
        }
        if (w.getPostNoticeBoards() == null) {
            w.setPostNoticeBoards(new BoardWorkflows());
        }
        expectedNoticeBoards.forEach(board -> w.getExpectedNoticeBoards().setAdditionalProperty(board, Collections.emptyList()));
        postNoticeBoards.forEach(board -> w.getPostNoticeBoards().setAdditionalProperty(board, Collections.emptyList()));
        if (workflowNamesFromAddOrders != null) {
            w.setAddOrderToWorkflows(workflowNamesFromAddOrders.stream().map(name -> {
                Workflow wf = new Workflow();
                wf.setPath(name);
                return wf;
            }).collect(Collectors.toList()));
        } else {
            w.setAddOrderToWorkflows(Collections.emptyList());
        }
    }
    
    private static void setWorkflowPositionsAndForkListVariables(Object[] parentPosition, List<Instruction> insts, Set<String> forkListVariables,
            Set<String> expectedNoticeBoards, Set<String> postNoticeBoards, Set<String> workflowNamesFromAddOrders) {
        if (insts != null) {
            for (int i = 0; i < insts.size(); i++) {
                Object[] pos = extendArray(parentPosition, i);
                pos[parentPosition.length] = i;
                Instruction inst = insts.get(i);
                inst.setPosition(Arrays.asList(pos));
                inst.setPositionString(getJPositionString(inst.getPosition()));
                switch (inst.getTYPE()) {
                case FORK:
                    ForkJoin f = inst.cast();
                    for (Branch b : f.getBranches()) {
                        if (b.getWorkflow().getInstructions() != null) {
                            b.getWorkflow().getInstructions().add(createImplicitEndInstruction());
                        } else {
                            b.getWorkflow().setInstructions(Collections.singletonList(createImplicitEndInstruction()));
                        }
                        setWorkflowPositionsAndForkListVariables(extendArray(pos, "fork+" + b.getId()), b.getWorkflow().getInstructions(),
                                forkListVariables, expectedNoticeBoards, postNoticeBoards, workflowNamesFromAddOrders);
                    }
                    break;
                case FORKLIST:
                    ForkList fl = inst.cast();
                    forkListVariables.add(fl.getChildren());
                    if (fl.getWorkflow().getInstructions() != null) {
                        fl.getWorkflow().getInstructions().add(createImplicitEndInstruction());
                    } else {
                        fl.getWorkflow().setInstructions(Collections.singletonList(createImplicitEndInstruction()));
                    }
                    setWorkflowPositionsAndForkListVariables(extendArray(pos, "fork"), fl.getWorkflow().getInstructions(), forkListVariables,
                            expectedNoticeBoards, postNoticeBoards, workflowNamesFromAddOrders);
                    break;
                case EXPECT_NOTICE:
                    ExpectNotice en = inst.cast();
                    expectedNoticeBoards.add(en.getNoticeBoardName());
                    break;
                case POST_NOTICE:
                    PostNotice pn = inst.cast();
                    postNoticeBoards.add(pn.getNoticeBoardName());
                    break;
                case IF:
                    IfElse ie = inst.cast();
                    setWorkflowPositionsAndForkListVariables(extendArray(pos, "then"), ie.getThen().getInstructions(), forkListVariables,
                            expectedNoticeBoards, postNoticeBoards, workflowNamesFromAddOrders);
                    if (ie.getElse() != null) {
                        setWorkflowPositionsAndForkListVariables(extendArray(pos, "else"), ie.getElse().getInstructions(), forkListVariables,
                                expectedNoticeBoards, postNoticeBoards, workflowNamesFromAddOrders);
                    }
                    break;
                case TRY:
                    TryCatch tc = inst.cast();
                    setWorkflowPositionsAndForkListVariables(extendArray(pos, "try+0"), tc.getTry().getInstructions(), forkListVariables,
                            expectedNoticeBoards, postNoticeBoards, workflowNamesFromAddOrders);
                    if (tc.getCatch() != null) {
                        setWorkflowPositionsAndForkListVariables(extendArray(pos, "catch+0"), tc.getCatch().getInstructions(), forkListVariables,
                                expectedNoticeBoards, postNoticeBoards, workflowNamesFromAddOrders);
                    }
                    break;
                case LOCK:
                    Lock l = inst.cast();
                    setWorkflowPositionsAndForkListVariables(extendArray(pos, "lock"), l.getLockedWorkflow().getInstructions(), forkListVariables,
                            expectedNoticeBoards, postNoticeBoards, workflowNamesFromAddOrders);
                    break;
                case ADD_ORDER:
                    AddOrder ao = inst.cast();
                    workflowNamesFromAddOrders.add(ao.getWorkflowName());
                default:
                    break;
                }
            }
        }
    }
    
//    private static void setPositions(Object[] parentPosition, List<Instruction> insts, Set<Positions> posSet) {
//        if (insts != null) {
//            for (int i = 0; i < insts.size(); i++) {
//                Object[] pos = extendArray(parentPosition, i);
//                pos[parentPosition.length] = i;
//                Instruction inst = insts.get(i);
//                Positions p = new Positions();
//                p.setPosition(Arrays.asList(pos));
//                p.setPositionString(getJPositionString(inst.getPosition()));
//                posSet.add(p);
//                switch (inst.getTYPE()) {
//                case FORK:
//                    ForkJoin f = inst.cast();
//                    for (Branch b : f.getBranches()) {
//                        setPositions(extendArray(pos, "fork+" + b.getId()), b.getWorkflow().getInstructions(), posSet);
//                    }
//                    break;
//                case IF:
//                    IfElse ie = inst.cast();
//                    setPositions(extendArray(pos, "then"), ie.getThen().getInstructions(), posSet);
//                    if (ie.getElse() != null) {
//                        setPositions(extendArray(pos, "else"), ie.getElse().getInstructions(), posSet);
//                    }
//                    break;
//                case TRY:
//                    TryCatch tc = inst.cast();
//                    setPositions(extendArray(pos, "try+0"), tc.getTry().getInstructions(), posSet);
//                    if (tc.getCatch() != null) {
//                        setPositions(extendArray(pos, "catch+0"), tc.getCatch().getInstructions(), posSet);
//                    }
//                    break;
//                case LOCK:
//                    Lock l = inst.cast();
//                    setPositions(extendArray(pos, "lock"), l.getLockedWorkflow().getInstructions(), posSet);
//                    break;
//                default:
//                    break;
//                }
//            }
//        }
//    }
    
    public static Set<String> extractImplicitEnds(List<Instruction> insts) {
        Set<String> posSet = new HashSet<>();
        extractImplicitEnds(insts, posSet, false);
        return posSet;
    }
    
    private static void extractImplicitEnds(List<Instruction> insts, Set<String> posSet, boolean extract) {
        if (insts != null) {
            for (int i = 0; i < insts.size(); i++) {
                Instruction inst = insts.get(i);
                switch (inst.getTYPE()) {
                case IMPLICIT_END:
                    if (extract) {
                        posSet.add(getJPositionString(inst.getPosition()));
                    }
                    break;
                case FORK:
                    ForkJoin f = inst.cast();
                    for (Branch b : f.getBranches()) {
                        extractImplicitEnds(b.getWorkflow().getInstructions(), posSet, false);
                    }
                    break;
                case FORKLIST:
                    ForkList fl = inst.cast();
                    extractImplicitEnds(fl.getWorkflow().getInstructions(), posSet, false);
                    break;
                case IF:
                    IfElse ie = inst.cast();
                    extractImplicitEnds(ie.getThen().getInstructions(), posSet, true);
                    if (ie.getElse() != null) {
                        extractImplicitEnds(ie.getElse().getInstructions(), posSet, true);
                    }
                    break;
                case TRY:
                    TryCatch tc = inst.cast();
                    extractImplicitEnds(tc.getTry().getInstructions(), posSet, true);
                    if (tc.getCatch() != null) {
                        extractImplicitEnds(tc.getCatch().getInstructions(), posSet, true);
                    }
                    break;
                case LOCK:
                    Lock l = inst.cast();
                    extractImplicitEnds(l.getLockedWorkflow().getInstructions(), posSet, true);
                    break;
                default:
                    break;
                }
            }
        }
    }
    
    private static String getJPositionString(List<Object> positionList) {
        Either<Problem, JPosition> jPosEither = JPosition.fromList(positionList);
        if (jPosEither.isRight()) {
            return jPosEither.get().toString();
        }
        return null;
    }

    private static Object[] extendArray(Object[] position, Object extValue) {
        Object[] pos = Arrays.copyOf(position, position.length + 1);
        pos[position.length] = extValue;
        return pos;
    }

    public static SyncState getState(JControllerState currentstate, Workflow workflow) {
        // TODO Collection of available workflows should read from memory
        SyncStateText stateText = SyncStateText.UNKNOWN;
        if (currentstate != null) {
            stateText = SyncStateText.NOT_IN_SYNC;
            Either<Problem, JWorkflow> workflowV = currentstate.repo().idToWorkflow(JWorkflowId.of(JocInventory.pathToName(workflow.getPath()),
                    workflow.getVersionId()));
            // ProblemHelper.throwProblemIfExist(workflowV);
            if (workflowV != null && workflowV.isRight()) {
                stateText = SyncStateText.IN_SYNC;
            }
        }
        return SyncStateHelper.getState(stateText);
    }

    public static Boolean workflowCurrentlyExists(JControllerState currentstate, String workflow) {
        Boolean exists = false;
        if (currentstate != null) {
            Either<Problem, JWorkflow> workflowV = currentstate.repo().pathToWorkflow(WorkflowPath.of(workflow));
            if (workflowV != null && workflowV.isRight()) {
                exists = true;
            }
        }
        return exists;
    }
    
    public static Map<String, List<FileOrderSource>> workflowToFileOrderSources(JControllerState controllerState, String controllerId,
            Set<String> workflowNames, DeployedConfigurationDBLayer dbLayer) {
        Set<String> syncFileOrderSources = controllerState == null ? Collections.emptySet() : controllerState.fileWatches().stream().parallel().map(
                f -> f.workflowPath().string()).collect(Collectors.toSet());

        DeployedConfigurationFilter filter = new DeployedConfigurationFilter();
        filter.setControllerId(controllerId);
        filter.setObjectTypes(Collections.singleton(DeployType.FILEORDERSOURCE.intValue()));
        List<DeployedContent> fileOrderSources = dbLayer.getDeployedInventory(filter);
        if (fileOrderSources != null && !fileOrderSources.isEmpty()) {
            return fileOrderSources.stream().parallel().filter(dbItem -> dbItem.getContent() != null).map(dbItem -> {
                try {
                    FileOrderSource f = Globals.objectMapper.readValue(dbItem.getContent(), FileOrderSource.class);
                    if (!workflowNames.contains(f.getWorkflowName())) {
                        return null;
                    }
                    f.setPath(dbItem.getPath());
                    f.setVersionDate(dbItem.getCreated());
                    if (syncFileOrderSources.contains(f.getWorkflowName())) {
                        f.setState(SyncStateHelper.getState(SyncStateText.IN_SYNC));
                    } else {
                        f.setState(SyncStateHelper.getState(SyncStateText.NOT_IN_SYNC));
                    }
                    return f;
                } catch (Exception e) {
                    return null;
                }
            }).filter(Objects::nonNull).collect(Collectors.groupingBy(FileOrderSource::getWorkflowName));
        }
        return Collections.emptyMap();
    }
    
    public static List<FileOrderSource> workflowToFileOrderSources(JControllerState controllerState, String controllerId, String WorkflowPath,
            DeployedConfigurationDBLayer dbLayer) {
        Set<String> fileWatchNames = WorkflowsHelper.workflowToFileWatchNames(controllerState, WorkflowPath);
        if (!fileWatchNames.isEmpty()) {
            DeployedConfigurationFilter filter = new DeployedConfigurationFilter();
            filter.setControllerId(controllerId);
            filter.setNames(fileWatchNames);
            filter.setObjectTypes(Collections.singleton(DeployType.FILEORDERSOURCE.intValue()));
            List<DeployedContent> fileOrderSources = dbLayer.getDeployedInventory(filter);
            if (fileOrderSources != null && !fileOrderSources.isEmpty()) {
                return fileOrderSources.stream().parallel().filter(dbItem -> dbItem.getContent() != null).map(dbItem -> {
                    try {
                        FileOrderSource f = Globals.objectMapper.readValue(dbItem.getContent(), FileOrderSource.class);
                        f.setPath(dbItem.getPath());
                        f.setVersionDate(dbItem.getCreated());
                        f.setState(SyncStateHelper.getState(SyncStateText.IN_SYNC));
                        return f;
                    } catch (Exception e) {
                        return null;
                    }
                }).filter(Objects::nonNull).collect(Collectors.toList());
            }
        }
        return null;
    }
    
    private static Set<String> workflowToFileWatchNames(JControllerState controllerState, String workflowPath) {
        String workflowName = JocInventory.pathToName(workflowPath);
        return controllerState.fileWatches().stream().parallel().filter(f -> f.workflowPath().string().equals(workflowName)).map(f -> f.path()
                .string()).collect(Collectors.toSet());
    }
    
    public static ConcurrentMap<JWorkflowId, Map<OrderStateText, Integer>> getGroupedOrdersCountPerWorkflow(JControllerState currentstate,
            WorkflowIdsFilter workflowsFilter, Set<Folder> permittedFolders) {

        final Instant surveyInstant = currentstate.instant();
        Predicate<JOrder> dateToFilter = o -> true;
        if (workflowsFilter.getDateTo() != null && !workflowsFilter.getDateTo().isEmpty()) {
                String dateTo = workflowsFilter.getDateTo();
                if ("0d".equals(dateTo)) {
                    dateTo = "1d";
                }
                Instant dateToInstant = JobSchedulerDate.getInstantFromDateStr(dateTo, false, workflowsFilter.getTimeZone());
                final Instant until = (dateToInstant.isBefore(surveyInstant)) ? surveyInstant : dateToInstant;
                dateToFilter = o -> {
                    if (!o.asScala().isSuspended() && OrderStateText.SCHEDULED.equals(OrdersHelper.getGroupedState(o.asScala().state().getClass()))) {
                        if (o.scheduledFor().isPresent() && o.scheduledFor().get().isAfter(until)) {
                            if (o.scheduledFor().get().toEpochMilli() == JobSchedulerDate.NEVER_MILLIS.longValue()) {
                                return true;
                            }
                            return false;
                        }
                    }
                    return true;
                };
        }
        
        Set<VersionedItemId<WorkflowPath>> workflows2 = workflowsFilter.getWorkflowIds().parallelStream().filter(w -> JOCResourceImpl.canAdd(WorkflowPaths
                .getPath(w), permittedFolders)).map(w -> {
                    if (w.getVersionId() == null || w.getVersionId().isEmpty()) {
                        return currentstate.repo().pathToWorkflow(WorkflowPath.of(JocInventory.pathToName(w.getPath())));
                    } else {
                        return currentstate.repo().idToWorkflow(JWorkflowId.of(JocInventory.pathToName(w.getPath()), w.getVersionId()));
                    }
                }).filter(Either::isRight).map(Either::get).map(JWorkflow::id).map(JWorkflowId::asScala).collect(Collectors.toSet());
        
        Function1<Order<Order.State>, Object> workflowFilter = o -> workflows2.contains(o.workflowId());
        Function1<Order<Order.State>, Object> finishedFilter = JOrderPredicates.or(JOrderPredicates.or(JOrderPredicates.byOrderState(
                Order.Finished$.class), JOrderPredicates.byOrderState(Order.Cancelled$.class)), JOrderPredicates.byOrderState(
                        Order.ProcessingKilled$.class));
        Function1<Order<Order.State>, Object> suspendFilter = JOrderPredicates.and(o -> o.isSuspended(), JOrderPredicates.not(finishedFilter));
        Function1<Order<Order.State>, Object> cycledOrderFilter = JOrderPredicates.and(JOrderPredicates.byOrderState(Order.Fresh$.class),
                JOrderPredicates.and(o -> o.id().string().matches(".*#C[0-9]+-.*"), JOrderPredicates.not(suspendFilter)));
        Function1<Order<Order.State>, Object> notCycledOrderFilter = JOrderPredicates.not(cycledOrderFilter);

        Stream<JOrder> cycledOrderStream = currentstate.ordersBy(JOrderPredicates.and(workflowFilter, cycledOrderFilter)).parallel().filter(
                dateToFilter);
        Stream<JOrder> notCycledOrderStream = currentstate.ordersBy(JOrderPredicates.and(workflowFilter, notCycledOrderFilter)).parallel().filter(
                dateToFilter);
        Comparator<JOrder> comp = Comparator.comparing(o -> o.id().string());
        Collection<TreeSet<JOrder>> cycledOrderColl = cycledOrderStream.collect(Collectors.groupingBy(o -> o.id().string().substring(0, 24),
                Collectors.toCollection(() -> new TreeSet<>(comp)))).values();
        cycledOrderStream = cycledOrderColl.stream().parallel().map(t -> t.first());
        ConcurrentMap<JWorkflowId, Map<OrderStateText, Integer>> groupedOrdersCount = Stream.concat(notCycledOrderStream, cycledOrderStream)
                .collect(Collectors.groupingByConcurrent(JOrder::workflowId, Collectors.groupingBy(o -> groupingByState(o, surveyInstant),
                        Collectors.reducing(0, e -> 1, Integer::sum))));
        
        workflows2.forEach(w -> groupedOrdersCount.putIfAbsent(JWorkflowId.apply(w), Collections.emptyMap()));
        
        return groupedOrdersCount;
    }
    
    private static OrderStateText groupingByState(JOrder order, Instant surveyInstant) {
        OrderStateText groupedState = OrdersHelper.getGroupedState(order.asScala().state().getClass());
        if (order.asScala().isSuspended() && !(OrderStateText.CANCELLED.equals(groupedState) || OrderStateText.FINISHED.equals(groupedState))) {
            groupedState = OrderStateText.SUSPENDED;
        }
        if (OrderStateText.SCHEDULED.equals(groupedState) && order.scheduledFor().isPresent()) {
            Instant scheduledInstant = order.scheduledFor().get();
            if (JobSchedulerDate.NEVER_MILLIS.longValue() == scheduledInstant.toEpochMilli()) {
                groupedState = OrderStateText.PENDING;
            } else if (scheduledInstant.isBefore(surveyInstant)) {
                groupedState = OrderStateText.BLOCKED;
            }
        }
        return groupedState;
    }
    
}
