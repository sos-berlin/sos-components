package com.sos.js7.order.initiator.classes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.controller.model.order.FreshOrder;
import com.sos.joc.Globals;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.db.orders.DBItemDailyPlanHistory;
import com.sos.joc.exceptions.BulkError;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.model.common.Err419;
import com.sos.js7.order.initiator.db.DBLayerDailyPlanHistory;
import com.sos.js7.order.initiator.db.DBLayerDailyPlannedOrders;
import com.sos.js7.order.initiator.db.FilterDailyPlannedOrders;

import io.vavr.control.Either;
import js7.data.order.OrderId;
import js7.data.value.BooleanValue;
import js7.data.value.NumberValue;
import js7.data.value.StringValue;
import js7.data.value.Value;
import js7.data.workflow.WorkflowPath;
import js7.data_for_java.order.JFreshOrder;
import js7.proxy.javaapi.JControllerProxy;
import reactor.core.publisher.Flux;

public class OrderApi {

    private static JFreshOrder mapToFreshOrder(FreshOrder order) {
        OrderId orderId = OrderId.of(order.getId());
        Map<String, Value> arguments = new HashMap<>();
        if (order.getArguments() != null) {
            Map<String, Object> a = order.getArguments().getAdditionalProperties();
            for (String key : a.keySet()) {
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
                    arguments.put(key, NumberValue.of(BigDecimal.valueOf((Double) val)));
                } else if (val instanceof BigDecimal) {
                    arguments.put(key, NumberValue.of((BigDecimal) val));
                }
            }
        }
        Optional<Instant> scheduledFor = Optional.empty();
        if (order.getScheduledFor() != null) {
            scheduledFor = Optional.of(Instant.ofEpochMilli(order.getScheduledFor()));
        }
        return JFreshOrder.of(orderId, WorkflowPath.of(order.getWorkflowPath()), scheduledFor, arguments);
    }

    public static Set<PlannedOrder> addOrderToController(JocError jocError, String accessToken, Set<PlannedOrder> orders,
            List<DBItemDailyPlanHistory> listOfInsertHistoryEntries) throws JobSchedulerConnectionResetException,
            JobSchedulerConnectionRefusedException, DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException,
            DBConnectionRefusedException, InterruptedException, ExecutionException {

        Function<PlannedOrder, Either<Err419, JFreshOrder>> mapper = order -> {
            Either<Err419, JFreshOrder> either = null;
            try {
                either = Either.right(mapToFreshOrder(order.getFreshOrder()));
            } catch (Exception ex) {
                either = Either.left(new BulkError().get(ex, jocError, order.getFreshOrder().getId()));
            }
            return either;
        };

        String controllerId = OrderInitiatorGlobals.orderInitiatorSettings.getControllerId();
        Map<Boolean, Set<Either<Err419, JFreshOrder>>> freshOrders = orders.stream().map(mapper).collect(Collectors.groupingBy(Either::isRight,
                Collectors.toSet()));
        final Map<OrderId, JFreshOrder> freshOrderMappedIds = freshOrders.get(true).stream().map(Either::get).collect(Collectors.toMap(
                JFreshOrder::id, Function.identity()));

        if (freshOrders.containsKey(true) && !freshOrders.get(true).isEmpty()) {

            JControllerProxy jControllerProxy = Proxy.of(controllerId);

            jControllerProxy.addOrders(Flux.fromIterable(freshOrderMappedIds.values())).thenAccept(either -> {
                if (either.isRight()) {
                    SOSHibernateSession sosHibernateSession = null;

                    try {
                        sosHibernateSession = Globals.createSosHibernateStatelessConnection("submitOrdersToController");
                        sosHibernateSession.setAutoCommit(false);
                        Globals.beginTransaction(sosHibernateSession);

                        OrderApi.updatePlannedOrders(sosHibernateSession, jocError, accessToken, orders);
                        OrderApi.updateHistory(sosHibernateSession, jocError, accessToken, listOfInsertHistoryEntries);
                        jControllerProxy.api().removeOrdersWhenTerminated(freshOrderMappedIds.keySet()).thenAccept(e -> ProblemHelper
                                .postProblemEventIfExist(e, accessToken, jocError, controllerId));
                        Globals.commit(sosHibernateSession);

                    } catch (Exception e) {
                        ProblemHelper.postExceptionEventIfExist(Either.left(e), accessToken, jocError, controllerId);
                    } finally {
                        Globals.disconnect(sosHibernateSession);
                    }
                } else {
                    ProblemHelper.postProblemEventIfExist(either, accessToken, jocError, controllerId);
                }
            });
        }
        return orders;
    }

    public static void updatePlannedOrders(SOSHibernateSession sosHibernateSession, JocError jocError, String accessToken,
            Set<PlannedOrder> addedOrders) throws SOSHibernateException {

        DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders = new DBLayerDailyPlannedOrders(sosHibernateSession);

        FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
        filter.setControllerId(OrderInitiatorGlobals.orderInitiatorSettings.getControllerId());
        filter.setSetOfPlannedOrder(addedOrders);
        filter.setSubmitted(true);
        dbLayerDailyPlannedOrders.setSubmitted(filter);
    }

    public static void updateHistory(SOSHibernateSession sosHibernateSession, JocError jocError, String accessToken,
            List<DBItemDailyPlanHistory> listOfInsertHistoryEntries) throws SOSHibernateException {

        DBLayerDailyPlanHistory dbLayerDailyPlanHistory = new DBLayerDailyPlanHistory(sosHibernateSession);
        for (DBItemDailyPlanHistory dbItemDailyPlanHistory : listOfInsertHistoryEntries) {
            dbItemDailyPlanHistory.setSubmitted(true);
            dbLayerDailyPlanHistory.updateDailyPlanHistory(dbItemDailyPlanHistory);
        }

    }
}
