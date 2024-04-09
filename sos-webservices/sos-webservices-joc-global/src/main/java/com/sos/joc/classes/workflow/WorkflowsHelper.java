package com.sos.joc.classes.workflow;

import java.time.Instant;
import java.time.ZoneId;
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
import java.util.Optional;
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
import com.sos.inventory.model.instruction.ConsumeNotices;
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
import com.sos.inventory.model.instruction.Instructions;
import com.sos.inventory.model.instruction.Lock;
import com.sos.inventory.model.instruction.NamedJob;
import com.sos.inventory.model.instruction.Options;
import com.sos.inventory.model.instruction.PostNotice;
import com.sos.inventory.model.instruction.PostNotices;
import com.sos.inventory.model.instruction.StickySubagent;
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
import com.sos.joc.classes.inventory.LockToLockDemandsConverter;
import com.sos.joc.classes.inventory.NoticeToNoticesConverter;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.deploy.DeployedConfigurationFilter;
import com.sos.joc.db.deploy.items.DeployedContent;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.model.audit.ObjectType;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.order.BlockPosition;
import com.sos.joc.model.order.OrderStateText;
import com.sos.joc.model.order.Position;
import com.sos.joc.model.workflow.WorkflowOrderCountFilter;
import com.sos.joc.model.workflow.WorkflowsFilter;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.item.Repo;
import js7.data.item.VersionedItemId;
import js7.data.order.Order;
import js7.data.order.OrderId;
import js7.data.workflow.WorkflowPath;
import js7.data.workflow.WorkflowPathControl;
import js7.data.workflow.WorkflowPathControlPath;
import js7.data.workflow.position.Label;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JOrder;
import js7.data_for_java.order.JOrderPredicates;
import js7.data_for_java.workflow.JWorkflow;
import js7.data_for_java.workflow.JWorkflowControl;
import js7.data_for_java.workflow.JWorkflowControlId;
import js7.data_for_java.workflow.JWorkflowId;
import js7.data_for_java.workflow.position.JBranchPath;
import js7.data_for_java.workflow.position.JPosition;
import scala.Function1;
import scala.collection.JavaConverters;
import scala.jdk.javaapi.OptionConverters;

public class WorkflowsHelper {
    
    public static final Map<InstructionStateText, Integer> instructionStates = Collections.unmodifiableMap(
            new HashMap<InstructionStateText, Integer>() {

                private static final long serialVersionUID = 1L;

                {
                    put(InstructionStateText.SKIPPED, 5);
                    put(InstructionStateText.STOPPED, 2);
                    put(InstructionStateText.STOPPED_AND_SKIPPED, 2);
                }
            });

    public static InstructionState getState(InstructionStateText stateText) {
        InstructionState state = new InstructionState();
        state.set_text(stateText);
        state.setSeverity(instructionStates.get(stateText));
        return state;
    }

    private static Stream<WorkflowId> oldWorkflowIds(JControllerState currentState) {
        return oldJWorkflowIds(currentState).map(o -> new WorkflowId(o.path().string(), o.versionId().string()));
    }

    public static Stream<JWorkflowId> oldJWorkflowIds(JControllerState currentState) {
        Repo repo = currentState.asScala().repo();
        Function1<Order<Order.State>, Object> isOldWorkflow = o -> !repo.isCurrentItem(o.workflowId());
        return currentState.ordersBy(isOldWorkflow).map(JOrder::workflowId).distinct();
    }

    public static ImplicitEnd createImplicitEndInstruction() {
        ImplicitEnd i = new ImplicitEnd();
        i.setTYPE(InstructionType.IMPLICIT_END);
        return i;
    }

    public static <T extends Workflow> T addWorkflowPositionsAndForkListVariablesAndExpectedNoticeBoards(T w, Set<String> skippedLabels,
            Set<JPosition> stoppedPositions) {
        if (w == null) {
            return null;
        }
        List<Instruction> instructions = w.getInstructions();
        Set<String> expectedNoticeBoards = new LinkedHashSet<>();
        Set<String> postNoticeBoards = new LinkedHashSet<>();
        Set<String> consumeNoticeBoards = new LinkedHashSet<>();
        Set<String> workflowNamesFromAddOrders = new LinkedHashSet<>();
        if (instructions != null) {
            instructions.add(createImplicitEndInstruction());
            w.setForkListVariables(new LinkedHashSet<>());
        } else {
            w.setInstructions(Collections.singletonList(createImplicitEndInstruction()));
        }
        Object[] o = {};
        setWorkflowPositionsAndForkListVariables(o, w.getInstructions(), w.getForkListVariables(), expectedNoticeBoards, postNoticeBoards,
                consumeNoticeBoards, workflowNamesFromAddOrders, skippedLabels, stoppedPositions);
        if (w.getForkListVariables() == null || w.getForkListVariables().isEmpty()) {
            w.setForkListVariables(null);
        }
        if (w instanceof WorkflowDeps) {
            setInitialDeps((WorkflowDeps) w, expectedNoticeBoards, postNoticeBoards, consumeNoticeBoards, workflowNamesFromAddOrders);
        } else {
            if (w.getHasAddOrderDependencies() != Boolean.TRUE) {
                w.setHasAddOrderDependencies(!workflowNamesFromAddOrders.isEmpty());
            }
            w.setHasExpectedNoticeBoards(!expectedNoticeBoards.isEmpty());
            w.setHasPostNoticeBoards(!postNoticeBoards.isEmpty());
            w.setHasConsumeNoticeBoards(!consumeNoticeBoards.isEmpty());
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
    
    public static Map<String, List<Object>> getLabelToPositionsMap(com.sos.inventory.model.workflow.Workflow w) {
        return getLabelToPositionsMap(w, false);
    }
    
    public static Map<String, List<Object>> getLabelToPositionsMap(com.sos.inventory.model.workflow.Workflow w, boolean withAllPositions) {
        if (w == null) {
            return Collections.emptyMap();
        }
        List<Instruction> instructions = w.getInstructions();
        if (instructions != null) {
            instructions.add(createImplicitEndInstruction());
        } else {
            w.setInstructions(Collections.singletonList(createImplicitEndInstruction()));
        }
        Object[] o = {};
        Map<String, List<Object>> labelToPositionsMap = new HashMap<>();
        setWorkflowPositions(o, w.getInstructions(), labelToPositionsMap, withAllPositions);
        return labelToPositionsMap;
    }
    
    public static Map<String, List<Object>> getLabelToPositionsMapFromInventory(String workflowPath) {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection("GetPositionFromLabel");
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            List<DBItemInventoryConfiguration> configs = dbLayer.getConfigurationByName(workflowPath, ConfigurationType.WORKFLOW.intValue());
            if (configs != null && !configs.isEmpty()) {
                DBItemInventoryConfiguration config = configs.get(0);
                try {
                    com.sos.inventory.model.workflow.Workflow w = JocInventory.workflowContent2Workflow(config.getContent());
                    Map<String, List<Object>> labelMap = getLabelToPositionsMap(w);
                    labelMap.putAll(getWorkflowBlockPositions(w.getInstructions()).stream().filter(p -> p.getLabel() != null).collect(Collectors
                            .toMap(BlockPosition::getLabel, BlockPosition::getPosition)));
                    return labelMap;
                } catch (Exception e) {
                    throw new DBInvalidDataException(e);
                }
            }
            return Collections.emptyMap();
        } finally {
            Globals.disconnect(session);
        }
    }
    
    public static Map<String, List<Object>> getLabelToPositionsMapFromDepHistory(String controllerId, String workflowPath, String workflowVersionId) {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection("GetPositionFromLabel");
            DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(session);
            DeployedContent config = null;
            if (workflowVersionId == null || workflowVersionId.isEmpty()) {
                config = dbLayer.getDeployedInventory(controllerId, ConfigurationType.WORKFLOW.intValue(), workflowPath);
            } else {
                config = dbLayer.getDeployedInventory(controllerId, ConfigurationType.WORKFLOW.intValue(), workflowPath, workflowVersionId);
            }
            if (config != null) {
                try {
                    return getLabelToPositionsMap(JocInventory.workflowContent2Workflow(config.getContent()));
                } catch (Exception e) {
                    throw new DBInvalidDataException(e);
                }
            } else {
                throw new DBMissingDataException("Couldn't find workflow '" + workflowPath + "'.");
            }
        } finally {
            Globals.disconnect(session);
        }
    }
    
