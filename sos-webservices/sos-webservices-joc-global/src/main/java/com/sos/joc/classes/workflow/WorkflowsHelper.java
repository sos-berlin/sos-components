package com.sos.joc.classes.workflow;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sos.commons.hibernate.SOSHibernate;
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
import com.sos.inventory.model.instruction.Cycle;
import com.sos.inventory.model.instruction.ExpectNotice;
import com.sos.inventory.model.instruction.ExpectNotices;
import com.sos.inventory.model.instruction.ForkJoin;
import com.sos.inventory.model.instruction.ForkList;
import com.sos.inventory.model.instruction.IfElse;
import com.sos.inventory.model.instruction.ImplicitEnd;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.instruction.InstructionState;
import com.sos.inventory.model.instruction.InstructionStateText;
import com.sos.inventory.model.instruction.InstructionType;
import com.sos.inventory.model.instruction.Lock;
import com.sos.inventory.model.instruction.NamedJob;
import com.sos.inventory.model.instruction.PostNotice;
import com.sos.inventory.model.instruction.PostNotices;
import com.sos.inventory.model.instruction.TryCatch;
import com.sos.inventory.model.workflow.Branch;
import com.sos.inventory.model.workflow.Parameters;
import com.sos.inventory.model.workflow.Requirements;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.classes.audit.JocAuditLog;
import com.sos.joc.classes.common.SyncStateHelper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.NoticeToNoticesConverter;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.deploy.DeployedConfigurationFilter;
import com.sos.joc.db.deploy.items.DeployedContent;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.model.audit.ObjectType;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.order.OrderStateText;
import com.sos.joc.model.order.Position;
import com.sos.joc.model.workflow.WorkflowOrderCountFilter;
import com.sos.joc.model.workflow.WorkflowsFilter;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.item.VersionedItemId;
import js7.data.order.Order;
import js7.data.order.OrderId;
import js7.data.workflow.WorkflowPath;
import js7.data.workflow.WorkflowPathControlState;
import js7.data.workflow.position.Label;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JOrder;
import js7.data_for_java.order.JOrderPredicates;
import js7.data_for_java.workflow.JWorkflow;
import js7.data_for_java.workflow.JWorkflowId;
import js7.data_for_java.workflow.position.JPosition;
import scala.Function1;
import scala.collection.JavaConverters;

public class WorkflowsHelper {
    
    public static final Map<InstructionStateText, Integer> instructionStates = Collections.unmodifiableMap(
            new HashMap<InstructionStateText, Integer>() {

                private static final long serialVersionUID = 1L;

                {
                    put(InstructionStateText.SKIPPED, 5);
                    put(InstructionStateText.STOPPED, 2);
                }
            });
    
    public static InstructionState getState(InstructionStateText stateText) {
        InstructionState state = new InstructionState();
        state.set_text(stateText);
        state.setSeverity(instructionStates.get(stateText));
        return state;
    }

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

//    public static Stream<String> currentVersions(JControllerState currentState) {
//        return currentState.ordersBy(currentState.orderIsInCurrentVersionWorkflow()).parallel().map(o -> o.workflowId().versionId().string());
//    }
//
//    public static Stream<JWorkflowId> currentJWorkflowIds(JControllerState currentState) {
//        return currentState.ordersBy(currentState.orderIsInCurrentVersionWorkflow()).parallel().map(JOrder::workflowId);
//    }
//
//    public static Stream<WorkflowId> currentWorkflowIds(JControllerState currentState) {
//        return currentState.ordersBy(currentState.orderIsInCurrentVersionWorkflow()).parallel().map(o -> new WorkflowId(o.workflowId().path()
//                .string(), o.workflowId().versionId().string()));
//    }
//
//    public static Stream<String> oldVersions(JControllerState currentState) {
//        return currentState.ordersBy(JOrderPredicates.not(currentState.orderIsInCurrentVersionWorkflow())).parallel().map(o -> o.workflowId()
//                .versionId().string());
//    }

