package com.sos.js7.order.initiator.common;

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
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
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

    public static Set<PlannedOrder> addOrdersToController(String controllerId, String submissionForDate, boolean fromService,
            Set<PlannedOrder> orders, List<DBItemDailyPlanHistory> items, JocError jocError, String accessToken)
            throws ControllerConnectionResetException, ControllerConnectionRefusedException, DBMissingDataException, JocConfigurationException,
            DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException, InterruptedException, ExecutionException {

        final String method = "addOrdersToController";
        String logSubmissionForDate = SOSString.isEmpty(submissionForDate) ? "" : "[" + submissionForDate + "]";

        Function<PlannedOrder, Either<Err419, JFreshOrder>> mapper = order -> {
            Either<Err419, JFreshOrder> either = null;
            try {
                either = Either.right(mapToFreshOrder(order.getFreshOrder()));
            } catch (Exception ex) {
                either = Either.left(new BulkError().get(ex, jocError, order.getFreshOrder().getId()));
            }
            return either;
        };

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[%s][%s]%s update submit state for history and order", method, controllerId, logSubmissionForDate));
        }
        Map<Boolean, Set<Either<Err419, JFreshOrder>>> freshOrders = orders.stream().map(mapper).collect(Collectors.groupingBy(Either::isRight,
                Collectors.toSet()));
        final Map<OrderId, JFreshOrder> map = freshOrders.get(true).stream().map(Either::get).collect(Collectors.toMap(JFreshOrder::id, Function
                .identity()));

        if (freshOrders.containsKey(true) && !freshOrders.get(true).isEmpty()) {
            JControllerApi controllerApi = ControllerApi.of(controllerId);
            final Set<OrderId> set = map.keySet();
            String add = Proxies.isCoupled(controllerId) ? "" : "not ";
            LOGGER.info(String.format("[%s][%s]%s[%scoupled with proxy]start submitting %s orders ...", method, controllerId, logSubmissionForDate,
                    add, set.size()));

            final JControllerProxy proxy = Proxy.of(controllerId);
            proxy.api().addOrders(Flux.fromIterable(map.values())).thenAccept(either -> {
                if (fromService) {
                    AJocClusterService.setLogger(ClusterServices.dailyplan.name());
                }
                if (either.isRight()) {
                    // AJocClusterService.setLogger(ClusterServices.dailyplan.name());
                    // Set<OrderId> set = map.keySet();
                    SOSHibernateSession session = null;
                    try {
                        controllerApi.deleteOrdersWhenTerminated(set).thenAccept(e -> ProblemHelper.postProblemEventIfExist(e, accessToken, jocError,
                                controllerId));

                        session = Globals.createSosHibernateStatelessConnection(method);
                        DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);

                        Instant start = Instant.now();

                        session.setAutoCommit(false);
                        Globals.beginTransaction(session);
                        int updateOrders = OrderApi.updatePlannedOrders(dbLayer, set, controllerId);
                        Instant updateOrdersEnd = Instant.now();
                        int updateHistory = OrderApi.updateHistory(dbLayer, items, true, null);
                        Globals.commit(session);
                        session.close();
                        session = null;

                        Instant end = Instant.now();
                        LOGGER.info(String.format("[%s][%s]%s[submitted=%s][updated orders=%s(%s), history=%s(%s)]", method, controllerId,
                                logSubmissionForDate, set.size(), updateOrders, SOSDate.getDuration(start, updateOrdersEnd), updateHistory, SOSDate
                                        .getDuration(updateOrdersEnd, end)));
                    } catch (Exception e) {
                        LOGGER.error(String.format("[%s][%s]%s %s", method, controllerId, logSubmissionForDate, e.toString()), e);
                        Globals.rollback(session);
                        ProblemHelper.postExceptionEventIfExist(Either.left(e), accessToken, jocError, controllerId);
                    } finally {
                        Globals.disconnect(session);
                    }
                } else {
                    // AJocClusterService.setLogger(ClusterServices.dailyplan.name());
                    SOSHibernateSession session = null;
                    try {
                        String msg = either.getLeft().toString();
                        LOGGER.error(String.format("[%s][%s]%s[error]%s", method, controllerId, logSubmissionForDate, msg));

                        session = Globals.createSosHibernateStatelessConnection(method);
                        DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);
                        Instant start = Instant.now();

                        session.setAutoCommit(false);
                        Globals.beginTransaction(session);
                        int updateHistory = OrderApi.updateHistory(dbLayer, items, false, msg);
                        Globals.commit(session);
                        session.close();
                        session = null;

                        Instant end = Instant.now();
                        LOGGER.info(String.format("[%s][%s]%s[onError][rollback  submitted=false][updated history=%s(%s)]%s", method, controllerId,
                                logSubmissionForDate, updateHistory, SOSDate.getDuration(start, end), msg));

                        ProblemHelper.postProblemEventIfExist(either, accessToken, jocError, controllerId);
                    } catch (Throwable e) {
                        LOGGER.error(String.format("[%s][%s]%s %s", method, controllerId, logSubmissionForDate, e.toString()), e);
                        Globals.rollback(session);
                    } finally {
                        Globals.disconnect(session);
                    }
                }
            });
        }
        // if (fromService) {
        // AJocClusterService.setLogger(ClusterServices.dailyplan.name());
        // }
        return orders;
    }

    private static int updatePlannedOrders(DBLayerDailyPlannedOrders dbLayer, Set<OrderId> orders, String controllerId) throws SOSHibernateException {
        int result = 0;
        for (OrderId orderId : orders) {
            result += dbLayer.setSubmitted(controllerId, orderId.string());
        }
        return result;
    }

    private static int updateHistory(DBLayerDailyPlannedOrders dbLayer, List<DBItemDailyPlanHistory> items, boolean submitted, String message)
            throws SOSHibernateException {
        int result = 0;
        for (DBItemDailyPlanHistory item : items) {
            result += dbLayer.setHistorySubmitted(item.getId(), submitted, message);
        }
        return result;
    }
}
