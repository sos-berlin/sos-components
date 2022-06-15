package com.sos.joc.orders.impl;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.joc.Globals;
import com.sos.joc.classes.CheckJavaVariableName;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.JsonConverter;
import com.sos.joc.classes.order.CheckedAddOrdersPositions;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.BulkError;
import com.sos.joc.exceptions.JocException;
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
import js7.base.problem.Problem;
import js7.data.order.OrderId;
import js7.data.workflow.WorkflowPath;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JFreshOrder;
import js7.data_for_java.workflow.JWorkflow;
import js7.data_for_java.workflow.position.JPosition;
import js7.proxy.javaapi.JControllerProxy;
import reactor.core.publisher.Flux;

@Path("orders")
public class OrdersResourceAddImpl extends JOCResourceImpl implements IOrdersResourceAdd {

    private static final String API_CALL = "./orders/add";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());

    @Override
    public JOCDefaultResponse postOrdersAdd(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, AddOrders.class);
            AddOrders addOrders = Globals.objectMapper.readValue(filterBytes, AddOrders.class);
            String controllerId = addOrders.getControllerId();
            JOCDefaultResponse jocDefaultResponse = initPermissions(controllerId, getControllerPermissions(controllerId, accessToken).getOrders()
                    .getCreate());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            DBItemJocAuditLog dbAuditLog = storeAuditLog(addOrders.getAuditLog(), controllerId, CategoryType.CONTROLLER);

            final Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
            Predicate<AddOrder> permissions = order -> canAdd(order.getWorkflowPath(), permittedFolders);

            final JControllerProxy proxy = Proxy.of(controllerId);
            final JControllerState currentState = proxy.currentState();
            
            final String yyyymmdd = formatter.format(Instant.now());
            
            final String defaultOrderName = CheckJavaVariableName.makeStringRuleConform(getAccount());
            List<AuditLogDetail> auditLogDetails = new ArrayList<>();

            Function<AddOrder, Either<Err419, JFreshOrder>> mapper = order -> {
                Either<Err419, JFreshOrder> either = null;
                try {
                    if (order.getOrderName() == null || order.getOrderName().isEmpty()) {
                        order.setOrderName(defaultOrderName);
                    } else {
                        CheckJavaVariableName.test("orderName", order.getOrderName());
                    }
                    Either<Problem, JWorkflow> e = currentState.repo().pathToCheckedWorkflow(WorkflowPath.of(JocInventory.pathToName(order.getWorkflowPath())));
                    ProblemHelper.throwProblemIfExist(e);
                    Workflow workflow = Globals.objectMapper.readValue(e.get().toJson(), Workflow.class);
                    order.setArguments(OrdersHelper.checkArguments(order.getArguments(), JsonConverter.signOrderPreparationToInvOrderPreparation(
                            workflow.getOrderPreparation())));
                    Set<String> reachablePositions = CheckedAddOrdersPositions.getReachablePositions(e.get());
                    Optional<JPosition> startPos = OrdersHelper.getStartPosition(order.getStartPosition(), order.getStartPositionString(), reachablePositions);
                    Optional<JPosition> endPos = OrdersHelper.getEndPosition(order.getEndPosition(), order.getEndPositionString(), reachablePositions);
                    // TODO check if endPos not before startPos
                    
                    JFreshOrder o = OrdersHelper.mapToFreshOrder(order, yyyymmdd, startPos, endPos);
                    auditLogDetails.add(new AuditLogDetail(order.getWorkflowPath(), o.id().string(), controllerId));
                    either = Either.right(o);
                } catch (Exception ex) {
                    either = Either.left(new BulkError().get(ex, getJocError(), order));
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
        }
    }

}