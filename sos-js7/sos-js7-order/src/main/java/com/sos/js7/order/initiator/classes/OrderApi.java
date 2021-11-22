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
import com.sos.joc.cluster.AJocClusterService;
import com.sos.joc.db.dailyplan.DBItemDailyPlanHistory;
import com.sos.joc.exceptions.BulkError;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;
import com.sos.joc.exceptions.ControllerConnectionResetException;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.model.cluster.common.ClusterServices;
import com.sos.joc.model.common.Err419;
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
        Optional<Instant> scheduledFor = Optional.empty();
        if (order.getScheduledFor() != null) {
            scheduledFor = Optional.of(Instant.ofEpochMilli(order.getScheduledFor()));
        } else {
            scheduledFor = Optional.of(Instant.now());
        }
        Map<String, Value> arguments = OrdersHelper.variablesToScalaValuedArguments(order.getArguments());
        return JFreshOrder.of(OrderId.of(order.getId()), WorkflowPath.of(order.getWorkflowPath()), scheduledFor, arguments);
    }

    public static Set<PlannedOrder> addOrdersToController(String controllerId, JocError jocError, String accessToken, Set<PlannedOrder> orders,
            List<DBItemDailyPlanHistory> items, boolean fromService) throws ControllerConnectionResetException, ControllerConnectionRefusedException,
            DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException,
            InterruptedException, ExecutionException {

        final String method = "addOrdersToController";

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
        final Map<OrderId, JFreshOrder> map = freshOrders.get(true).stream().map(Either::get).collect(Collectors.toMap(JFreshOrder::id, Function
                .identity()));

        if (freshOrders.containsKey(true) && !freshOrders.get(true).isEmpty()) {
            JControllerApi controllerApi = ControllerApi.of(controllerId);
            if (Proxies.isCoupled(controllerId)) {
                LOGGER.debug("Controller " + controllerId + " is coupled with proxy");
            } else {
                LOGGER.warn("Controller " + controllerId + " is NOT coupled with proxy");
            }

            final JControllerProxy proxy = Proxy.of(controllerId);
            proxy.api().addOrders(Flux.fromIterable(map.values())).thenAccept(either -> {
                if (fromService) {
                    AJocClusterService.setLogger(ClusterServices.dailyplan.name());
                }

                if (either.isRight()) {
                    Set<OrderId> set = map.keySet();
                    SOSHibernateSession session = null;
                    try {
                        controllerApi.deleteOrdersWhenTerminated(set).thenAccept(e -> ProblemHelper.postProblemEventIfExist(e, accessToken, jocError,
                                controllerId));

                        session = Globals.createSosHibernateStatelessConnection(method);
                        session.setAutoCommit(false);
                        Globals.beginTransaction(session);
                        OrderApi.updatePlannedOrders(session, set, controllerId);
                        OrderApi.updateHistory(session, items, true, null);
                        Globals.commit(session);
                        session.close();
                        session = null;

                        LOGGER.info(String.format("[%s][%s][submitted=%s][updated orders=%s, history=%s]", method, controllerId, set.size(), set
                                .size(), items.size()));
                    } catch (Exception e) {
                        LOGGER.error(String.format("[%s]%s", method, e.toString()), e);
                        Globals.rollback(session);
                        ProblemHelper.postExceptionEventIfExist(Either.left(e), accessToken, jocError, controllerId);
                    } finally {
                        Globals.disconnect(session);
                    }
                } else {
                    SOSHibernateSession session = null;
                    try {
                        String msg = jocError.getCode() + ":" + either.getLeft().toString();

                        session = Globals.createSosHibernateStatelessConnection(method);
                        session.setAutoCommit(false);
                        Globals.beginTransaction(session);
                        OrderApi.updateHistory(session, items, false, msg);
                        Globals.commit(session);
                        session.close();
                        session = null;

                        LOGGER.info(String.format("[%s][%s][onError][updated history=%s]%s", method, controllerId, items.size(), msg));

                        ProblemHelper.postProblemEventIfExist(either, accessToken, jocError, controllerId);
                    } catch (SOSHibernateException e) {
                        LOGGER.error(String.format("[%s]%s", method, e.toString()), e);
                        Globals.rollback(session);
                    } finally {
                        Globals.disconnect(session);
                    }
                }
            });

        }
        if (fromService) {
            AJocClusterService.setLogger(ClusterServices.dailyplan.name());
        }
        return orders;
    }

    public static void updatePlannedOrders(SOSHibernateSession session, Set<OrderId> orders, String controllerId) throws SOSHibernateException {

        FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
        filter.setControllerId(controllerId);
        filter.setSubmitted(true);
        filter.setSetOfOrders(orders);

        DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);
        dbLayer.setSubmitted(filter);
    }

    public static void updateHistory(SOSHibernateSession session, List<DBItemDailyPlanHistory> items, Boolean submitted, String message)
            throws SOSHibernateException {

        for (DBItemDailyPlanHistory item : items) {
            item.setSubmitted(submitted);
            if (message != null && !message.isEmpty()) {
                item.setMessage(message);
            }
            session.update(item);
        }
    }
}