    private static Stream<WorkflowId> oldWorkflowIds(JControllerState currentState) {
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
    
    public static <T extends Workflow> T addWorkflowPositionsAndForkListVariablesAndExpectedNoticeBoards(T w, Set<String> skippedLabels) {
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
                workflowNamesFromAddOrders, skippedLabels);
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
    
    public static com.sos.inventory.model.workflow.Workflow addWorkflowPositions(com.sos.inventory.model.workflow.Workflow w) {
        if (w == null) {
            return null;
        }
        List<Instruction> instructions = w.getInstructions();
        if (instructions != null) {
            instructions.add(createImplicitEndInstruction());
        } else {
            w.setInstructions(Collections.singletonList(createImplicitEndInstruction()));
        }
        Object[] o = {};
        setWorkflowPositions(o, w.getInstructions());
        return w;
    }
    
    public static Set<Position> getWorkflowAddOrderPositions(Workflow w) {
        if (w == null) {
            return null;
        }
        List<Instruction> instructions = w.getInstructions();
        if (instructions != null) {
            instructions.add(createImplicitEndInstruction());
        } else {
            w.setInstructions(Collections.singletonList(createImplicitEndInstruction()));
        }
        Object[] o = {};
        Set<Position> positions = new LinkedHashSet<Position>();
        setWorkflowAddOrderPositions(o, w.getInstructions(), positions);
        return positions;
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
    
    public static Stream<JWorkflowId> getWorkflowIdsStreamFromFolders(String controllerId, List<Folder> folders, JControllerState currentstate,
            Set<Folder> permittedFolders) {

        WorkflowsFilter workflowsFilter = new WorkflowsFilter();
        workflowsFilter.setControllerId(controllerId);
        workflowsFilter.setFolders(folders);
        SOSHibernateSession connection = null;
        try {
            connection = Globals.createSosHibernateStatelessConnection("getWorkflowIdsFromFolder");
            List<DeployedContent> contents = WorkflowsHelper.getDeployedContents(workflowsFilter, new DeployedConfigurationDBLayer(connection),
                    currentstate, permittedFolders);
            return contents.parallelStream().map(w -> currentstate.repo().idToCheckedWorkflow(JWorkflowId.of(w.getName(), w.getCommitId()))).filter(
                    Either::isRight).map(Either::get).map(JWorkflow::id);
        } finally {
            Globals.disconnect(connection);
        }
    }
    
    public static Set<VersionedItemId<WorkflowPath>> getWorkflowIdsFromFolders(String controllerId, List<Folder> folders,
            JControllerState currentstate, Set<Folder> permittedFolders) {

        return getWorkflowIdsStreamFromFolders(controllerId, folders, currentstate, permittedFolders).map(JWorkflowId::asScala).collect(Collectors
                .toSet());
    }
    
    public static Set<WorkflowPath> getWorkflowPathsFromFolders(String controllerId, List<Folder> folders,
            JControllerState currentstate, Set<Folder> permittedFolders) {

        return getWorkflowIdsStreamFromFolders(controllerId, folders, currentstate, permittedFolders).map(JWorkflowId::path).collect(Collectors
                .toSet());
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
            workflowsFilter.setStates(null);
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
        // TODO instead workflowName as glob
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
                    dbFilter.setNames(workflowMap.get(true).parallelStream().map(WorkflowId::getPath).map(JocInventory::pathToName).collect(Collectors
                            .toSet()));
                    List<DeployedContent> contents2 = dbLayer.getDeployedInventory(dbFilter);
                    if (contents2 != null && !contents2.isEmpty()) {
                        Set<String> commitIds = contents2.parallelStream().map(c -> c.getName() + "," + c.getCommitId()).collect(Collectors.toSet());
                        contents = contents.parallelStream().map(c -> {
                            c.setPath(WorkflowPaths.getPath(c.getName()));
                            c.setIsCurrentVersion(commitIds.contains(c.getName() + "," + c.getCommitId()));
                            return c;
                        }).collect(Collectors.toList());
                    } else {
                        contents = contents.parallelStream().peek(c -> c.setPath(WorkflowPaths.getPath(c.getName()))).collect(Collectors.toList());
                    }
                }
            }
            if (workflowMap.containsKey(false)) { // without versionId
                dbFilter.setNames(workflowMap.get(false).stream().parallel().map(WorkflowId::getPath).map(JocInventory::pathToName).collect(Collectors
                        .toSet()));

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
            
            List<WorkflowId> wIds = oldWorkflowIds(currentState).collect(Collectors.toList());
            if (wIds == null || wIds.isEmpty()) {
                return Collections.emptyList();
            }
            
            //List<String> jsons = oldJWorkflowIds(currentState).map(wId -> currentState.repo().idToCheckedWorkflow(wId)).filter(Either::isRight).map(Either::get).map(JWorkflow::toJson).collect(Collectors.toList());
            
            DeployedConfigurationFilter dbFilter = new DeployedConfigurationFilter();
            dbFilter.setControllerId(workflowsFilter.getControllerId());
            dbFilter.setObjectTypes(Collections.singleton(DeployType.WORKFLOW.intValue()));
//            if (permittedFolders != null && !permittedFolders.isEmpty()) {
//                dbFilter.setFolders(permittedFolders);
//            }
            
            // considered that wIds.size() can be > 1000
            if (wIds.size() > SOSHibernate.LIMIT_IN_CLAUSE) {
                List<List<WorkflowId>> wIdsPartitions = SOSHibernate.getInClausePartitions(wIds);
                contents = new ArrayList<>();
                for (List<WorkflowId> wIdsPartition : wIdsPartitions) {
                    dbFilter.setWorkflowIds(wIdsPartition);
                    contents.addAll(dbLayer.getDeployedInventoryWithCommitIds(dbFilter));
                }
            } else {
                dbFilter.setWorkflowIds(wIds);
                contents = dbLayer.getDeployedInventoryWithCommitIds(dbFilter);
            }
        }

        if (contents == null) {
            return Collections.emptyList();
        }
        
        Stream<DeployedContent> stream = contents.stream().peek(i -> i.setPath(WorkflowPaths.getPath(i.getName())));
        if (permittedFolders != null && !permittedFolders.isEmpty()) {
            stream = stream.filter(i -> JOCResourceImpl.canAdd(i.getPath(), permittedFolders));
        }

        return stream.collect(Collectors.toList());
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
            Set<String> expectedNoticeBoards, Set<String> postNoticeBoards, Set<String> workflowNamesFromAddOrders, Set<String> skippedLabels) {
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
                                forkListVariables, expectedNoticeBoards, postNoticeBoards, workflowNamesFromAddOrders, skippedLabels);
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
                            expectedNoticeBoards, postNoticeBoards, workflowNamesFromAddOrders, skippedLabels);
                    break;
                case EXPECT_NOTICE:
                    ExpectNotice en = inst.cast();
                    expectedNoticeBoards.add(en.getNoticeBoardName());
                    insts.set(i, NoticeToNoticesConverter.expectNoticeToExpectNotices(en));
                    break;
                case EXPECT_NOTICES:
                    ExpectNotices ens = inst.cast();
                    String ensNamesExpr = ens.getNoticeBoardNames();
                    List<String> ensNames = NoticeToNoticesConverter.expectNoticeBoardsToList(ensNamesExpr);
                    ensNames.forEach(nb -> expectedNoticeBoards.add(nb));
                    break;
                case POST_NOTICE:
                    PostNotice pn = inst.cast();
                    postNoticeBoards.add(pn.getNoticeBoardName());
                    insts.set(i, NoticeToNoticesConverter.postNoticeToPostNotices(pn));
                    break;
                case POST_NOTICES:
                    PostNotices pns = inst.cast();
                    pns.getNoticeBoardNames().forEach(nb -> postNoticeBoards.add(nb));
                    break;
                case IF:
                    IfElse ie = inst.cast();
                    setWorkflowPositionsAndForkListVariables(extendArray(pos, "then"), ie.getThen().getInstructions(), forkListVariables,
                            expectedNoticeBoards, postNoticeBoards, workflowNamesFromAddOrders, skippedLabels);
                    if (ie.getElse() != null) {
                        setWorkflowPositionsAndForkListVariables(extendArray(pos, "else"), ie.getElse().getInstructions(), forkListVariables,
                                expectedNoticeBoards, postNoticeBoards, workflowNamesFromAddOrders, skippedLabels);
                    }
                    break;
                case TRY:
                    TryCatch tc = inst.cast();
                    setWorkflowPositionsAndForkListVariables(extendArray(pos, "try"), tc.getTry().getInstructions(), forkListVariables,
                            expectedNoticeBoards, postNoticeBoards, workflowNamesFromAddOrders, skippedLabels);
                    if (tc.getCatch() != null) {
                        setWorkflowPositionsAndForkListVariables(extendArray(pos, "catch"), tc.getCatch().getInstructions(), forkListVariables,
                                expectedNoticeBoards, postNoticeBoards, workflowNamesFromAddOrders, skippedLabels);
                    }
                    break;
                case LOCK:
                    Lock l = inst.cast();
                    setWorkflowPositionsAndForkListVariables(extendArray(pos, "lock"), l.getLockedWorkflow().getInstructions(), forkListVariables,
                            expectedNoticeBoards, postNoticeBoards, workflowNamesFromAddOrders, skippedLabels);
                    break;
                case ADD_ORDER:
                    AddOrder ao = inst.cast();
                    workflowNamesFromAddOrders.add(ao.getWorkflowName());
                    break;
                case CYCLE:
                    Cycle c = inst.cast();
                    setWorkflowPositionsAndForkListVariables(extendArray(pos, "cycle"), c.getCycleWorkflow().getInstructions(), forkListVariables,
                            expectedNoticeBoards, postNoticeBoards, workflowNamesFromAddOrders, skippedLabels);
                    break;
                case EXECUTE_NAMED:
                    NamedJob nj = inst.cast();
                    if (skippedLabels.contains(nj.getLabel())) {
                        inst.setState(getState(InstructionStateText.SKIPPED));
                    }
                    break;
                default:
                    break;
                }
            }
        }
    }
    
    private static void setWorkflowPositions(Object[] parentPosition, List<Instruction> insts) {
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
                    if (f.getBranches() != null) {
                        for (Branch b : f.getBranches()) {
                            if (b.getWorkflow().getInstructions() != null) {
                                b.getWorkflow().getInstructions().add(createImplicitEndInstruction());
                            } else {
                                b.getWorkflow().setInstructions(Collections.singletonList(createImplicitEndInstruction()));
                            }
                            setWorkflowPositions(extendArray(pos, "fork+" + b.getId()), b.getWorkflow().getInstructions());
                        }
                    }
                    break;
                case FORKLIST:
                    ForkList fl = inst.cast();
                    if (fl.getWorkflow() != null) {
                        if (fl.getWorkflow().getInstructions() != null) {
                            fl.getWorkflow().getInstructions().add(createImplicitEndInstruction());
                        } else {
                            fl.getWorkflow().setInstructions(Collections.singletonList(createImplicitEndInstruction()));
                        }
                        setWorkflowPositions(extendArray(pos, "fork"), fl.getWorkflow().getInstructions());
                    }
                    break;
                case EXPECT_NOTICE:
                    insts.set(i, NoticeToNoticesConverter.expectNoticeToExpectNotices(inst.cast()));
                    break;
                case POST_NOTICE:
                    insts.set(i, NoticeToNoticesConverter.postNoticeToPostNotices(inst.cast()));
                    break;
                case IF:
                    IfElse ie = inst.cast();
                    if (ie.getThen() != null) {
                        setWorkflowPositions(extendArray(pos, "then"), ie.getThen().getInstructions());
                    }
                    if (ie.getElse() != null) {
                        setWorkflowPositions(extendArray(pos, "else"), ie.getElse().getInstructions());
                    }
                    break;
                case TRY:
                    TryCatch tc = inst.cast();
                    if (tc.getTry() != null) {
                        setWorkflowPositions(extendArray(pos, "try"), tc.getTry().getInstructions());
                    }
                    if (tc.getCatch() != null) {
                        setWorkflowPositions(extendArray(pos, "catch"), tc.getCatch().getInstructions());
                    }
                    break;
                case LOCK:
                    Lock l = inst.cast();
                    if (l.getLockedWorkflow() != null) {
                        setWorkflowPositions(extendArray(pos, "lock"), l.getLockedWorkflow().getInstructions());
                    }
                    break;
                case CYCLE:
                    Cycle c = inst.cast();
                    if (c.getCycleWorkflow() != null) {
                        setWorkflowPositions(extendArray(pos, "cycle"), c.getCycleWorkflow().getInstructions());
                    }
                    break;
                default:
                    break;
                }
            }
        }
    }
    
    private static void setWorkflowAddOrderPositions(Object[] parentPosition, List<Instruction> insts, Set<Position> positions) {
        if (insts != null) {
            for (int i = 0; i < insts.size(); i++) {
                Object[] pos = extendArray(parentPosition, i);
                pos[parentPosition.length] = i;
                Instruction inst = insts.get(i);
                Position p = new Position();
                p.setPosition(Arrays.asList(pos));
                if (p.getPosition().size() > 3) {
                    continue;
                }
                p.setPositionString(getJPositionString(p.getPosition()));
                p.setType(inst.getTYPE().value().replace("Execute.Named", "Job"));
                positions.add(p);
//                inst.setPosition(Arrays.asList(pos));
//                inst.setPositionString(getJPositionString(inst.getPosition()));
                switch (inst.getTYPE()) {
                case EXECUTE_NAMED:
                    NamedJob j = inst.cast();
                    p.setLabel(j.getLabel());
                    break;
                case FORK:
//                    ForkJoin f = inst.cast();
//                    for (Branch b : f.getBranches()) {
//                        if (b.getWorkflow().getInstructions() != null) {
//                            b.getWorkflow().getInstructions().add(createImplicitEndInstruction());
//                        } else {
//                            b.getWorkflow().setInstructions(Collections.singletonList(createImplicitEndInstruction()));
//                        }
//                        setWorkflowAddOrderPositions(extendArray(pos, "fork+" + b.getId()), b.getWorkflow().getInstructions(), positions);
//                    }
                    break;
                case FORKLIST:
//                    ForkList fl = inst.cast();
//                    if (fl.getWorkflow().getInstructions() != null) {
//                        fl.getWorkflow().getInstructions().add(createImplicitEndInstruction());
//                    } else {
//                        fl.getWorkflow().setInstructions(Collections.singletonList(createImplicitEndInstruction()));
//                    }
//                    setWorkflowAddOrderPositions(extendArray(pos, "fork"), fl.getWorkflow().getInstructions(), positions);
                    break;
                case IF:
                    IfElse ie = inst.cast();
                    if (ie.getThen() != null) {
                        setWorkflowAddOrderPositions(extendArray(pos, "then"), ie.getThen().getInstructions(), positions);
                    }
//                    if (ie.getElse() != null) {
//                        setWorkflowAddOrderPositions(extendArray(pos, "else"), ie.getElse().getInstructions(), positions);
//                    }
                    break;
                case TRY:
                    TryCatch tc = inst.cast();
                    if (tc.getTry() != null) {
                        setWorkflowAddOrderPositions(extendArray(pos, "try"), tc.getTry().getInstructions(), positions);
                    }
//                    if (tc.getCatch() != null) {
//                        setWorkflowAddOrderPositions(extendArray(pos, "catch"), tc.getCatch().getInstructions(), positions);
//                    }
                    break;
                case LOCK:
//                    Lock l = inst.cast();
//                    setWorkflowAddOrderPositions(extendArray(pos, "lock"), l.getLockedWorkflow().getInstructions(), positions);
                    break;
                case CYCLE:
//                    Cycle c = inst.cast();
//                    setWorkflowAddOrderPositions(extendArray(pos, "cycle"), c.getCycleWorkflow().getInstructions(), positions);
                    break;
                default:
                    break;
                }
            }
        }
    }
    
    public static void updateWorkflowBoardname(Map<String, String> oldNewBoardNames, List<Instruction> insts) {
        if (oldNewBoardNames == null || oldNewBoardNames.isEmpty()) {
            return;
        }
        if (insts != null) {
            for (int i = 0; i < insts.size(); i++) {
                Instruction inst = insts.get(i);
                switch (inst.getTYPE()) {
                case FORK:
                    ForkJoin f = inst.cast();
                    for (Branch b : f.getBranches()) {
                        if (b.getWorkflow() != null) {
                            updateWorkflowBoardname(oldNewBoardNames, b.getWorkflow().getInstructions());
                        }
                    }
                    break;
                case FORKLIST:
                    ForkList fl = inst.cast();
                    if (fl.getWorkflow() != null) {
                        updateWorkflowBoardname(oldNewBoardNames, fl.getWorkflow().getInstructions());
                    }
                    break;
                case IF:
                    IfElse ie = inst.cast();
                    if (ie.getThen() != null) {
                        updateWorkflowBoardname(oldNewBoardNames, ie.getThen().getInstructions());
                    }
                    if (ie.getElse() != null) {
                        updateWorkflowBoardname(oldNewBoardNames, ie.getElse().getInstructions());
                    }
                    break;
                case TRY:
                    TryCatch tc = inst.cast();
                    if (tc.getTry() != null) {
                        updateWorkflowBoardname(oldNewBoardNames, tc.getTry().getInstructions());
                    }
                    if (tc.getCatch() != null) {
                        updateWorkflowBoardname(oldNewBoardNames, tc.getCatch().getInstructions());
                    }
                    break;
                case LOCK:
                    Lock l = inst.cast();
                    updateWorkflowBoardname(oldNewBoardNames, l.getLockedWorkflow().getInstructions());
                    break;
                case CYCLE:
                    Cycle c = inst.cast();
                    updateWorkflowBoardname(oldNewBoardNames, c.getCycleWorkflow().getInstructions());
                    break;
                case EXPECT_NOTICE:
                    ExpectNotice en = inst.cast();
                    for (Map.Entry<String, String> oldNewBoardName : oldNewBoardNames.entrySet()) {
                        String oldBoardName = oldNewBoardName.getKey();
                        String newBoardName = oldNewBoardName.getValue();
                        if (newBoardName.isEmpty()) {
                            continue;
                        }
                        if (oldBoardName.equals(en.getNoticeBoardName())) {
                            en.setNoticeBoardName(newBoardName);
                            break;
                        }
                    }
                    break;
                case EXPECT_NOTICES:
                    ExpectNotices ens = inst.cast();
                    String ensNamesExpr = ens.getNoticeBoardNames();
                    List<String> ensNames = NoticeToNoticesConverter.expectNoticeBoardsToList(ensNamesExpr);
                    for (Map.Entry<String, String> oldNewBoardName : oldNewBoardNames.entrySet()) {
                        String oldBoardName = oldNewBoardName.getKey();
                        String newBoardName = oldNewBoardName.getValue();
                        if (newBoardName.isEmpty()) {
                            continue;
                        }
                        if (!ensNames.isEmpty() && ensNames.contains(oldBoardName)) {
                            ensNamesExpr = ensNamesExpr.replace("'" + oldBoardName + "'", "'" + newBoardName + "'").replace("\"" + oldBoardName
                                    + "\"", "\"" + newBoardName + "\"");
                        }
                    }
                    ens.setNoticeBoardNames(ensNamesExpr);
                    break;
                case POST_NOTICE:
                    PostNotice pn = inst.cast();
                    for (Map.Entry<String, String> oldNewBoardName : oldNewBoardNames.entrySet()) {
                        String oldBoardName = oldNewBoardName.getKey();
                        String newBoardName = oldNewBoardName.getValue();
                        if (newBoardName.isEmpty()) {
                            continue;
                        }
                        if (oldBoardName.equals(pn.getNoticeBoardName())) {
                            pn.setNoticeBoardName(newBoardName);
                            break;
                        }
                    }
                    break;
                case POST_NOTICES:
                    PostNotices pns = inst.cast();
                    pns.getNoticeBoardNames().removeIf(pnb -> oldNewBoardNames.keySet().contains(pnb) || oldNewBoardNames.values().contains(pnb));
                    pns.getNoticeBoardNames().addAll(oldNewBoardNames.values().stream().filter(s -> !s.isEmpty()).collect(Collectors.toSet()));
                    break;
                default:
                    break;
                }
            }
        }
    }
    
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
                case CYCLE:
                    Cycle c = inst.cast();
                    extractImplicitEnds(c.getCycleWorkflow().getInstructions(), posSet, true);
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
        SyncStateText stateText = SyncStateText.UNKNOWN;
        if (currentstate != null) {
            JWorkflowId wId = JWorkflowId.of(JocInventory.pathToName(workflow.getPath()), workflow.getVersionId());
            stateText = SyncStateHelper.getWorkflowState(currentstate.repo().idToCheckedWorkflow(wId), currentstate);
        }
        return SyncStateHelper.getState(stateText);
    }
    
    public static boolean getSuspended(SyncState syncState) {
        SyncStateText stateText = syncState.get_text();
        return SyncStateText.SUSPENDED.equals(stateText) || SyncStateText.SUSPENDING.equals(stateText);
    }

    public static Boolean workflowCurrentlyExists(JControllerState currentstate, WorkflowPath workflowPath) {
        Boolean exists = false;
        if (currentstate != null) {
            Either<Problem, JWorkflow> workflowV = currentstate.repo().pathToCheckedWorkflow(workflowPath);
            if (workflowV != null && workflowV.isRight()) {
                exists = true;
            }
        }
        return exists;
    }
    
    public static Boolean workflowCurrentlyExists(JControllerState currentstate, String workflow) {
        return workflowCurrentlyExists(currentstate, WorkflowPath.of(workflow));
    }
    
    public static Map<String, List<FileOrderSource>> workflowToFileOrderSources(JControllerState controllerState, String controllerId,
            Set<String> workflowNames, DeployedConfigurationDBLayer dbLayer) {
        Set<String> syncFileOrderSources = controllerState == null ? Collections.emptySet() : controllerState.pathToFileWatch().values().stream()
                .parallel().map(f -> f.workflowPath().string()).collect(Collectors.toSet());

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
    
    public static List<FileOrderSource> workflowToFileOrderSources(JControllerState controllerState, String controllerId, String workflowName,
            DeployedConfigurationDBLayer dbLayer) {
        Set<String> fileWatchNames = WorkflowsHelper.workflowToFileWatchNames(controllerState, workflowName);
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
    
    private static Set<String> workflowToFileWatchNames(JControllerState controllerState, String workflowName) {
        return controllerState.pathToFileWatch().values().stream().parallel().filter(f -> f.workflowPath().string().equals(workflowName)).map(f -> f.path()
                .string()).collect(Collectors.toSet());
    }
    
    public static ConcurrentMap<JWorkflowId, Map<OrderStateText, Integer>> getGroupedOrdersCountPerWorkflow(JControllerState currentstate,
            WorkflowOrderCountFilter workflowsFilter, Set<Folder> permittedFolders) {

        final Instant surveyInstant = currentstate.instant();
        long surveyDateMillis = surveyInstant.toEpochMilli();
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
                        Instant scheduledFor = OrdersHelper.getScheduledForInstant(o);
                        if (scheduledFor != null && scheduledFor.isAfter(until)) {
                            if (scheduledFor.toEpochMilli() == JobSchedulerDate.NEVER_MILLIS.longValue()) {
                                return true;
                            }
                            return false;
                        }
                    }
                    return true;
                };
        }
        
        Set<VersionedItemId<WorkflowPath>> workflows2 = workflowsFilter.getWorkflowIds().parallelStream().filter(w -> JOCResourceImpl.canAdd(
                WorkflowPaths.getPath(w), permittedFolders)).map(w -> {
                    if (w.getVersionId() == null || w.getVersionId().isEmpty()) {
                        return currentstate.repo().pathToCheckedWorkflow(WorkflowPath.of(JocInventory.pathToName(w.getPath())));
                    } else {
                        return currentstate.repo().idToCheckedWorkflow(JWorkflowId.of(JocInventory.pathToName(w.getPath()), w.getVersionId()));
                    }
                }).filter(Either::isRight).map(Either::get).map(JWorkflow::id).map(JWorkflowId::asScala).collect(Collectors.toSet());

        Function1<Order<Order.State>, Object> workflowFilter = o -> workflows2.contains(o.workflowId());
        Function1<Order<Order.State>, Object> finishedFilter = JOrderPredicates.or(JOrderPredicates.or(JOrderPredicates.byOrderState(
                Order.Finished$.class), JOrderPredicates.byOrderState(Order.Cancelled$.class)), JOrderPredicates.byOrderState(
                        Order.ProcessingKilled$.class));
        Function1<Order<Order.State>, Object> suspendFilter = JOrderPredicates.and(o -> o.isSuspended(), JOrderPredicates.not(finishedFilter));
        Function1<Order<Order.State>, Object> cycledOrderFilter = JOrderPredicates.and(JOrderPredicates.byOrderState(Order.Fresh$.class),
                JOrderPredicates.and(o -> OrdersHelper.isCyclicOrderId(o.id().string()), JOrderPredicates.not(suspendFilter)));
        Function1<Order<Order.State>, Object> notCycledOrderFilter = JOrderPredicates.not(cycledOrderFilter);
        
        Function1<Order<Order.State>, Object> blockedFilter = JOrderPredicates.and(JOrderPredicates.byOrderState(Order.Fresh$.class), o -> !o
                .isSuspended() && OrdersHelper.getScheduledForMillis(o, surveyDateMillis) < surveyDateMillis);

        Set<JOrder> blockedOrders = currentstate.ordersBy(JOrderPredicates.and(workflowFilter, blockedFilter)).collect(Collectors.toSet());
        ConcurrentMap<OrderId, JOrder> blockedButWaitingForAdmissionOrders = OrdersHelper.getWaitingForAdmissionOrders(blockedOrders, currentstate);
        Set<OrderId> blockedButWaitingForAdmissionOrderIds = blockedButWaitingForAdmissionOrders.keySet();
        
        Stream<JOrder> cycledOrderStream = currentstate.ordersBy(JOrderPredicates.and(workflowFilter, cycledOrderFilter)).parallel().filter(
                dateToFilter);
        Stream<JOrder> notCycledOrderStream = currentstate.ordersBy(JOrderPredicates.and(workflowFilter, notCycledOrderFilter)).parallel().filter(
                dateToFilter);
        Comparator<JOrder> comp = Comparator.comparing(o -> o.id().string());
        Collection<TreeSet<JOrder>> cycledOrderColl = cycledOrderStream.filter(o -> !blockedButWaitingForAdmissionOrderIds.contains(o.id())).collect(
                Collectors.groupingBy(o -> OrdersHelper.getCyclicOrderIdMainPart(o.id().string()), Collectors.toCollection(() -> new TreeSet<>(
                        comp)))).values();
        cycledOrderStream = cycledOrderColl.stream().parallel().map(t -> t.first());
        notCycledOrderStream = Stream.concat(notCycledOrderStream, blockedButWaitingForAdmissionOrders.values().stream()).distinct();
        ConcurrentMap<JWorkflowId, Map<OrderStateText, Integer>> groupedOrdersCount = Stream.concat(notCycledOrderStream, cycledOrderStream).collect(
                Collectors.groupingByConcurrent(JOrder::workflowId, Collectors.groupingBy(o -> groupingByState(o, surveyInstant,
                        blockedButWaitingForAdmissionOrderIds), Collectors.reducing(0, e -> 1, Integer::sum))));

        workflows2.forEach(w -> groupedOrdersCount.putIfAbsent(JWorkflowId.apply(w), Collections.emptyMap()));
        
        return groupedOrdersCount;
    }
    
    private static OrderStateText groupingByState(JOrder order, Instant surveyInstant, Set<OrderId> blockedButWaitingForAdmissionOrderIds) {
        OrderStateText groupedState = OrdersHelper.getGroupedState(order.asScala().state().getClass());
        if (order.asScala().isSuspended() && !(OrderStateText.CANCELLED.equals(groupedState) || OrderStateText.FINISHED.equals(groupedState))) {
            groupedState = OrderStateText.SUSPENDED;
        }
        if (OrderStateText.SCHEDULED.equals(groupedState)) {
            Instant scheduledInstant = OrdersHelper.getScheduledForInstant(order);
            if (scheduledInstant != null) {
                if (JobSchedulerDate.NEVER_MILLIS.longValue() == scheduledInstant.toEpochMilli()) {
                    groupedState = OrderStateText.PENDING;
                } else if (scheduledInstant.isBefore(surveyInstant)) {
                    if (blockedButWaitingForAdmissionOrderIds.contains(order.id())) {
                        groupedState = OrderStateText.INPROGRESS;
                    } else {
                        groupedState = OrderStateText.BLOCKED;
                    }
                }
            }
        }
        return groupedState;
    }
    
    public static CompletableFuture<Either<Exception, Void>> storeAuditLogDetailsFromWorkflowPath(WorkflowPath workflowPath,
            DBItemJocAuditLog dbAuditLog, String controllerId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                JocAuditLog.storeAuditLogDetail(new AuditLogDetail(WorkflowPaths.getPath(workflowPath.string()), ObjectType.WORKFLOW.intValue(),
                        controllerId), null, dbAuditLog);
                return Either.right(null);
            } catch (Exception e) {
                return Either.left(e);
            }
        });
    }
    
    public static Set<String> getSkippedLabels(JControllerState currentstate, String workflowName, boolean compact) {
        if (!compact && currentstate != null) {
            WorkflowPathControlState controlState = JavaConverters.asJava(currentstate.asScala().pathToWorkflowPathControlState_()).get(
                    WorkflowPath.of(workflowName));
            return getSkippedLabels(controlState, compact);
        }
        return Collections.emptySet();
    }
    
    public static Set<String> getSkippedLabels(WorkflowPathControlState controlState, boolean compact) {
        if (!compact) {
            if (controlState != null) {
                return JavaConverters.asJava(controlState.workflowPathControl().skip()).stream().map(Label::string).collect(
                        Collectors.toSet());
            }
        }
        return Collections.emptySet();
    }
    
    public static Map<WorkflowPath, WorkflowPathControlState> getWorkflowPathControlStates(JControllerState currentstate, boolean compact) {
        if (!compact && currentstate != null) {
            return JavaConverters.asJava(currentstate.asScala().pathToWorkflowPathControlState_());
        }
        return Collections.emptyMap();
    }
    
}
