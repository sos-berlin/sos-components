package com.sos.joc.orders.impl;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSCheckJavaVariableName;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.inventory.model.workflow.Requirements;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.order.CheckedAddOrdersPositions;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.settings.ClusterSettings;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.deploy.DeployedConfigurationFilter;
import com.sos.joc.db.deploy.items.DeployedContent;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.BulkError;
import com.sos.joc.exceptions.JocAccessDeniedException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Err419;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.order.AddOrder;
import com.sos.joc.model.order.AddOrders;
import com.sos.joc.model.order.BlockPosition;
import com.sos.joc.model.order.OrderIds;
import com.sos.joc.model.order.OrderV;
import com.sos.joc.orders.resource.IOrdersResourceAdd;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import jakarta.ws.rs.Path;
import js7.base.problem.Problem;
import js7.data.order.OrderId;
import js7.data.workflow.WorkflowPath;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JFreshOrder;
import js7.data_for_java.workflow.JWorkflow;
import js7.data_for_java.workflow.position.JBranchPath;
import js7.data_for_java.workflow.position.JPositionOrLabel;
import js7.proxy.javaapi.JControllerProxy;
import reactor.core.publisher.Flux;

@Path("orders")
public class OrdersResourceAddImpl extends JOCResourceImpl implements IOrdersResourceAdd {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrdersResourceAddImpl.class);
    private static final String API_CALL = "./orders/add";

    @Override
    public JOCDefaultResponse postOrdersAdd(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validate(filterBytes, AddOrders.class);
            AddOrders addOrders = Globals.objectMapper.readValue(filterBytes, AddOrders.class);
            String controllerId = addOrders.getControllerId();
            JOCDefaultResponse jocDefaultResponse = initPermissions(controllerId, getControllerPermissions(controllerId, accessToken).getOrders()
                    .getCreate());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            boolean hasManagePositionsPermission = getControllerPermissions(controllerId, accessToken).getOrders().getManagePositions();
            Predicate<AddOrder> requestHasStartPositionSettings = o -> o.getStartPosition() != null;
            Predicate<AddOrder> requestHasEndPositionSettings = o -> o.getEndPositions() != null && !o.getEndPositions().isEmpty();
            Predicate<AddOrder> requestHasBlockPositionSettings = o -> o.getBlockPosition() != null;
            if (!hasManagePositionsPermission && addOrders.getOrders().parallelStream().anyMatch(requestHasStartPositionSettings.or(
                    requestHasEndPositionSettings).or(requestHasBlockPositionSettings))) {
                return accessDeniedResponse("Access denied for setting start-/end-/blockpositions");
            }
            
            DBItemJocAuditLog dbAuditLog = storeAuditLog(addOrders.getAuditLog(), controllerId, CategoryType.CONTROLLER);
            
            Set<String> workflows = addOrders.getOrders().stream().map(AddOrder::getWorkflowPath).map(JocInventory::pathToName).collect(Collectors.toSet());
            
            // TODO JOC-1453
//            Set<String> workflowsWithLabels = Stream.of(
//                    addOrders.getOrders().stream().filter(requestHasStartPositionSettings).filter(ao -> ao.getStartPosition() instanceof String)
//                        .map(AddOrder::getWorkflowPath).map(JocInventory::pathToName), 
//                    addOrders.getOrders().stream().filter(requestHasEndPositionSettings).filter(ao -> ao.getEndPositions().stream().filter(Objects::nonNull)
//                        .anyMatch(o -> o instanceof String)).map(AddOrder::getWorkflowPath).map(JocInventory::pathToName), 
//                    addOrders.getOrders().stream().filter(requestHasBlockPositionSettings)
//                        .map(AddOrder::getWorkflowPath).map(JocInventory::pathToName)).flatMap(s -> s).collect(Collectors.toSet());

            Map<String, Map<String, List<Object>>> workflowsWithLabelsMap = new HashMap<>();
            Map<String, Set<BlockPosition>> workflowsWithBlockPositions = new HashMap<>();
            Map<String, Requirements> workflowOrderPreparations = new HashMap<>();
            if (!workflows.isEmpty()) {
                connection = Globals.createSosHibernateStatelessConnection(API_CALL);
                DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(connection);
                DeployedConfigurationFilter dbFilter = new DeployedConfigurationFilter();
                dbFilter.setControllerId(controllerId);
                dbFilter.setNames(workflows);
                dbFilter.setObjectTypes(Collections.singleton(DeployType.WORKFLOW.intValue()));
                List<DeployedContent> dbWorkflows = dbLayer.getDeployedInventory(dbFilter);
                Globals.disconnect(connection);
                connection = null;
                if (dbWorkflows != null) {
                    for (DeployedContent dbWorkflow : dbWorkflows) {
                        com.sos.inventory.model.workflow.Workflow w = JocInventory.workflowContent2Workflow(dbWorkflow.getContent());
                        if (w != null) {
                            workflowsWithLabelsMap.put(dbWorkflow.getName(), WorkflowsHelper.getLabelToPositionsMap(w));
                            workflowsWithBlockPositions.put(dbWorkflow.getName(), WorkflowsHelper.getWorkflowBlockPositions(w.getInstructions()));
                            workflowOrderPreparations.put(dbWorkflow.getName(), w.getOrderPreparation());
                        }
                    }
                }
            }

            final Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
//            final Predicate<AddOrder> permissions = order -> canAdd(WorkflowPaths.getPath(JocInventory.pathToName(order.getWorkflowPath())),
//                    permittedFolders);

            final JControllerProxy proxy = Proxy.of(controllerId);
            final JControllerState currentState = proxy.currentState();
            
            final ZoneId zoneId = OrdersHelper.getDailyPlanTimeZone();
            
            final String defaultOrderName = SOSCheckJavaVariableName.makeStringRuleConform(getAccount());
            final boolean allowEmptyArguments = ClusterSettings.getAllowEmptyArguments(Globals.getConfigurationGlobalsJoc());
            List<AuditLogDetail> auditLogDetails = new ArrayList<>();
            Consumer<AddOrder> workflowNameToPath = o -> o.setWorkflowPath(WorkflowPaths.getPath(JocInventory.pathToName(o.getWorkflowPath())));
            Map<OrderV, Set<String>> orderTags = new HashMap<>();

            Function<AddOrder, Either<Err419, JFreshOrder>> mapper = order -> {
                Either<Err419, JFreshOrder> either = null;
                try {
                    if (order.getOrderName() == null || order.getOrderName().isEmpty()) {
                        order.setOrderName(defaultOrderName);
                    } else {
                        SOSCheckJavaVariableName.test("orderName", order.getOrderName());
                    }
                    String workflowName = JocInventory.pathToName(order.getWorkflowPath());
                    Either<Problem, JWorkflow> e = currentState.repo().pathToCheckedWorkflow(WorkflowPath.of(workflowName));
                    ProblemHelper.throwProblemIfExist(e);
                    if (!canAdd(order.getWorkflowPath(), permittedFolders)) {
                        throw new JocAccessDeniedException("Missing folder permissions for workflow: " + order.getWorkflowPath());
                    }
//                    Workflow workflow = Globals.objectMapper.readValue(e.get().toJson(), Workflow.class);
//                    order.setArguments(OrdersHelper.checkArguments(order.getArguments(), JsonConverter.signOrderPreparationToInvOrderPreparation(
//                            workflow.getOrderPreparation()), allowEmptyArguments));
                    
                    order.setArguments(OrdersHelper.checkArguments(order.getArguments(), workflowOrderPreparations.get(workflowName),
                            allowEmptyArguments));

                    Map<String, List<Object>> labelMap = workflowsWithLabelsMap.getOrDefault(workflowName, Collections.emptyMap());
                    boolean forceJobAdmission = order.getForceJobAdmission() == Boolean.TRUE;
                    Optional<JPositionOrLabel> startPos = Optional.empty();
                    Set<JPositionOrLabel> endPoss = Collections.emptySet();
                    JBranchPath jBrachPath = null;
                    
                    if (requestHasBlockPositionSettings.test(order)) {

                        Set<BlockPosition> availableBlockPositions = workflowsWithBlockPositions.getOrDefault(workflowName, Collections.emptySet());

                        BlockPosition blockPosition = OrdersHelper.getBlockPosition(order.getBlockPosition(), workflowName, availableBlockPositions);

                        //check start-/endpositions inside block
                        startPos = OrdersHelper.getStartPositionInBlock(order.getStartPosition(), labelMap, blockPosition);
                        endPoss = OrdersHelper.getEndPositionInBlock(order.getEndPositions(), labelMap, blockPosition);
                        
                        jBrachPath = OrdersHelper.getJBranchPath(blockPosition);
                        
                    } else {
                        Set<String> reachablePositions = CheckedAddOrdersPositions.getReachablePositions(e.get());

                        startPos = OrdersHelper.getStartPosition(order.getStartPosition(), labelMap, reachablePositions);
                        endPoss = OrdersHelper.getEndPosition(order.getEndPositions(), labelMap, reachablePositions);
                    }
                    
                    // TODO check if endPos not before startPos
                    Optional<Instant> scheduledFor = JobSchedulerDate.getScheduledForInUTC(order.getScheduledFor(), order.getTimeZone());
                    JFreshOrder o = OrdersHelper.mapToFreshOrder(order, scheduledFor, zoneId, startPos, endPoss, jBrachPath, forceJobAdmission);
                    auditLogDetails.add(new AuditLogDetail(WorkflowPaths.getPath(workflowName), o.id().string(), controllerId));
                    
                    if (order.getTags() != null && !order.getTags().isEmpty()) {
                        order.getTags().forEach(tag -> SOSCheckJavaVariableName.test("tags", tag));
                        OrderV orderV = new OrderV();
                        orderV.setOrderId(o.id().string());
                        orderV.setScheduledFor(scheduledFor.orElse(Instant.now()).toEpochMilli());
                        orderTags.put(orderV, order.getTags());
                    }
                    either = Either.right(o);
                } catch (Exception ex) {
                    either = Either.left(new BulkError(LOGGER).get(ex, getJocError(), order));
                }
                return either;
            };
            

            Map<Boolean, Set<Either<Err419, JFreshOrder>>> result = addOrders.getOrders().stream().peek(workflowNameToPath).map(mapper).collect(
                    Collectors.groupingBy(Either::isRight, Collectors.toSet()));

            OrderIds entity = new OrderIds();
            if (result.containsKey(true) && !result.get(true).isEmpty()) {
                final Map<OrderId, JFreshOrder> freshOrders = result.get(true).stream().map(Either::get).collect(Collectors.toMap(JFreshOrder::id,
                        Function.identity()));
                proxy.api().addOrders(Flux.fromIterable(freshOrders.values())).thenAccept(either -> {
                    if (either.isRight()) {
                        if (!orderTags.isEmpty()) {
                            OrdersHelper.storeTags(controllerId, orderTags).thenAccept(either2 -> ProblemHelper.postExceptionEventIfExist(either2,
                                    accessToken, getJocError(), addOrders.getControllerId()));
                        }
                        OrdersHelper.storeAuditLogDetails(auditLogDetails, dbAuditLog.getId()).thenAccept(either2 -> ProblemHelper
                                .postExceptionEventIfExist(either2, accessToken, getJocError(), addOrders.getControllerId()));
                    } else {
                        ProblemHelper.postProblemEventIfExist(either, accessToken, getJocError(), addOrders.getControllerId());
                    }
                });

                entity.setOrderIds(freshOrders.keySet().stream().map(o -> o.string()).collect(Collectors.toSet()));
            }
            entity.setDeliveryDate(Date.from(Instant.now()));
            
            if (result.containsKey(false) && !result.get(false).isEmpty()) {
                return JOCDefaultResponse.responseStatus419(result.get(false).stream().map(Either::getLeft).collect(Collectors.toList()));
            }
            return JOCDefaultResponse.responseStatus200(entity);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
    }

}