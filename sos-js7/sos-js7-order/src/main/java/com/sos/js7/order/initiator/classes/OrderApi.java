package com.sos.js7.order.initiator.classes;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.controller.model.order.FreshOrder;
import com.sos.joc.Globals;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.db.orders.DBItemDailyPlanHistory;
import com.sos.joc.exceptions.BulkError;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;
import com.sos.joc.exceptions.ControllerConnectionResetException;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.model.common.Err419;
import com.sos.js7.order.initiator.db.DBLayerDailyPlanHistory;
import com.sos.js7.order.initiator.db.DBLayerDailyPlannedOrders;
import com.sos.js7.order.initiator.db.FilterDailyPlannedOrders;

import io.vavr.control.Either;
import js7.data.order.OrderId;
import js7.data.value.Value;
import js7.data.workflow.WorkflowPath;
import js7.data_for_java.order.JFreshOrder;
import js7.proxy.javaapi.JControllerApi;
import js7.proxy.javaapi.JControllerProxy;
import reactor.core.publisher.Flux;

public class OrderApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderApi.class);

    private static JFreshOrder mapToFreshOrder(FreshOrder order) {
        OrderId orderId = OrderId.of(order.getId());

        Map<String, Value> arguments = OrdersHelper.variablesToScalaValuedArguments(order.getArguments());

        Optional<Instant> scheduledFor = Optional.empty();
        if (order.getScheduledFor() != null) {
            scheduledFor = Optional.of(Instant.ofEpochMilli(order.getScheduledFor()));
        } else {
            scheduledFor = Optional.of(Instant.now());
        }
        return JFreshOrder.of(orderId, WorkflowPath.of(order.getWorkflowPath()), scheduledFor, arguments);
    }

    public static Set<PlannedOrder> addOrderToController(String controllerId, JocError jocError, String accessToken, Set<PlannedOrder> orders,
            List<DBItemDailyPlanHistory> listOfInsertHistoryEntries) throws ControllerConnectionResetException, ControllerConnectionRefusedException,
            DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException,
            InterruptedException, ExecutionException {

        Function<PlannedOrder, Either<Err419, JFreshOrder>> mapper = order -> {
            Either<Err419, JFreshOrder> either = null;
            try {
                either = Either.right(mapToFreshOrder(order.getFreshOrder()));
            } catch (Exception ex) {
                either = Either.left(new BulkError().get(ex, jocError, order.getFreshOrder().getId()));
            }
            return either;
        };

        LOGGER.debug("update submit state for history and order on controller::" + controllerId);

        Map<Boolean, Set<Either<Err419, JFreshOrder>>> freshOrders = orders.stream().map(mapper).collect(Collectors.groupingBy(Either::isRight,
                Collectors.toSet()));
        final Map<OrderId, JFreshOrder> freshOrderMappedIds = freshOrders.get(true).stream().map(Either::get).collect(Collectors.toMap(
                JFreshOrder::id, Function.identity()));

        if (freshOrders.containsKey(true) && !freshOrders.get(true).isEmpty()) {

            JControllerApi controllerApi = ControllerApi.of(controllerId);
            if (Proxies.isCoupled(controllerId)) {
                LOGGER.debug("Controller " + controllerId + " is coupled with proxy");
            } else {
                LOGGER.warn("Controller " + controllerId + " is NOT coupled with proxy");
            }
            final JControllerProxy proxy = Proxy.of(controllerId);
            proxy.api().addOrders(Flux.fromIterable(freshOrderMappedIds.values())).thenAccept(either -> {
                if (either.isRight()) {

                    SOSHibernateSession sosHibernateSession = null;
                    try {
                        sosHibernateSession = Globals.createSosHibernateStatelessConnection("addOrderToController");
                        sosHibernateSession.setAutoCommit(false);
                        Globals.beginTransaction(sosHibernateSession);

                        OrderApi.updatePlannedOrders(sosHibernateSession, freshOrderMappedIds.keySet(), controllerId);
                        OrderApi.updateHistory(sosHibernateSession, listOfInsertHistoryEntries, true, null);
                        controllerApi.deleteOrdersWhenTerminated(freshOrderMappedIds.keySet()).thenAccept(e -> ProblemHelper.postProblemEventIfExist(
                                e, accessToken, jocError, controllerId));
                        Globals.commit(sosHibernateSession);

                    } catch (Exception e) {
                        Globals.rollback(sosHibernateSession);
                        ProblemHelper.postExceptionEventIfExist(Either.left(e), accessToken, jocError, controllerId);
                    } finally {
                        Globals.disconnect(sosHibernateSession);
                    }
                } else {
                    SOSHibernateSession sosHibernateSession = null;
                    try {
                        sosHibernateSession = Globals.createSosHibernateStatelessConnection("addOrderToController");
                        sosHibernateSession.setAutoCommit(false);
                        Globals.beginTransaction(sosHibernateSession);
                        OrderApi.updateHistory(sosHibernateSession, listOfInsertHistoryEntries, false, jocError.getCode() + ":" + either.getLeft()
                                .toString());
                        Globals.commit(sosHibernateSession);
                        ProblemHelper.postProblemEventIfExist(either, accessToken, jocError, controllerId);
                    } catch (SOSHibernateException e) {
                        Globals.rollback(sosHibernateSession);
                    } finally {
                        Globals.disconnect(sosHibernateSession);
                    }
                }
            });

        }

        return orders;
    }

    public static void updatePlannedOrders(SOSHibernateSession sosHibernateSession, Set<OrderId> addedOrders, String controllerId)
            throws SOSHibernateException {

        DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders = new DBLayerDailyPlannedOrders(sosHibernateSession);

        FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
        filter.setControllerId(controllerId);
        filter.setSubmitted(true);
        filter.setSetOfOrders(addedOrders);
        dbLayerDailyPlannedOrders.setSubmitted(filter);

    }

    public static void updateHistory(SOSHibernateSession sosHibernateSession, List<DBItemDailyPlanHistory> listOfInsertHistoryEntries,
            Boolean submitted, String message) throws SOSHibernateException {

        DBLayerDailyPlanHistory dbLayerDailyPlanHistory = new DBLayerDailyPlanHistory(sosHibernateSession);
        for (DBItemDailyPlanHistory dbItemDailyPlanHistory : listOfInsertHistoryEntries) {
            dbItemDailyPlanHistory.setSubmitted(submitted);
            if (message != null && !message.isEmpty()) {
                dbItemDailyPlanHistory.setMessage(message);
            }
            dbLayerDailyPlanHistory.updateDailyPlanHistory(dbItemDailyPlanHistory);
        }

    }
}
