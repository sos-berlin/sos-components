package com.sos.joc.orders.impl;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSCheckJavaVariableName;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.JsonConverter;
import com.sos.joc.classes.order.CheckedAddOrdersPositions;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.deploy.DeployedConfigurationFilter;
import com.sos.joc.db.deploy.items.DeployedContent;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.BulkError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocNotImplementedException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Err419;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.order.AddOrder;
import com.sos.joc.model.order.AddOrders;
import com.sos.joc.model.order.OrderIds;
import com.sos.joc.orders.resource.IOrdersResourceAdd;
import com.sos.schema.JsonValidator;
import com.sos.sign.model.workflow.Workflow;

import io.vavr.control.Either;
import jakarta.ws.rs.Path;
import js7.base.problem.Problem;
import js7.data.order.OrderId;
import js7.data.workflow.WorkflowPath;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JFreshOrder;
import js7.data_for_java.workflow.JWorkflow;
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
            if (!hasManagePositionsPermission && addOrders.getOrders().parallelStream().anyMatch(requestHasStartPositionSettings.or(
                    requestHasEndPositionSettings))) {
                return accessDeniedResponse("Access denied for setting start-/endpositions");
            }
            
            // TODO JOC-1453
//            if (addOrders.getOrders().stream().map(AddOrder::getStartPosition).filter(Objects::nonNull).anyMatch(o -> o instanceof String)) {
//                throw new JocNotImplementedException("The use of labels as a start position is not yet implemented");
//            }
//            if (addOrders.getOrders().stream().map(AddOrder::getEndPositions).filter(Objects::nonNull).anyMatch(l -> l.stream().filter(
//                    Objects::nonNull).anyMatch(o -> o instanceof String))) {
//                throw new JocNotImplementedException("The use of labels as end positions is not yet implemented");
//            }
            
            
            DBItemJocAuditLog dbAuditLog = storeAuditLog(addOrders.getAuditLog(), controllerId, CategoryType.CONTROLLER);
            
            // TODO JOC-1453
            Set<String> workflowsWithLabels = Stream.concat(addOrders.getOrders().stream().filter(requestHasStartPositionSettings).filter(ao -> ao
                    .getStartPosition() instanceof String).map(AddOrder::getWorkflowPath).map(JocInventory::pathToName), addOrders.getOrders().stream().filter(
                            requestHasEndPositionSettings).filter(ao -> ao.getEndPositions().stream().filter(Objects::nonNull).anyMatch(
                                    o -> o instanceof String)).map(AddOrder::getWorkflowPath).map(JocInventory::pathToName)).collect(Collectors.toSet());
            
            Map<String, Map<String, List<Object>>> workflowsWithLabelsMap = new HashMap<>();
            if (!workflowsWithLabels.isEmpty()) {
                connection = Globals.createSosHibernateStatelessConnection(API_CALL);
                DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(connection);
                DeployedConfigurationFilter dbFilter = new DeployedConfigurationFilter();
                dbFilter.setControllerId(controllerId);
                dbFilter.setNames(workflowsWithLabels);
                dbFilter.setObjectTypes(Collections.singleton(DeployType.WORKFLOW.intValue()));
                List<DeployedContent> dbWorkflows = dbLayer.getDeployedInventory(dbFilter);
                Globals.disconnect(connection);
                connection = null;
                if (dbWorkflows != null) {
                    for (DeployedContent dbWorkflow : dbWorkflows) {
                        com.sos.inventory.model.workflow.Workflow w = JocInventory.workflowContent2Workflow(dbWorkflow.getContent());
                        if (w != null) {
                            workflowsWithLabelsMap.put(dbWorkflow.getName(), WorkflowsHelper.getLabelToPositionsMap(w));
                        }
                    }
                }
            }

            final Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
            Predicate<AddOrder> permissions = order -> canAdd(WorkflowPaths.getPath(order.getWorkflowPath()), permittedFolders);

            final JControllerProxy proxy = Proxy.of(controllerId);
            final JControllerState currentState = proxy.currentState();
            
            final ZoneId zoneId = OrdersHelper.getDailyPlanTimeZone();
            
            final String defaultOrderName = SOSCheckJavaVariableName.makeStringRuleConform(getAccount());
            List<AuditLogDetail> auditLogDetails = new ArrayList<>();

            Function<AddOrder, Either<Err419, JFreshOrder>> mapper = order -> {
                Either<Err419, JFreshOrder> either = null;
                try {
                    if (order.getOrderName() == null || order.getOrderName().isEmpty()) {
                        order.setOrderName(defaultOrderName);
                    } else {
                        SOSCheckJavaVariableName.test("orderName", order.getOrderName());
                    }
                    Either<Problem, JWorkflow> e = currentState.repo().pathToCheckedWorkflow(WorkflowPath.of(JocInventory.pathToName(order
                            .getWorkflowPath())));
                    ProblemHelper.throwProblemIfExist(e);
                    Workflow workflow = Globals.objectMapper.readValue(e.get().toJson(), Workflow.class);
                    order.setArguments(OrdersHelper.checkArguments(order.getArguments(), JsonConverter.signOrderPreparationToInvOrderPreparation(
                            workflow.getOrderPreparation())));
                    Set<String> reachablePositions = CheckedAddOrdersPositions.getReachablePositions(e.get());
                    
                    Map<String, List<Object>> labelMap = workflowsWithLabelsMap.getOrDefault(workflow.getPath(), Collections.emptyMap());
                    Optional<JPositionOrLabel> startPos = OrdersHelper.getStartPosition(order.getStartPosition(), labelMap, reachablePositions);
                    Set<JPositionOrLabel> endPoss = OrdersHelper.getEndPosition(order.getEndPositions(), labelMap, reachablePositions);
                    // TODO check if endPos not before startPos
                    
                    JFreshOrder o = OrdersHelper.mapToFreshOrder(order, zoneId, startPos, endPoss);
                    auditLogDetails.add(new AuditLogDetail(WorkflowPaths.getPath(order.getWorkflowPath()), o.id().string(), controllerId));
                    either = Either.right(o);
                } catch (Exception ex) {
                    either = Either.left(new BulkError(LOGGER).get(ex, getJocError(), order));
                }
                return either;
            };
            

            Map<Boolean, Set<Either<Err419, JFreshOrder>>> result = addOrders.getOrders().stream().filter(permissions).map(mapper).collect(
                    Collectors.groupingBy(Either::isRight, Collectors.toSet()));
            
            OrderIds entity = new OrderIds();
            if (result.containsKey(true) && !result.get(true).isEmpty()) {
                final Map<OrderId, JFreshOrder> freshOrders = result.get(true).stream().map(Either::get).collect(Collectors.toMap(JFreshOrder::id,
                        Function.identity()));
                proxy.api().addOrders(Flux.fromIterable(freshOrders.values())).thenAccept(either -> {
                    if (either.isRight()) {
//                        proxy.api().deleteOrdersWhenTerminated(freshOrders.keySet()).thenAccept(e -> ProblemHelper.postProblemEventIfExist(e,
//                                accessToken, getJocError(), addOrders.getControllerId()));
                        // auditlog is written even "deleteOrdersWhenTerminated" has a problem
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