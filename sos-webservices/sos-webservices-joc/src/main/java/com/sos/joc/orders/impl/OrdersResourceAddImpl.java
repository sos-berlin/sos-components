package com.sos.joc.orders.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
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
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.OrdersHelper;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.audit.AddOrderAudit;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.exceptions.BulkError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
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
import js7.data.value.BooleanValue;
import js7.data.value.NumberValue;
import js7.data.value.StringValue;
import js7.data.value.Value;
import js7.data.workflow.WorkflowPath;
import js7.proxy.javaapi.JControllerProxy;
import js7.proxy.javaapi.data.controller.JControllerState;
import js7.proxy.javaapi.data.order.JFreshOrder;
import js7.proxy.javaapi.data.workflow.JWorkflow;
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

            JOCDefaultResponse jocDefaultResponse = initPermissions(addOrders.getControllerId(), getPermissonsJocCockpit(addOrders
                    .getControllerId(), accessToken).getOrder().getExecute().isStart());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            checkRequiredComment(addOrders.getAuditLog());
            if (addOrders.getOrders().size() == 0) {
                throw new JocMissingRequiredParameterException("undefined 'orders'");
            }

            final Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
            Predicate<AddOrder> permissions = order -> canAdd(order.getWorkflowPath(), permittedFolders);

            final JControllerProxy proxy = Proxy.of(addOrders.getControllerId());
            final JControllerState currentState = proxy.currentState();
            
            final String yyyymmdd = formatter.format(Instant.now());

            Function<AddOrder, Either<Err419, JFreshOrder>> mapper = order -> {
                Either<Err419, JFreshOrder> either = null;
                try {
                    CheckJavaVariableName.test("orderName", order.getOrderName());
                    Either<Problem, JWorkflow> e = currentState.pathToWorkflow(WorkflowPath.of(JocInventory.pathToName(order.getWorkflowPath())));
                    ProblemHelper.throwProblemIfExist(e);
                    Workflow workflow = Globals.objectMapper.readValue(e.get().toJson(), Workflow.class);
                    order.setArguments(OrdersHelper.checkArguments(order.getArguments(), workflow.getOrderRequirements()));
                    JFreshOrder o = mapToFreshOrder(order, yyyymmdd);
                    AddOrderAudit orderAudit = new AddOrderAudit(order, addOrders, o.id().string());
                    logAuditMessage(orderAudit);
                    either = Either.right(o);
                    storeAuditLogEntry(orderAudit);
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
                        proxy.api().removeOrdersWhenTerminated(freshOrders.keySet()).thenAccept(e -> ProblemHelper.postProblemEventIfExist(e,
                                accessToken, getJocError(), addOrders.getControllerId()));
                    } else {
                        ProblemHelper.postProblemEventIfExist(either, accessToken, getJocError(), addOrders.getControllerId());
                    }
                });

                entity.setOrderIds(freshOrders.keySet().stream().map(o -> o.string()).collect(Collectors.toList()));
            }
            entity.setDeliveryDate(Date.from(Instant.now()));
            
//            if (result.containsKey(true) && !result.get(true).isEmpty()) {
//                OrderApi.addOrders(addOrders, this.getAccount());
//            }
            
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
    
    private static JFreshOrder mapToFreshOrder(AddOrder order, String yyyymmdd) {
        //TODO uniqueId comes from dailyplan, here a fake
        String uniqueId = Long.valueOf(Instant.now().toEpochMilli()).toString().substring(4);
        OrderId orderId = OrderId.of(String.format("#%s#T%s-%s", yyyymmdd, uniqueId, order.getOrderName()));
        Optional<Instant> scheduledFor = JobSchedulerDate.getScheduledForInUTC(order.getScheduledFor(), order.getTimeZone());
        Map<String, Value> arguments = new HashMap<>();
        if (order.getArguments() != null) {
            Map<String, Object> a = order.getArguments().getAdditionalProperties();
            for(String key : a.keySet()) {
                Object val = a.get(key);
                if (val instanceof String) {
                    arguments.put(key, StringValue.of((String) val));
                } else if (val instanceof Boolean) {
                    arguments.put(key, BooleanValue.of((Boolean) val));
                } else if (val instanceof Integer) {
                    arguments.put(key, NumberValue.of((Integer) val));
                } else if (val instanceof Long) {
                    arguments.put(key, NumberValue.of((Long) val));
                } else if (val instanceof Double) {
                    arguments.put(key, NumberValue.of(scala.math.BigDecimal.javaBigDecimal2bigDecimal(BigDecimal.valueOf((Double) val))));
                } else if (val instanceof BigDecimal) {
                    arguments.put(key, NumberValue.of(scala.math.BigDecimal.javaBigDecimal2bigDecimal((BigDecimal) val)));
                }
            }
        }
        return JFreshOrder.of(orderId, WorkflowPath.of(JocInventory.pathToName(order.getWorkflowPath())), scheduledFor, arguments);
    }

}