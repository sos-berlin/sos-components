package com.sos.joc.dailyplan;

import java.time.Instant;
import java.util.HashSet;
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
import com.sos.inventory.model.schedule.OrderPositions;
import com.sos.joc.Globals;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.service.JocClusterServiceLogger;
import com.sos.joc.dailyplan.common.PlannedOrder;
import com.sos.joc.dailyplan.db.DBLayerDailyPlannedOrders;
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

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.order.OrderId;
import js7.data.value.Value;
import js7.data.workflow.WorkflowPath;
import js7.data_for_java.order.JFreshOrder;
import js7.data_for_java.workflow.position.JBranchPath;
import js7.data_for_java.workflow.position.JPosition;
import js7.data_for_java.workflow.position.JPositionOrLabel;
import js7.proxy.javaapi.JControllerApi;
import js7.proxy.javaapi.JControllerProxy;
import reactor.core.publisher.Flux;

public class OrderApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderApi.class);

    @SuppressWarnings("unchecked")
    private static Optional<JPositionOrLabel> getPositionOrLabel(Object position, Map<String, List<Object>> labelToPositionMap) {
        if (position != null) {
            if (position instanceof List<?>) {
                return getPosition((List<Object>) position);
            } else if (position instanceof String) {
                // TODO JOC-1453 consider labels
                return getPosition(labelToPositionMap.get((String) position));

                // if (!label.isEmpty()) {
                // return Optional.of(JLabel.of(label));
                // }
                // return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private static Optional<JPositionOrLabel> getPosition(List<Object> pos) {
        if (pos != null && !pos.isEmpty()) {
            Either<Problem, JPosition> posE = JPosition.fromList(pos);
            ProblemHelper.throwProblemIfExist(posE);
            return Optional.of(posE.get());
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private static JBranchPath getBlockPosition(Object position, Map<String, List<Object>> labelToPositionMap) {
        if (position != null) {
            if (position instanceof List<?>) {
                return getBlockPosition((List<Object>) position);
            } else if (position instanceof String) {
                return getBlockPosition(labelToPositionMap.get((String) position));
            }
        }
        return JBranchPath.empty();
    }

    private static JBranchPath getBlockPosition(List<Object> pos) {
        if (pos != null && !pos.isEmpty()) {
            Either<Problem, JBranchPath> posE = JBranchPath.fromList(pos);
            ProblemHelper.throwProblemIfExist(posE);
            return posE.get();
        }
        return JBranchPath.empty();
    }

    private static JFreshOrder mapToFreshOrder(FreshOrder order, Map<String, List<Object>> labelToPositionMap) {
        Optional<Instant> scheduledFor = Optional.empty();
        if (order.getScheduledFor() != null) {
            scheduledFor = Optional.of(Instant.ofEpochMilli(order.getScheduledFor()));
        } else {
            scheduledFor = Optional.of(Instant.now());
        }
        OrderPositions positions = order.getPositions();
        Optional<JPositionOrLabel> startPosition = Optional.empty();
        JBranchPath blockPosition = JBranchPath.empty();
        Set<JPositionOrLabel> endPositions = new HashSet<>();
        if (positions != null) {
            // TODO JOC-1453 consider labels
            startPosition = getPositionOrLabel(positions.getStartPosition(), labelToPositionMap);
            if (positions.getEndPositions() != null) {
                for (Object endPosition : positions.getEndPositions()) {
                    getPositionOrLabel(endPosition, labelToPositionMap).ifPresent(p -> endPositions.add(p));
                }
            }
            // TODO blockPosition
            // labelToBlockPositionMap instead labelToPositionMap
            blockPosition = getBlockPosition(positions.getBlockPosition(), labelToPositionMap);

        }
        Map<String, Value> arguments = OrdersHelper.variablesToScalaValuedArguments(order.getArguments());
        boolean forceJobAdmission = order.getForceJobAdmission() == Boolean.TRUE;
        return JFreshOrder.of(OrderId.of(order.getId()), WorkflowPath.of(order.getWorkflowPath()), scheduledFor, arguments, false, forceJobAdmission,
                blockPosition, startPosition, endPositions);
    }

    public static Set<PlannedOrder> addOrdersToController(StartupMode startupMode, String callerForLog, String controllerId, String dailyPlanDate,
            Set<PlannedOrder> orders, List<DBItemDailyPlanHistory> items, JocError jocError, String accessToken)
            throws ControllerConnectionResetException, ControllerConnectionRefusedException, DBMissingDataException, JocConfigurationException,
            DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException, InterruptedException, ExecutionException {

        // TODO JOC-1453 determine workflows from order and their possible positions
        // convert labels to positions

        final String method = "addOrdersToController";
        String logDailyPlanDate = SOSString.isEmpty(dailyPlanDate) ? "" : "[" + dailyPlanDate + "]";
        final String lp = String.format("[%s]%s[%s][%s]%s", startupMode, callerForLog, method, controllerId, logDailyPlanDate);

        Function<PlannedOrder, Either<Err419, JFreshOrder>> mapper = order -> {
            Either<Err419, JFreshOrder> either = null;
            try {
                either = Either.right(mapToFreshOrder(order.getFreshOrder(), order.getLabelToPositionMap()));
            } catch (Exception ex) {
                either = Either.left(new BulkError(LOGGER).get(ex, jocError, order.getFreshOrder().getId()));
            }
            return either;
        };

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("%s update submit state for history and order", lp));
        }
        Map<Boolean, Set<Either<Err419, JFreshOrder>>> freshOrders = orders.stream().map(mapper).collect(Collectors.groupingBy(Either::isRight,
                Collectors.toSet()));
        final Map<OrderId, JFreshOrder> map = freshOrders.get(true).stream().map(Either::get).collect(Collectors.toMap(JFreshOrder::id, Function
                .identity()));

        if (freshOrders.containsKey(true) && !freshOrders.get(true).isEmpty()) {
            JControllerApi controllerApi = ControllerApi.of(controllerId);
            final Set<OrderId> set = map.keySet();
            String add = Proxies.isCoupled(controllerId) ? "" : "not ";
            LOGGER.info(String.format("%s[%scoupled with proxy]start submitting %s orders ...", lp, add, set.size()));

            final boolean log2serviceFile = true; // !StartupMode.manual.equals(startupMode);
            final JControllerProxy proxy = Proxy.of(controllerId);
            proxy.api().addOrders(Flux.fromIterable(map.values())).thenAccept(either -> {
                if (log2serviceFile) {
                    JocClusterServiceLogger.setLogger(ClusterServices.dailyplan.name());
                }
                if (either.isRight()) {
                    // JocClusterServiceLogger.setLogger(ClusterServices.dailyplan.name());
                    // Set<OrderId> set = map.keySet();
                    SOSHibernateSession session = null;
                    try {
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

                        controllerApi.deleteOrdersWhenTerminated(set).thenAccept(e -> ProblemHelper.postProblemEventIfExist(e, accessToken, jocError,
                                controllerId));

                        Instant end = Instant.now();
                        LOGGER.info(String.format("%s[submitted=%s][updated db planned orders=%s(%s),history=%s(%s)]", lp, set.size(), updateOrders,
                                SOSDate.getDuration(start, updateOrdersEnd), updateHistory, SOSDate.getDuration(updateOrdersEnd, end)));
                    } catch (Exception e) {
                        // LOGGER.error(String.format("%s %s", lp, e.toString()), e);
                        Globals.rollback(session);
                        ProblemHelper.postExceptionEventIfExist(Either.left(e), accessToken, jocError, controllerId);
                    } finally {
                        Globals.disconnect(session);
                    }
                } else {
                    // JocClusterServiceLogger.setLogger(ClusterServices.dailyplan.name());
                    SOSHibernateSession session = null;
                    try {
                        String msg = either.getLeft().toString();
                        // LOGGER.error(String.format("%s[error]%s", lp, msg));

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
                        LOGGER.info(String.format("%s[onError][rollback  submitted=false][updated history=%s(%s)]%s", lp, updateHistory, SOSDate
                                .getDuration(start, end), msg));

                        ProblemHelper.postProblemEventIfExist(either, accessToken, jocError, controllerId);
                    } catch (Throwable e) {
                        LOGGER.error(String.format("%s %s", lp, e.toString()), e);
                        Globals.rollback(session);
                    } finally {
                        Globals.disconnect(session);
                    }
                }
            });
        }
        // if (fromService) {
        // JocClusterServiceLogger.setLogger(ClusterServices.dailyplan.name());
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