    public static Map<List<Object>, String> getPositionToLabelsMapFromDepHistory(String controllerId, WorkflowId workflowId) {
        Map<String, List<Object>> map = getLabelToPositionsMapFromDepHistory(controllerId, workflowId.getPath(), workflowId.getVersionId());
        return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey)); 
    }
    
    public static Map<List<Object>, String> getPositionToLabelsMapFromDepHistory(String controllerId, JWorkflowId workflowId) {
        Map<String, List<Object>> map = getLabelToPositionsMapFromDepHistory(controllerId, workflowId.path().string(), workflowId.versionId().string());
        return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey)); 
    }
    
    public static Set<Position> getWorkflowAddOrderPositions(List<Instruction> instructions) {
        Object[] pos = {};
        return getWorkflowAddOrderPositions(instructions, pos);
    }
    
    public static Set<Position> getWorkflowAddOrderPositions(List<Instruction> instructions, Object[] pos) {
        if (instructions != null) {
            instructions.add(createImplicitEndInstruction());
        } else {
            instructions = Collections.singletonList(createImplicitEndInstruction());
        }
        Set<Position> positions = new LinkedHashSet<Position>();
        setWorkflowAddOrderPositions(pos, pos.length, instructions, positions);
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

    public static Set<WorkflowPath> getWorkflowPathsFromFolders(String controllerId, List<Folder> folders, JControllerState currentstate,
            Set<Folder> permittedFolders) {

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
            workflowsFilter.setTags(null);
        }

        return contents;
    }

    public static Stream<DeployedContent> getDeployedContentsStream(WorkflowsFilter workflowsFilter, DeployedConfigurationDBLayer dbLayer,
            List<DeployedContent> contents, Set<Folder> permittedFolders) {

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

        if (workflowsFilter.getAgentNames() != null && !workflowsFilter.getAgentNames().isEmpty()) {
            InventoryAgentInstancesDBLayer agentDbLayer = new InventoryAgentInstancesDBLayer(dbLayer.getSession());
            Map<String, Set<String>> agentNamesAndAliasesPerId = agentDbLayer.getAgentWithAliasesByControllerIds(Collections.singleton(workflowsFilter
                    .getControllerId())).get(workflowsFilter.getControllerId());
            
            Set<String> agentNamesAndAliases = new HashSet<>();
            if (agentNamesAndAliasesPerId != null) {
//                for (String agentName : workflowsFilter.getAgentNames()) {
//                    for (Set<String> a : agentNamesAndAliasesPerId.values()) {
//                        if (a.contains(agentName)) {
//                            agentNamesAndAliases.addAll(a);
//                            break;
//                        }
//                    }
//                }
                agentNamesAndAliasesPerId.forEach((k, v) -> {
                    Set<String> copy = new HashSet<>(v);
                    copy.retainAll(workflowsFilter.getAgentNames());
                    if (!copy.isEmpty()) {
                        agentNamesAndAliases.addAll(v);
                    }
                });
            }

            Predicate<String> pred = Pattern.compile("\"agentName\"\\s*:\\s*\"" + String.join("|", agentNamesAndAliases) + "\"",
                    Pattern.CASE_INSENSITIVE).asPredicate();
            contentsStream = contentsStream.filter(w -> pred.test(w.getContent()));
        }

        return contentsStream;
    }

    private static List<DeployedContent> getPermanentDeployedContent(WorkflowsFilter workflowsFilter, DeployedConfigurationDBLayer dbLayer,
            Set<Folder> permittedFolders) {
        DeployedConfigurationFilter dbFilter = new DeployedConfigurationFilter();
        dbFilter.setControllerId(workflowsFilter.getControllerId());
        dbFilter.setObjectTypes(Collections.singleton(DeployType.WORKFLOW.intValue()));
        dbFilter.setTags(workflowsFilter.getTags());

        List<WorkflowId> workflowIds = workflowsFilter.getWorkflowIds();
        if (workflowIds != null && !workflowIds.isEmpty()) {
            workflowsFilter.setFolders(null);
            workflowsFilter.setRegex(null);
            workflowsFilter.setTags(null);
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
                dbFilter.setNames(workflowMap.get(false).parallelStream().map(WorkflowId::getPath).map(JocInventory::pathToName).collect(Collectors
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
            // List<String> jsons = oldJWorkflowIds(currentState).map(wId ->
            // currentState.repo().idToCheckedWorkflow(wId)).filter(Either::isRight).map(Either::get).map(JWorkflow::toJson).collect(Collectors.toList());

            DeployedConfigurationFilter dbFilter = new DeployedConfigurationFilter();
            dbFilter.setControllerId(workflowsFilter.getControllerId());
            dbFilter.setObjectTypes(Collections.singleton(DeployType.WORKFLOW.intValue()));
            dbFilter.setTags(workflowsFilter.getTags());
            // if (permittedFolders != null && !permittedFolders.isEmpty()) {
            // dbFilter.setFolders(permittedFolders);
            // }

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
            Set<String> consumeNoticeBoards, Set<String> workflowNamesFromAddOrders) {
        if (w.getExpectedNoticeBoards() == null) {
            w.setExpectedNoticeBoards(new BoardWorkflows());
        }
        if (w.getPostNoticeBoards() == null) {
            w.setPostNoticeBoards(new BoardWorkflows());
        }
        if (w.getConsumeNoticeBoards() == null) {
            w.setConsumeNoticeBoards(new BoardWorkflows());
        }
        consumeNoticeBoards.forEach(board -> w.getConsumeNoticeBoards().setAdditionalProperty(board, Collections.emptyList()));
        expectedNoticeBoards.forEach(board -> w.getExpectedNoticeBoards().setAdditionalProperty(board, Collections.emptyList()));
        postNoticeBoards.forEach(board -> w.getPostNoticeBoards().setAdditionalProperty(board, Collections.emptyList()));
        if (workflowNamesFromAddOrders != null) {
            w.setAddOrderToWorkflows(workflowNamesFromAddOrders.stream().distinct().map(name -> new WorkflowId(name, null)).collect(Collectors
                    .toList()));
        } else {
            w.setAddOrderToWorkflows(Collections.emptyList());
        }
    }

    private static void setWorkflowPositionsAndForkListVariables(Object[] parentPosition, List<Instruction> insts, Set<String> forkListVariables,
            Set<String> expectedNoticeBoards, Set<String> postNoticeBoards, Set<String> consumeNoticeBoards, Set<String> workflowNamesFromAddOrders,
            Set<String> skippedLabels, Set<JPosition> stoppedPositions) {
        if (insts != null) {
            for (int i = 0; i < insts.size(); i++) {
                Object[] pos = extendArray(parentPosition, i);
                pos[parentPosition.length] = i;
                Instruction inst = insts.get(i);
                inst.setPosition(Arrays.asList(pos));
                JPosition jPos = getJPosition(inst.getPosition());
                if (stoppedPositions.contains(jPos)) {
                    inst.setState(getState(InstructionStateText.STOPPED));
                }
                inst.setPositionString(getJPositionString(jPos));
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
                                forkListVariables, expectedNoticeBoards, postNoticeBoards, consumeNoticeBoards, workflowNamesFromAddOrders,
                                skippedLabels, stoppedPositions);
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
                            expectedNoticeBoards, postNoticeBoards, consumeNoticeBoards, workflowNamesFromAddOrders, skippedLabels, stoppedPositions);
                    break;
                case EXPECT_NOTICE:
                    ExpectNotice en = inst.cast();
                    expectedNoticeBoards.add(en.getNoticeBoardName());
                    insts.set(i, NoticeToNoticesConverter.expectNoticeToExpectNotices(en));
                    break;
                case EXPECT_NOTICES:
                    ExpectNotices ens = inst.cast();
                    String ensNamesExpr = ens.getNoticeBoardNames();
                    NoticeToNoticesConverter.expectNoticeBoardsToStream(ensNamesExpr).forEach(nb -> expectedNoticeBoards.add(nb));
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
                case CONSUME_NOTICES:
                    ConsumeNotices cns = inst.cast();
                    String cnsNamesExpr = cns.getNoticeBoardNames();
                    List<String> cnsNames = NoticeToNoticesConverter.expectNoticeBoardsToList(cnsNamesExpr);
                    cnsNames.forEach(nb -> consumeNoticeBoards.add(nb));
                    if (cns.getSubworkflow() == null || cns.getSubworkflow().getInstructions() == null) {
                        cns.setSubworkflow(new Instructions(Collections.emptyList())); 
                    }
                    setWorkflowPositionsAndForkListVariables(extendArray(pos, "consumeNotices"), cns.getSubworkflow().getInstructions(), forkListVariables,
                            expectedNoticeBoards, postNoticeBoards, consumeNoticeBoards, workflowNamesFromAddOrders, skippedLabels, stoppedPositions);
                    break;
                case IF:
                    IfElse ie = inst.cast();
                    setWorkflowPositionsAndForkListVariables(extendArray(pos, "then"), ie.getThen().getInstructions(), forkListVariables,
                            expectedNoticeBoards, postNoticeBoards, consumeNoticeBoards, workflowNamesFromAddOrders, skippedLabels, stoppedPositions);
                    if (ie.getElse() != null) {
                        setWorkflowPositionsAndForkListVariables(extendArray(pos, "else"), ie.getElse().getInstructions(), forkListVariables,
                                expectedNoticeBoards, postNoticeBoards, consumeNoticeBoards, workflowNamesFromAddOrders, skippedLabels,
                                stoppedPositions);
                    }
                    break;
                case TRY:
                    TryCatch tc = inst.cast();
                    setWorkflowPositionsAndForkListVariables(extendArray(pos, "try"), tc.getTry().getInstructions(), forkListVariables,
                            expectedNoticeBoards, postNoticeBoards, consumeNoticeBoards, workflowNamesFromAddOrders, skippedLabels, stoppedPositions);
                    if (tc.getCatch() != null) {
                        setWorkflowPositionsAndForkListVariables(extendArray(pos, "catch"), tc.getCatch().getInstructions(), forkListVariables,
                                expectedNoticeBoards, postNoticeBoards, consumeNoticeBoards, workflowNamesFromAddOrders, skippedLabels,
                                stoppedPositions);
                    }
                    break;
                case LOCK:
                    Lock l = LockToLockDemandsConverter.lockToInventoryLockDemands(inst.cast());
                    setWorkflowPositionsAndForkListVariables(extendArray(pos, "lock"), l.getLockedWorkflow().getInstructions(), forkListVariables,
                            expectedNoticeBoards, postNoticeBoards, consumeNoticeBoards, workflowNamesFromAddOrders, skippedLabels, stoppedPositions);
                    break;
                case ADD_ORDER:
                    AddOrder ao = inst.cast();
                    workflowNamesFromAddOrders.add(ao.getWorkflowName());
                    break;
                case CYCLE:
                    Cycle c = inst.cast();
                    if (c.getCycleWorkflow().getInstructions() != null) {
                        c.getCycleWorkflow().getInstructions().add(createImplicitEndInstruction());
                    } else {
                        c.getCycleWorkflow().setInstructions(Collections.singletonList(createImplicitEndInstruction()));
                    }
                    setWorkflowPositionsAndForkListVariables(extendArray(pos, "cycle"), c.getCycleWorkflow().getInstructions(), forkListVariables,
                            expectedNoticeBoards, postNoticeBoards, consumeNoticeBoards, workflowNamesFromAddOrders, skippedLabels, stoppedPositions);
                    break;
                case EXECUTE_NAMED:
                    NamedJob nj = inst.cast();
                    if (skippedLabels.contains(nj.getLabel())) {
                        if (inst.getState() != null && InstructionStateText.STOPPED.equals(inst.getState().get_text())) {
                            inst.setState(getState(InstructionStateText.STOPPED_AND_SKIPPED));
                        } else {
                            inst.setState(getState(InstructionStateText.SKIPPED));
                        }
                    }
                    break;
                case STICKY_SUBAGENT:
                    StickySubagent sticky = inst.cast();
                    setWorkflowPositionsAndForkListVariables(extendArray(pos, "stickySubagent"), sticky.getSubworkflow().getInstructions(),
                            forkListVariables, expectedNoticeBoards, postNoticeBoards, consumeNoticeBoards, workflowNamesFromAddOrders, skippedLabels,
                            stoppedPositions);
                    break;
                case OPTIONS:
                    Options opts = inst.cast();
                    setWorkflowPositionsAndForkListVariables(extendArray(pos, "options"), opts.getBlock().getInstructions(), forkListVariables,
                            expectedNoticeBoards, postNoticeBoards, consumeNoticeBoards, workflowNamesFromAddOrders, skippedLabels, stoppedPositions);
                    break;
                default:
                    break;
                }
            }
        }
    }
    
    private static void setWorkflowPositions(Object[] parentPosition, List<Instruction> insts) {
        setWorkflowPositions(parentPosition, insts, null, false);
    }

    private static void setWorkflowPositions(Object[] parentPosition, List<Instruction> insts, Map<String, List<Object>> mapLabelToPos,
            boolean withAllPositions) {
        if (insts != null) {
            for (int i = 0; i < insts.size(); i++) {
                Object[] pos = extendArray(parentPosition, i);
                pos[parentPosition.length] = i;
                Instruction inst = insts.get(i);
                inst.setPosition(Arrays.asList(pos));
                inst.setPositionString(getJPositionString(inst.getPosition()));
                if (mapLabelToPos != null) {
                    if (inst.getLabel() != null && !inst.getLabel().isEmpty()) {
                        mapLabelToPos.putIfAbsent(inst.getLabel(), inst.getPosition());
                    }
                    if (withAllPositions) {
                        mapLabelToPos.putIfAbsent(inst.getPositionString(), inst.getPosition());
                    }
                }
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
                            setWorkflowPositions(extendArray(pos, "fork+" + b.getId()), b.getWorkflow().getInstructions(), mapLabelToPos, withAllPositions);
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
                        setWorkflowPositions(extendArray(pos, "fork"), fl.getWorkflow().getInstructions(), mapLabelToPos, withAllPositions);
                    }
                    break;
                case EXPECT_NOTICE:
                    insts.set(i, NoticeToNoticesConverter.expectNoticeToExpectNotices(inst.cast()));
                    break;
                case POST_NOTICE:
                    insts.set(i, NoticeToNoticesConverter.postNoticeToPostNotices(inst.cast()));
                    break;
                case CONSUME_NOTICES:
                    ConsumeNotices cn = inst.cast();
                    if (cn.getSubworkflow() == null || cn.getSubworkflow().getInstructions() == null) {
                        cn.setSubworkflow(new Instructions(Collections.emptyList())); 
                    }
                    setWorkflowPositions(extendArray(pos, "consumeNotices"), cn.getSubworkflow().getInstructions(), mapLabelToPos, withAllPositions);
                    break;
                case IF:
                    IfElse ie = inst.cast();
                    if (ie.getThen() != null) {
                        setWorkflowPositions(extendArray(pos, "then"), ie.getThen().getInstructions(), mapLabelToPos, withAllPositions);
                    }
                    if (ie.getElse() != null) {
                        setWorkflowPositions(extendArray(pos, "else"), ie.getElse().getInstructions(), mapLabelToPos, withAllPositions);
                    }
                    break;
                case TRY:
                    TryCatch tc = inst.cast();
                    if (tc.getTry() != null) {
                        setWorkflowPositions(extendArray(pos, "try"), tc.getTry().getInstructions(), mapLabelToPos, withAllPositions);
                    }
                    if (tc.getCatch() != null) {
                        setWorkflowPositions(extendArray(pos, "catch"), tc.getCatch().getInstructions(), mapLabelToPos, withAllPositions);
                    }
                    break;
                case LOCK:
                    Lock l = inst.cast();
                    if (l.getLockedWorkflow() != null) {
                        setWorkflowPositions(extendArray(pos, "lock"), l.getLockedWorkflow().getInstructions(), mapLabelToPos, withAllPositions);
                    }
                    break;
                case CYCLE:
                    Cycle c = inst.cast();
                    if (c.getCycleWorkflow() != null) {
                        setWorkflowPositions(extendArray(pos, "cycle"), c.getCycleWorkflow().getInstructions(), mapLabelToPos, withAllPositions);
                    }
                    break;
                case STICKY_SUBAGENT:
                    StickySubagent sticky = inst.cast();
                    if (sticky.getSubworkflow() != null) {
                        setWorkflowPositions(extendArray(pos, "stickySubagent"), sticky.getSubworkflow().getInstructions(), mapLabelToPos, withAllPositions);
                    }
                    break;
                case OPTIONS:
                    Options opts = inst.cast();
                    if (opts.getBlock() != null) {
                        setWorkflowPositions(extendArray(pos, "options"), opts.getBlock().getInstructions(), mapLabelToPos, withAllPositions);
                    }
                    break;
                default:
                    break;
                }
            }
        }
    }
    
    public static Set<BlockPosition> getWorkflowBlockPositions(List<Instruction> insts) {
        Object[] parentPosition = {};
        Set<BlockPosition> blockPoss = new HashSet<>();
        setWorkflowBlockPositions(parentPosition, insts, blockPoss);
        return blockPoss;
    }
    
    private static void setWorkflowBlockPositions(Object[] parentPosition, List<Instruction> insts, Set<BlockPosition> blockPoss) {
        if (insts != null) {
            for (int i = 0; i < insts.size(); i++) {
                Object[] pos = extendArray(parentPosition, i);
                pos[parentPosition.length] = i;
                Instruction inst = insts.get(i);
                switch (inst.getTYPE()) {
                case FORK:
                    ForkJoin f = inst.cast();
                    if (f.getBranches() != null) {
                        for (Branch b : f.getBranches()) {
                            if (b.getWorkflow().getInstructions() != null) {
                                Object[] blockPos = extendArray(pos, "fork+" + b.getId());
                                blockPoss.add(getBlockPosition(blockPos, inst, b.getId(), b.getWorkflow().getInstructions()));
                                setWorkflowBlockPositions(blockPos, b.getWorkflow().getInstructions(), blockPoss);
                            }
                        }
                    }
                    break;
                case FORKLIST:
                    ForkList fl = inst.cast();
                    if (fl.getWorkflow() != null) {
                        if (fl.getWorkflow().getInstructions() != null) {
                            Object[] blockPos = extendArray(pos, "fork");
                            blockPoss.add(getBlockPosition(blockPos, inst, null, fl.getWorkflow().getInstructions()));
                            setWorkflowBlockPositions(blockPos, fl.getWorkflow().getInstructions(), blockPoss);
                        }
                    }
                    break;
                case CONSUME_NOTICES:
                    ConsumeNotices cn = inst.cast();
                    if (cn.getSubworkflow() != null) {
                        Object[] blockPos = extendArray(pos, "consumeNotices");
                        blockPoss.add(getBlockPosition(blockPos, inst, null, cn.getSubworkflow().getInstructions()));
                        setWorkflowBlockPositions(blockPos, cn.getSubworkflow().getInstructions(), blockPoss);
                    }
                    break;
                case IF:
                    IfElse ie = inst.cast();
                    if (ie.getThen() != null) {
                        Object[] blockPos = extendArray(pos, "then");
                        blockPoss.add(getBlockPosition(blockPos, inst, "then", ie.getThen().getInstructions()));
                        setWorkflowBlockPositions(blockPos, ie.getThen().getInstructions(), blockPoss);
                    }
                    if (ie.getElse() != null) {
                        Object[] blockPos = extendArray(pos, "else");
                        blockPoss.add(getBlockPosition(blockPos, inst, "else", ie.getElse().getInstructions()));
                        setWorkflowBlockPositions(blockPos, ie.getElse().getInstructions(), blockPoss);
                    }
                    break;
                case TRY:
                    TryCatch tc = inst.cast();
                    if (tc.getTry() != null) {
                        Object[] blockPos = extendArray(pos, "try");
                        blockPoss.add(getBlockPosition(blockPos, inst, "try", tc.getTry().getInstructions()));
                        setWorkflowBlockPositions(blockPos, tc.getTry().getInstructions(), blockPoss);
                    }
                    if (tc.getCatch() != null) {
                        Object[] blockPos = extendArray(pos, "catch");
                        blockPoss.add(getBlockPosition(blockPos, inst, "catch", tc.getCatch().getInstructions()));
                        setWorkflowBlockPositions(blockPos, tc.getCatch().getInstructions(), blockPoss);
                    }
                    break;
                case LOCK:
                    Lock l = inst.cast();
                    if (l.getLockedWorkflow() != null) {
                        Object[] blockPos = extendArray(pos, "lock");
                        blockPoss.add(getBlockPosition(blockPos, inst, null, l.getLockedWorkflow().getInstructions()));
                        setWorkflowBlockPositions(blockPos, l.getLockedWorkflow().getInstructions(), blockPoss);
                    }
                    break;
                case CYCLE:
                    Cycle c = inst.cast();
                    if (c.getCycleWorkflow() != null) {
                        Object[] blockPos = extendArray(pos, "cycle");
                        blockPoss.add(getBlockPosition(blockPos, inst, null, c.getCycleWorkflow().getInstructions()));
                        setWorkflowBlockPositions(blockPos, c.getCycleWorkflow().getInstructions(), blockPoss);
                    }
                    break;
                case STICKY_SUBAGENT:
                    StickySubagent sticky = inst.cast();
                    if (sticky.getSubworkflow() != null) {
                        Object[] blockPos = extendArray(pos, "stickySubagent");
                        blockPoss.add(getBlockPosition(blockPos, inst, null, sticky.getSubworkflow().getInstructions()));
                        setWorkflowBlockPositions(blockPos, sticky.getSubworkflow().getInstructions(), blockPoss);
                    }
                    break;
                case OPTIONS:
                    Options opts = inst.cast();
                    if (opts.getBlock() != null) {
                        Object[] blockPos = extendArray(pos, "options");
                        blockPoss.add(getBlockPosition(blockPos, inst, null, opts.getBlock().getInstructions()));
                        setWorkflowBlockPositions(blockPos, opts.getBlock().getInstructions(), blockPoss);
                    }
                    break;
                default:
                    break;
                }
            }
        }
    }
    
    private static BlockPosition getBlockPosition(Object[] pos, Instruction inst, String blockName, List<Instruction> insts) {
        blockName = blockName == null ? "" : "+" + blockName;
        BlockPosition p = new BlockPosition();
        p.setPosition(Arrays.asList(pos));
        p.setPositionString(getJBranchPathString(p.getPosition()));
        p.setLabel(inst.getLabel() == null ? null : inst.getLabel() + blockName);
        p.setType(inst.getTYPE().value());
        p.setPositions(getWorkflowAddOrderPositions(insts, pos));
        return p;
    }

    private static void setWorkflowAddOrderPositions(Object[] parentPosition, int depth, List<Instruction> insts, Set<Position> positions) {
        if (insts != null) {
            for (int i = 0; i < insts.size(); i++) {
                Object[] pos = extendArray(parentPosition, i);
                pos[parentPosition.length] = i;
                Instruction inst = insts.get(i);
                Position p = new Position();
                p.setPosition(Arrays.asList(pos));
                if (p.getPosition().size() - depth > 3) {
                    continue;
                }
                p.setPositionString(getJPositionString(p.getPosition()));
                p.setType(inst.getTYPE().value().replace("Execute.Named", "Job"));
                p.setLabel(inst.getLabel());
                positions.add(p);
                // inst.setPosition(Arrays.asList(pos));
                // inst.setPositionString(getJPositionString(inst.getPosition()));
                switch (inst.getTYPE()) {
                case FORK:
                    // ForkJoin f = inst.cast();
                    // for (Branch b : f.getBranches()) {
                    // if (b.getWorkflow().getInstructions() != null) {
                    // b.getWorkflow().getInstructions().add(createImplicitEndInstruction());
                    // } else {
                    // b.getWorkflow().setInstructions(Collections.singletonList(createImplicitEndInstruction()));
                    // }
                    // setWorkflowAddOrderPositions(extendArray(pos, "fork+" + b.getId()), b.getWorkflow().getInstructions(), positions);
                    // }
                    break;
                case FORKLIST:
                    // ForkList fl = inst.cast();
                    // if (fl.getWorkflow().getInstructions() != null) {
                    // fl.getWorkflow().getInstructions().add(createImplicitEndInstruction());
                    // } else {
                    // fl.getWorkflow().setInstructions(Collections.singletonList(createImplicitEndInstruction()));
                    // }
                    // setWorkflowAddOrderPositions(extendArray(pos, "fork"), fl.getWorkflow().getInstructions(), positions);
                    break;
                case IF:
                    IfElse ie = inst.cast();
                    if (ie.getThen() != null) {
                        setWorkflowAddOrderPositions(extendArray(pos, "then"), depth, ie.getThen().getInstructions(), positions);
                    }
                    // if (ie.getElse() != null) {
                    // setWorkflowAddOrderPositions(extendArray(pos, "else"), ie.getElse().getInstructions(), positions);
                    // }
                    break;
                case TRY:
                    TryCatch tc = inst.cast();
                    if (tc.getTry() != null) {
                        setWorkflowAddOrderPositions(extendArray(pos, "try"), depth, tc.getTry().getInstructions(), positions);
                    }
                    // if (tc.getCatch() != null) {
                    // setWorkflowAddOrderPositions(extendArray(pos, "catch"), tc.getCatch().getInstructions(), positions);
                    // }
                    break;
                case OPTIONS:
                    Options o = inst.cast();
                    if (o.getBlock() != null) {
                        setWorkflowAddOrderPositions(extendArray(pos, "options"), depth, o.getBlock().getInstructions(), positions);
                    }
                    break;
                case LOCK:
                    // Lock l = inst.cast();
                    // setWorkflowAddOrderPositions(extendArray(pos, "lock"), l.getLockedWorkflow().getInstructions(), positions);
                    break;
                case CYCLE:
                    // Cycle c = inst.cast();
                    // setWorkflowAddOrderPositions(extendArray(pos, "cycle"), c.getCycleWorkflow().getInstructions(), positions);
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
                    if (l.getLockedWorkflow() != null) {
                        updateWorkflowBoardname(oldNewBoardNames, l.getLockedWorkflow().getInstructions());
                    }
                    break;
                case CYCLE:
                    Cycle c = inst.cast();
                    if(c.getCycleWorkflow() != null) {
                        updateWorkflowBoardname(oldNewBoardNames, c.getCycleWorkflow().getInstructions());
                    }
                    break;
                case CONSUME_NOTICES:
                    ConsumeNotices cns = inst.cast();
                    String cnsNamesExpr = cns.getNoticeBoardNames();
                    List<String> cnsNames = NoticeToNoticesConverter.expectNoticeBoardsToList(cnsNamesExpr);
                    for (Map.Entry<String, String> oldNewBoardName : oldNewBoardNames.entrySet()) {
                        String oldBoardName = oldNewBoardName.getKey();
                        String newBoardName = oldNewBoardName.getValue();
                        if (newBoardName.isEmpty()) {
                            continue;
                        }
                        if (!cnsNames.isEmpty() && cnsNames.contains(oldBoardName)) {
                            cnsNamesExpr = cnsNamesExpr.replace("'" + oldBoardName + "'", "'" + newBoardName + "'").replace("\"" + oldBoardName
                                    + "\"", "\"" + newBoardName + "\"");
                        }
                    }
                    cns.setNoticeBoardNames(cnsNamesExpr);
                    if (cns.getSubworkflow() != null) {
                        updateWorkflowBoardname(oldNewBoardNames, cns.getSubworkflow().getInstructions());
                    }
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
                    if (pns.getNoticeBoardNames() != null) {
                        pns.setNoticeBoardNames(pns.getNoticeBoardNames().stream().map(pnb -> oldNewBoardNames.getOrDefault(pnb, pnb)).filter(
                                pnb -> !pnb.isEmpty()).collect(Collectors.toList()));
                    }
                    break;
                case STICKY_SUBAGENT:
                    StickySubagent ss = inst.cast();
                    if (ss.getSubworkflow() != null) {
                        updateWorkflowBoardname(oldNewBoardNames, ss.getSubworkflow().getInstructions());
                    }
                    break;
                case OPTIONS:
                    Options opts = inst.cast();
                    if (opts.getBlock() != null) {
                        updateWorkflowBoardname(oldNewBoardNames, opts.getBlock().getInstructions());
                    }
                    break;
                default:
                    break;
                }
            }
        }
    }

    public static Set<String> extractDisallowedImplicitEnds(List<Instruction> insts) {
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
                    if (ie.getThen() != null) {
                        extractImplicitEnds(ie.getThen().getInstructions(), posSet, true);
                    }
                    if (ie.getElse() != null) {
                        extractImplicitEnds(ie.getElse().getInstructions(), posSet, true);
                    }
                    break;
                case TRY:
                    TryCatch tc = inst.cast();
                    if (tc.getTry() != null) {
                        extractImplicitEnds(tc.getTry().getInstructions(), posSet, true);
                    }
                    if (tc.getCatch() != null) {
                        extractImplicitEnds(tc.getCatch().getInstructions(), posSet, true);
                    }
                    break;
                case LOCK:
                    Lock l = inst.cast();
                    if (l.getLockedWorkflow() != null) {
                        extractImplicitEnds(l.getLockedWorkflow().getInstructions(), posSet, true);
                    }
                    break;
                case CYCLE:
                    Cycle c = inst.cast();
                    if (c.getCycleWorkflow() != null) {
                        extractImplicitEnds(c.getCycleWorkflow().getInstructions(), posSet, false);
                    }
                    break;
                case CONSUME_NOTICES:
                    ConsumeNotices cn = inst.cast();
                    if (cn.getSubworkflow() != null) {
                        extractImplicitEnds(cn.getSubworkflow().getInstructions(), posSet, true);
                    }
                    break;
                case STICKY_SUBAGENT:
                    StickySubagent ss = inst.cast();
                    if (ss.getSubworkflow() != null) {
                        extractImplicitEnds(ss.getSubworkflow().getInstructions(), posSet, true);
                    }
                    break;
                case OPTIONS:
                    Options opts = inst.cast();
                    if (opts.getBlock() != null) {
                        extractImplicitEnds(opts.getBlock().getInstructions(), posSet, true);
                    }
                    break;
                default:
                    break;
                }
            }
        }
    }

    // private static void extractImplicitEndOfScope(List<Instruction> insts, Set<String> posSet, boolean extract) {
    // if (insts != null) {
    // for (int i = 0; i < insts.size(); i++) {
    // Instruction inst = insts.get(i);
    // switch (inst.getTYPE()) {
    // case IMPLICIT_END:
    // if (extract) {
    // posSet.add(getJPositionString(inst.getPosition()));
    // }
    // break;
    // case FORK:
    // ForkJoin f = inst.cast();
    // for (Branch b : f.getBranches()) {
    // extractImplicitEnds(b.getWorkflow().getInstructions(), posSet, true);
    // }
    // break;
    // case FORKLIST:
    // ForkList fl = inst.cast();
    // extractImplicitEnds(fl.getWorkflow().getInstructions(), posSet, true);
    // break;
    // case IF:
    // IfElse ie = inst.cast();
    // if (ie.getThen() != null) {
    // extractImplicitEnds(ie.getThen().getInstructions(), posSet, false);
    // }
    // if (ie.getElse() != null) {
    // extractImplicitEnds(ie.getElse().getInstructions(), posSet, false);
    // }
    // break;
    // case TRY:
    // TryCatch tc = inst.cast();
    // if (tc.getTry() != null) {
    // extractImplicitEnds(tc.getTry().getInstructions(), posSet, false);
    // }
    // if (tc.getCatch() != null) {
    // extractImplicitEnds(tc.getCatch().getInstructions(), posSet, false);
    // }
    // break;
    // case LOCK:
    // Lock l = inst.cast();
    // if (l.getLockedWorkflow() != null) {
    // extractImplicitEnds(l.getLockedWorkflow().getInstructions(), posSet, false);
    // }
    // break;
    // case CYCLE:
    // Cycle c = inst.cast();
    // if (c.getCycleWorkflow() != null) {
    // extractImplicitEnds(c.getCycleWorkflow().getInstructions(), posSet, false);
    // }
    // break;
    // default:
    // break;
    // }
    // }
    // }
    // }

    public static String getJPositionString(List<Object> positionList) {
        Either<Problem, JPosition> jPosEither = JPosition.fromList(positionList);
        if (jPosEither.isRight()) {
            return jPosEither.get().toString();
        }
        return "";
    }

    private static String getJPositionString(JPosition jPosition) {
        if (jPosition != null) {
            return jPosition.toString();
        }
        return "";
    }

    private static JPosition getJPosition(List<Object> positionList) {
        Either<Problem, JPosition> jPosEither = JPosition.fromList(positionList);
        if (jPosEither.isRight()) {
            return jPosEither.get();
        }
        return null;
    }
    
    private static String getJBranchPathString(List<Object> positionList) {
        Either<Problem, JBranchPath> jPosEither = JBranchPath.fromList(positionList);
        if (jPosEither.isRight()) {
            return jPosEither.get().toString();
        }
        return "";
    }

    // private static JPosition getJPosition(List<Object> positionList) {
    // Either<Problem, JPosition> jPosEither = JPosition.fromList(positionList);
    // if (jPosEither.isRight()) {
    // return jPosEither.get();
    // }
    // return null;
    // }

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

    public static void setStateAndSuspended(JControllerState currentstate, Workflow workflow) {
        workflow.setState(SyncStateHelper.getState(SyncStateText.UNKNOWN));
        workflow.setSuspended(false);
        workflow.setNumOfSkippedInstructions(0);
        workflow.setNumOfStoppedInstructions(0);
        if (currentstate != null) {
            JWorkflowId wId = JWorkflowId.of(JocInventory.pathToName(workflow.getPath()), workflow.getVersionId());
            SyncStateHelper.setWorkflowWithStateAndSuspended(workflow, currentstate.repo().idToCheckedWorkflow(wId), currentstate);
        }
    }

    public static boolean getSuspended(SyncState syncState) {
        SyncStateText stateText = syncState.get_text();
        return SyncStateText.SUSPENDED.equals(stateText);
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
                .map(f -> f.workflowPath().string()).collect(Collectors.toSet());

        // TODO fileOrderSources should store permantly and updated by events
//        DeployedConfigurationFilter filter = new DeployedConfigurationFilter();
//        filter.setControllerId(controllerId);
//        filter.setObjectTypes(Collections.singleton(DeployType.FILEORDERSOURCE.intValue()));
        List<DeployedContent> fileOrderSources = WorkflowRefs.getFileOrderSources(controllerId);
        
        if (fileOrderSources != null && !fileOrderSources.isEmpty()) {
            return fileOrderSources.stream().parallel().filter(dbItem -> dbItem.getContent() != null).map(dbItem -> {
                try {
                    FileOrderSource f = JocInventory.convertFileOrderSource(dbItem.getContent(), FileOrderSource.class);
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

//            return fileOrderSources.parallelStream().filter(dbItem -> dbItem.getContent() != null).map(dbItem -> {
//                try {
//                    FileOrderSource f = JocInventory.convertFileOrderSource(dbItem.getContent(), FileOrderSource.class);
//                    f.setPath(dbItem.getPath());
//                    f.setVersionDate(dbItem.getCreated());
//                    return f;
//                } catch (Exception e) {
//                    return null;
//                }
//            }).filter(Objects::nonNull).filter(f -> workflowNames.contains(f.getWorkflowName())).peek(f -> f.setState(syncFileOrderSources.contains(f
//                    .getWorkflowName()) ? SyncStateHelper.getState(SyncStateText.IN_SYNC) : SyncStateHelper.getState(SyncStateText.NOT_IN_SYNC)))
//                    .collect(Collectors.groupingByConcurrent(FileOrderSource::getWorkflowName));
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
        return controllerState.pathToFileWatch().values().stream().parallel().filter(f -> f.workflowPath().string().equals(workflowName)).map(f -> f
                .path().string()).collect(Collectors.toSet());
    }

    public static ConcurrentMap<JWorkflowId, Map<OrderStateText, Integer>> getGroupedOrdersCountPerWorkflow(JControllerState currentstate,
            WorkflowOrderCountFilter workflowsFilter, Set<Folder> permittedFolders, ZoneId zoneId) {

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
                    Instant scheduledFor = OrdersHelper.getScheduledForInstant(o, zoneId);
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
        //JOC-1681
//        Function1<Order<Order.State>, Object> finishedFilter = JOrderPredicates.or(JOrderPredicates.or(JOrderPredicates.byOrderState(
//                Order.Finished$.class), JOrderPredicates.byOrderState(Order.Cancelled$.class)), JOrderPredicates.byOrderState(
//                        Order.ProcessingKilled$.class));
//        Function1<Order<Order.State>, Object> suspendFilter = JOrderPredicates.and(o -> o.isSuspended(), JOrderPredicates.not(finishedFilter));
//        Function1<Order<Order.State>, Object> cycledOrderFilter = JOrderPredicates.and(JOrderPredicates.byOrderState(Order.Fresh$.class),
//                JOrderPredicates.and(o -> OrdersHelper.isCyclicOrderId(o.id().string()), JOrderPredicates.not(suspendFilter)));
        Function1<Order<Order.State>, Object> cycledOrderFilter = JOrderPredicates.and(JOrderPredicates.byOrderState(Order.Fresh$.class),
                o -> OrdersHelper.isCyclicOrderId(o.id().string()));
        Function1<Order<Order.State>, Object> notCycledOrderFilter = JOrderPredicates.not(cycledOrderFilter);

        Function1<Order<Order.State>, Object> blockedFilter = JOrderPredicates.and(JOrderPredicates.byOrderState(Order.Fresh$.class), o -> !o
                .isSuspended() && OrdersHelper.getScheduledForMillis(o, zoneId, surveyDateMillis) < surveyDateMillis);

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
                        blockedButWaitingForAdmissionOrderIds, zoneId), Collectors.reducing(0, e -> 1, Integer::sum))));

        workflows2.forEach(w -> groupedOrdersCount.putIfAbsent(JWorkflowId.apply(w), Collections.emptyMap()));

        return groupedOrdersCount;
    }

    private static OrderStateText groupingByState(JOrder order, Instant surveyInstant, Set<OrderId> blockedButWaitingForAdmissionOrderIds,
            ZoneId zoneId) {
        OrderStateText groupedState = OrdersHelper.getGroupedState(order.asScala().state().getClass());
        if (order.asScala().isSuspended() && !(OrderStateText.CANCELLED.equals(groupedState) || OrderStateText.FINISHED.equals(groupedState))) {
            groupedState = OrderStateText.SUSPENDED;
        }
        if (OrderStateText.SCHEDULED.equals(groupedState)) {
            Instant scheduledInstant = OrdersHelper.getScheduledForInstant(order, zoneId);
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
        // TODO JOC-1645
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
        return getSkippedLabels(currentstate, WorkflowPath.of(workflowName), compact);
    }

    public static Set<String> getSkippedLabels(JControllerState currentstate, WorkflowPath workflowName, boolean compact) {
        if (!compact && currentstate != null) {
            return getSkippedLabels(getWorkflowPathControl(currentstate, workflowName, compact), compact);
        }
        return Collections.emptySet();
    }

    public static Set<String> getSkippedLabels(Optional<WorkflowPathControl> controlState, boolean compact) {
        if (!compact) {
            if (controlState.isPresent()) {
                return JavaConverters.asJava(controlState.get().skip()).stream().map(Label::string).collect(Collectors.toSet());
            }
        }
        return Collections.emptySet();
    }

    public static Set<JPosition> getStoppedPositions(JControllerState currentstate, String workflowName, String versionId, boolean compact) {
        if (!compact && currentstate != null) {
            return getStoppedPositions(getWorkflowControl(currentstate, workflowName, versionId, compact), compact);
        }
        return Collections.emptySet();
    }

    public static Set<JPosition> getStoppedPositions(JControllerState currentstate, JWorkflowId workflowId, boolean compact) {
        if (!compact && currentstate != null) {
            return getStoppedPositions(getWorkflowControl(currentstate, workflowId, compact), compact);
        }
        return Collections.emptySet();
    }

    public static Set<JPosition> getStoppedPositions(Optional<JWorkflowControl> controlState, boolean compact) {
        if (!compact) {
            if (controlState.isPresent()) {
                return controlState.get().breakpoints();
            }
        }
        return Collections.emptySet();
    }

    public static Optional<WorkflowPathControl> getWorkflowPathControl(JControllerState currentstate, String workflowName, boolean compact) {
        return getWorkflowPathControl(currentstate, WorkflowPath.of(workflowName), compact);
    }

    public static Optional<WorkflowPathControl> getWorkflowPathControl(JControllerState currentstate, WorkflowPath workflowName, boolean compact) {
        if (!compact && currentstate != null) {
            return OptionConverters.toJava(currentstate.asScala().pathToWorkflowPathControl().get(WorkflowPathControlPath.apply(workflowName)));
        }
        return Optional.empty();
    }

    public static Optional<JWorkflowControl> getWorkflowControl(JControllerState currentstate, String workflowPath, String versionId,
            boolean compact) {
        return getWorkflowControl(currentstate, JWorkflowId.of(workflowPath, versionId), compact);
    }

    public static Optional<JWorkflowControl> getWorkflowControl(JControllerState currentstate, JWorkflowId workflowId, boolean compact) {
        if (!compact && currentstate != null) {
            JWorkflowControl c = currentstate.idToWorkflowControl().get(JWorkflowControlId.of(workflowId));
            if (c != null) {
                return Optional.of(c);
            }
        }
        return Optional.empty();
    }

    public static boolean hasBoard(String boardName, List<Instruction> insts) {
        if (boardName == null || boardName.isEmpty()) {
            return false;
        }
        if (insts != null) {
            for (int i = 0; i < insts.size(); i++) {
                Instruction inst = insts.get(i);
                switch (inst.getTYPE()) {
                case FORK:
                    ForkJoin f = inst.cast();
                    for (Branch b : f.getBranches()) {
                        if (b.getWorkflow() != null) {
                            if(hasBoard(boardName, b.getWorkflow().getInstructions())) {
                                return true;
                            }
                        }
                    }
                    break;
                case FORKLIST:
                    ForkList fl = inst.cast();
                    if (fl.getWorkflow() != null) {
                        if(hasBoard(boardName, fl.getWorkflow().getInstructions())) {
                            return true;
                        }
                    }
                    break;
                case IF:
                    IfElse ie = inst.cast();
                    if (ie.getThen() != null) {
                        if(hasBoard(boardName, ie.getThen().getInstructions())) {
                            return true;
                        }
                    }
                    if (ie.getElse() != null) {
                        if(hasBoard(boardName, ie.getElse().getInstructions())) {
                            return true;
                        }
                    }
                    break;
                case TRY:
                    TryCatch tc = inst.cast();
                    if (tc.getTry() != null) {
                        if(hasBoard(boardName, tc.getTry().getInstructions())) {
                            return true;
                        }
                    }
                    if (tc.getCatch() != null) {
                        if(hasBoard(boardName, tc.getCatch().getInstructions())) {
                            return true;
                        }
                    }
                    break;
                case LOCK:
                    Lock l = inst.cast();
                    if (l.getLockedWorkflow() != null) {
                        if(hasBoard(boardName, l.getLockedWorkflow().getInstructions())) {
                            return true;
                        }
                    }
                    break;
                case CYCLE:
                    Cycle c = inst.cast();
                    if (c.getCycleWorkflow() != null) {
                        if(hasBoard(boardName, c.getCycleWorkflow().getInstructions())) {
                            return true;
                        }
                    }
                    break;
                case CONSUME_NOTICES:
                    ConsumeNotices cns = inst.cast();
                    String cnsNamesExpr = cns.getNoticeBoardNames();
                    List<String> cnsNames = NoticeToNoticesConverter.expectNoticeBoardsToList(cnsNamesExpr);
                    if (!cnsNames.isEmpty() && cnsNames.contains(boardName)) {
                        return true;
                    }
                    if (cns.getSubworkflow() != null) {
                        if(hasBoard(boardName, cns.getSubworkflow().getInstructions())) {
                            return true;
                        }
                    }
                    break;
                case EXPECT_NOTICE:
                    ExpectNotice en = inst.cast();
                    if (boardName.equals(en.getNoticeBoardName())) {
                        return true;
                    }
                    break;
                case EXPECT_NOTICES:
                    ExpectNotices ens = inst.cast();
                    String ensNamesExpr = ens.getNoticeBoardNames();
                    List<String> ensNames = NoticeToNoticesConverter.expectNoticeBoardsToList(ensNamesExpr);
                    if (!ensNames.isEmpty() && ensNames.contains(boardName)) {
                        return true;
                    }
                    break;
                case POST_NOTICE:
                    PostNotice pn = inst.cast();
                    if (boardName.equals(pn.getNoticeBoardName())) {
                        return true;
                    }
                    break;
                case POST_NOTICES:
                    PostNotices pns = inst.cast();
                    if(pns.getNoticeBoardNames() != null) {
                        if (!pns.getNoticeBoardNames().isEmpty() && pns.getNoticeBoardNames().contains(boardName)) {
                            return true;
                        }
                    }
                    break;
                case STICKY_SUBAGENT:
                    StickySubagent ss = inst.cast();
                    if (ss.getSubworkflow() != null) {
                        if(hasBoard(boardName, ss.getSubworkflow().getInstructions())) {
                            return true;
                        }
                    }
                    break;
                case OPTIONS:
                    Options opts = inst.cast();
                    if (opts.getBlock() != null) {
                        if(hasBoard(boardName, opts.getBlock().getInstructions())) {
                            return true;
                        }
                    }
                    break;
                default:
                    break;
                }
            }
        }
        return false;
    }
    
    public static Optional<NamedJob> getFirstJob(Instruction firstInst) {
        return getFirstJob(firstInst, Collections.emptySet());
    }
    
    public static Optional<NamedJob> getFirstJob(Instruction firstInst, Set<String> skippedLabels) {
        Object[] o = {};
        return getFirstJob(o, firstInst, skippedLabels);
    }
    
    private static Optional<NamedJob> getFirstJob(Object[] parentPosition, Instruction firstInst, Set<String> skippedLabels) {
        if (firstInst != null) {
            Object[] pos = extendArray(parentPosition, 0);
            pos[parentPosition.length] = 0;
            firstInst.setPosition(Arrays.asList(pos));
            JPosition jPos = getJPosition(firstInst.getPosition());
            firstInst.setPositionString(getJPositionString(jPos));
            switch (firstInst.getTYPE()) {
            case CONSUME_NOTICES:
                ConsumeNotices cns = firstInst.cast();
                if (cns.getSubworkflow() != null && cns.getSubworkflow().getInstructions() != null && !cns.getSubworkflow().getInstructions()
                        .isEmpty()) {
                    return getFirstJob(extendArray(pos, "consumeNotices"), cns.getSubworkflow().getInstructions().get(0), skippedLabels);
                }
                break;
            case IF:
                IfElse ie = firstInst.cast();
                if (!ie.getThen().getInstructions().isEmpty()) {
                    return getFirstJob(extendArray(pos, "then"), ie.getThen().getInstructions().get(0), skippedLabels);
                }
                break;
            case TRY:
                TryCatch tc = firstInst.cast();
                if (!tc.getTry().getInstructions().isEmpty()) {
                    return getFirstJob(extendArray(pos, "try"), tc.getTry().getInstructions().get(0), skippedLabels);
                }
                break;
            case LOCK:
                Lock l = LockToLockDemandsConverter.lockToInventoryLockDemands(firstInst.cast());
                if (l.getLockedWorkflow() != null && l.getLockedWorkflow().getInstructions() != null && !l.getLockedWorkflow().getInstructions()
                        .isEmpty()) {
                    return getFirstJob(extendArray(pos, "lock"), l.getLockedWorkflow().getInstructions().get(0), skippedLabels);
                }
                break;
            case CYCLE:
                Cycle c = firstInst.cast();
                if (c.getCycleWorkflow() != null && c.getCycleWorkflow().getInstructions() != null && !c.getCycleWorkflow().getInstructions()
                        .isEmpty()) {
                    return getFirstJob(extendArray(pos, "cycle"), c.getCycleWorkflow().getInstructions().get(0), skippedLabels);
                }
                break;
            case EXECUTE_NAMED:
                NamedJob nj = firstInst.cast();
                if (skippedLabels.contains(nj.getLabel())) {
                    nj.setState(getState(InstructionStateText.SKIPPED));
                    //return Optional.of(nj);
                }
                return Optional.of(nj);
            case STICKY_SUBAGENT:
                StickySubagent sticky = firstInst.cast();
                if (sticky.getSubworkflow() != null && sticky.getSubworkflow().getInstructions() != null && !sticky.getSubworkflow().getInstructions()
                        .isEmpty()) {
                    return getFirstJob(extendArray(pos, "stickySubagent"), sticky.getSubworkflow().getInstructions().get(0), skippedLabels);
                }
                break;
            case OPTIONS:
                Options opts = firstInst.cast();
                if (opts.getBlock() != null && opts.getBlock().getInstructions() != null && !opts.getBlock().getInstructions().isEmpty()) {
                    return getFirstJob(extendArray(pos, "options"), opts.getBlock().getInstructions().get(0), skippedLabels);
                }
                break;
            default:
                break;
            }
        }
        return Optional.empty();
    }

}
