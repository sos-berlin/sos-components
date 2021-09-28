package com.sos.joc.classes.workflow;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import com.sos.joc.Globals;
import com.sos.joc.classes.common.SyncStateHelper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.deploy.DeployedConfigurationFilter;
import com.sos.joc.db.deploy.items.DeployedContent;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.workflow.WorkflowPath;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JOrder;
import js7.data_for_java.order.JOrderPredicates;
import js7.data_for_java.workflow.JWorkflow;
import js7.data_for_java.workflow.JWorkflowId;
import js7.data_for_java.workflow.position.JPosition;

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
    
}
