package com.sos.joc.dailyplan;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.controller.model.order.FreshOrder;
import com.sos.inventory.model.common.Variables;
import com.sos.inventory.model.schedule.OrderPositions;
import com.sos.inventory.model.workflow.Requirements;
import com.sos.joc.Globals;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.board.PlanSchemas;
import com.sos.joc.classes.inventory.JsonConverter;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.settings.ClusterSettings;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.service.JocClusterServiceLogger;
import com.sos.joc.dailyplan.common.PlannedOrder;
import com.sos.joc.dailyplan.db.DBLayerDailyPlannedOrders;
import com.sos.joc.db.dailyplan.DBItemDailyPlanHistory;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;
import com.sos.joc.exceptions.ControllerConnectionResetException;
import com.sos.joc.exceptions.ControllerObjectNotExistException;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.model.cluster.common.ClusterServices;
import com.sos.sign.model.workflow.Workflow;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.order.OrderId;
import js7.data.plan.PlanSchemaId;
import js7.data.value.Value;
import js7.data.workflow.WorkflowPath;
import js7.data_for_java.controller.JControllerCommand;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.item.JRepo;
import js7.data_for_java.order.JFreshOrder;
import js7.data_for_java.order.JOrder;
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
            return Optional.of(getPos(JPosition.fromList(pos)));
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
            return getPos(JBranchPath.fromList(pos));
        }
        return JBranchPath.empty();
    }
    
    private static <T> T getPos(Either<Problem, T> either) {
        ProblemHelper.throwProblemIfExist(either);
        return either.get();
    }

    private static JFreshOrder mapToFreshOrder(FreshOrder order, Map<String, List<Object>> labelToPositionMap, PlanSchemaId planSchemaId,
            Map<WorkflowPath, Optional<Requirements>> workflowToOrderPreparation, boolean allowEmptyArguments) throws JsonMappingException,
            JsonProcessingException {
        WorkflowPath workflowPath = WorkflowPath.of(order.getWorkflowPath());
   
        Optional<Requirements> orderPrep = workflowToOrderPreparation.get(workflowPath);
    
        if (orderPrep == null) {
            throw new ControllerObjectNotExistException("unknown workflow: " + order.getWorkflowPath());
        }
        Variables args = order.getArguments();
        if (orderPrep.isPresent()) {
            args = OrdersHelper.checkArguments(order.getArguments(), orderPrep.get(), allowEmptyArguments);
        }
        Map<String, Value> arguments = OrdersHelper.variablesToScalaValuedArguments(args);
        
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
        boolean forceJobAdmission = order.getForceJobAdmission() == Boolean.TRUE;
        return JFreshOrder.of(OrderId.of(order.getId()), workflowPath, arguments, scheduledFor, OrdersHelper.getDailyPlanPlanId(planSchemaId, order
                .getId()), false, forceJobAdmission, blockPosition, startPosition, endPositions);
    }

    public static Set<PlannedOrder> addOrdersToController(StartupMode startupMode, String callerForLog, String controllerId, String dailyPlanDate,
            Set<PlannedOrder> orders, Map<String, DBItemDailyPlanHistory> items, JocError jocError, String accessToken)
            throws ControllerConnectionResetException, ControllerConnectionRefusedException, DBMissingDataException, JocConfigurationException,
            DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException, InterruptedException, ExecutionException {

        // TODO JOC-1453 determine workflows from order and their possible positions
        // convert labels to positions

        final String method = "addOrdersToController";
        String logDailyPlanDate = SOSString.isEmpty(dailyPlanDate) ? "" : "[" + dailyPlanDate + "]";
        final String lp = String.format("[%s]%s[%s][%s]%s", startupMode, callerForLog, method, controllerId, logDailyPlanDate);
        JControllerProxy proxy = Proxy.of(controllerId);
        JControllerState currentState = proxy.currentState();
        PlanSchemaId planSchemaId = PlanSchemas.getDailyPlanPlanSchemaIfExists(currentState);
        JRepo repo = currentState.repo();
        final boolean allowEmptyArguments = ClusterSettings.getAllowEmptyArguments(Globals.getConfigurationGlobalsJoc());

        Map<WorkflowPath, Optional<Requirements>> workflowToOrderPreparation = orders.stream().map(PlannedOrder::getWorkflowName).distinct().map(
                WorkflowPath::of).map(w -> repo.pathToCheckedWorkflow(w)).filter(Either::isRight).map(Either::get).collect(Collectors.toMap(w -> w
                        .id().path(), w -> {
                            try {
                                Workflow wo = Globals.objectMapper.readValue(w.toJson(), Workflow.class);
                                return Optional.ofNullable(JsonConverter.signOrderPreparationToInvOrderPreparation(wo.getOrderPreparation()));
                            } catch (Exception e) {
                                return Optional.empty();
                            }
                        }));

        Function<PlannedOrder, Either<PlannedOrder, JFreshOrder>> mapper = order -> {
            Either<PlannedOrder, JFreshOrder> either = null;
            try {
                either = Either.right(mapToFreshOrder(order.getFreshOrder(), order.getLabelToPositionMap(), planSchemaId, workflowToOrderPreparation,
                        allowEmptyArguments));
            } catch (Exception ex) {
                order.setException(ex);
                either = Either.left(order);
            }
            return either;
        };

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("%s update submit state for history and order", lp));
        }
        Map<Boolean, Set<Either<PlannedOrder, JFreshOrder>>> freshOrders = orders.stream().map(mapper).collect(Collectors.groupingBy(Either::isRight,
                Collectors.toSet()));
        
        if (freshOrders.containsKey(true) && !freshOrders.get(true).isEmpty()) {
            final Map<OrderId, JFreshOrder> map = freshOrders.get(true).stream().map(Either::get).collect(Collectors.toMap(JFreshOrder::id, Function
                    .identity()));
            
            final Set<OrderId> set = map.keySet();
//            String add = Proxies.isCoupled(controllerId) ? "" : "not ";
//            LOGGER.info(String.format("%s[%scoupled with proxy]start submitting %s orders ...", lp, add, set.size()));
            
            boolean submitOrdersIndividually = Globals.getConfigurationGlobalsDailyPlan().getSubmitOrdersIndividually();
            LOGGER.info(String.format("%s start submitting %s orders%s", lp, set.size(), submitOrdersIndividually ? " individually" : ""));

            final boolean log2serviceFile = true; // !StartupMode.manual.equals(startupMode);
            if (submitOrdersIndividually) {
                List<JControllerCommand> addOrderCommands = map.values().stream().map(JControllerCommand::addOrder).collect(Collectors.toList());
                proxy.api().executeCommand(JControllerCommand.batch(addOrderCommands)).thenAccept(either -> {
                    if (log2serviceFile) {
                        JocClusterServiceLogger.setLogger(ClusterServices.dailyplan.name());
                    }
                    if (either.isRight()) { 
                        /* this "either" is almost always "right" when using JControllerCommand.batch: 
                         * either.toString() -> Right(BatchResponse(1 succeeded and 1 failed))
                         */
                        
                        // check if all orders are added
                        Map<OrderId, JOrder> knownOrders = proxy.currentState().idToOrder();
                        if (set.stream().anyMatch(oId -> knownOrders.get(oId) == null)) {
                            // at least one failed -> check again after 2 seconds
                            try {
                                TimeUnit.SECONDS.sleep(2);
                            } catch (InterruptedException e) {
                                //
                            }
                            Set<OrderId> newKnownOrderIds = proxy.currentState().idToOrder().keySet();
                            Map<Boolean, Set<OrderId>> orderIdsToSuccessOrNot = set.stream().collect(Collectors.groupingBy(newKnownOrderIds::contains,
                                    Collectors.toSet()));

                            if (orderIdsToSuccessOrNot.containsKey(Boolean.TRUE)) {
                                updateHistoryOnSuccess(proxy.api(), orderIdsToSuccessOrNot.get(Boolean.TRUE), items, jocError, accessToken,
                                        controllerId, method, lp);
                            }
                            if (orderIdsToSuccessOrNot.containsKey(Boolean.FALSE)) {
                                updateHistoryOnError(null, orderIdsToSuccessOrNot.get(Boolean.FALSE), items, jocError, accessToken, controllerId,
                                        method, lp);
                            }

                        } else {
                            updateHistoryOnSuccess(proxy.api(), set, items, jocError, accessToken, controllerId, method, lp);
                        }

                    } else {
                        updateHistoryOnError(either, set, items, jocError, accessToken, controllerId, method, lp);
                    }
                });
            } else {
                proxy.api().addOrders(Flux.fromIterable(map.values())).thenAccept(either -> {
                    if (log2serviceFile) {
                        JocClusterServiceLogger.setLogger(ClusterServices.dailyplan.name());
                    }
                    if (either.isRight()) {
                        updateHistoryOnSuccess(proxy.api(), set, items, jocError, accessToken, controllerId, method, lp);
                    } else {
                        updateHistoryOnError(either, set, items, jocError, accessToken, controllerId, method, lp);
                    }
                });
            }
        }
        
        if (freshOrders.containsKey(false) && !freshOrders.get(false).isEmpty()) {
            SOSHibernateSession session = null;
            try {
                session = Globals.createSosHibernateStatelessConnection(method);
                DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);
                Instant start = Instant.now();

                session.setAutoCommit(false);
                Globals.beginTransaction(session);
                int updateHistory = OrderApi.updateHistory(dbLayer, freshOrders.get(false), items);
                Globals.commit(session);
                session.close();
                session = null;

                String msg = freshOrders.get(false).stream().map(Either::getLeft).map(PlannedOrder::getException).filter(Objects::nonNull).map(
                        Exception::getMessage).distinct().collect(Collectors.joining(", "));
                Instant end = Instant.now();
                LOGGER.info(String.format("%s[onError][submitted=false][updated history=%s(%s)]%s", lp, updateHistory, SOSDate.getDuration(
                        start, end), msg));
                if (jocError != null) {
                    ProblemHelper.postProblemEventIfExist(Either.left(Problem.pure(msg)), accessToken, jocError, controllerId);
                }
            } catch (Throwable e) {
                Globals.rollback(session);
                if (jocError != null) {
                    ProblemHelper.postExceptionEventIfExist(Either.left(e), accessToken, jocError, controllerId);
                }
                LOGGER.error(String.format("%s %s", lp, e.toString()), e);
            } finally {
                Globals.disconnect(session);
            }
        }
        return orders;
    }
    
    private static void updateHistoryOnSuccess(JControllerApi controllerApi, Set<OrderId> set, Map<String, DBItemDailyPlanHistory> items,
            JocError jocError, String accessToken, String controllerId, String method, String lp) {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(method);
            DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);

            Instant start = Instant.now();

            session.setAutoCommit(false);
            Globals.beginTransaction(session);
            int updateOrders = updatePlannedOrders(dbLayer, set, controllerId);
            Instant updateOrdersEnd = Instant.now();
            int updateHistory = updateHistory(dbLayer, set, items, true, null);
            Globals.commit(session);
            session.close();
            session = null;

            controllerApi.deleteOrdersWhenTerminated(set).thenAccept(e -> ProblemHelper.postProblemEventIfExist(e, accessToken, jocError,
                    controllerId));

            Instant end = Instant.now();
            LOGGER.info(String.format("%s[submitted=%s][updated db planned orders=%s(%s),history=%s(%s)]", lp, set.size(), updateOrders, SOSDate
                    .getDuration(start, updateOrdersEnd), updateHistory, SOSDate.getDuration(updateOrdersEnd, end)));
        } catch (Throwable e) {
            Globals.rollback(session);
            if (jocError != null) {
                ProblemHelper.postExceptionEventIfExist(Either.left(e), accessToken, jocError, controllerId);
            }
            LOGGER.error(String.format("%s %s", lp, e.toString()), e);
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private static void updateHistoryOnError(Either<Problem, ?> either, Set<OrderId> set, Map<String, DBItemDailyPlanHistory> items,
            JocError jocError, String accessToken, String controllerId, String method, String lp) {
        SOSHibernateSession session = null;
        try {
            String msg = either == null ? null : either.getLeft().toString();

            session = Globals.createSosHibernateStatelessConnection(method);
            DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);
            Instant start = Instant.now();

            session.setAutoCommit(false);
            Globals.beginTransaction(session);
            int updateHistory = OrderApi.updateHistory(dbLayer, set, items, false, msg);
            Globals.commit(session);
            session.close();
            session = null;

            Instant end = Instant.now();
            
            if (either == null) {
                msg = "submission failed for " + set.size() + "orders";
                either = Either.left(Problem.pure(msg));
            }
            LOGGER.info(String.format("%s[onError][rollback submitted=false][updated history=%s(%s)]%s", lp, updateHistory, SOSDate.getDuration(start,
                    end), msg));
            if (jocError != null) {
                ProblemHelper.postProblemEventIfExist(either, accessToken, jocError, controllerId);
            }
        } catch (Throwable e) {
            LOGGER.error(String.format("%s %s", lp, e.toString()), e);
            Globals.rollback(session);
        } finally {
            Globals.disconnect(session);
        }
    }

    private static int updatePlannedOrders(DBLayerDailyPlannedOrders dbLayer, Set<OrderId> orderIds, String controllerId) throws SOSHibernateException {
        int result = 0;
        for (OrderId orderId : orderIds) {
            result += dbLayer.setSubmitted(controllerId, orderId.string());
        }
        return result;
    }

    private static int updateHistory(DBLayerDailyPlannedOrders dbLayer, Set<OrderId> orderIds, Map<String, DBItemDailyPlanHistory> items,
            boolean submitted, String message) throws SOSHibernateException {
        int result = 0;
        for (OrderId orderId : orderIds) {
            DBItemDailyPlanHistory item = items.get(orderId.string());
            if (item != null) {
                result += dbLayer.setHistorySubmitted(item.getId(), submitted, message);
            }
        }
        return result;
    }
    
    private static int updateHistory(DBLayerDailyPlannedOrders dbLayer, Set<Either<PlannedOrder, JFreshOrder>> errors,
            Map<String, DBItemDailyPlanHistory> items) throws SOSHibernateException {
        int result = 0;
        for (Either<PlannedOrder, JFreshOrder> error : errors) {
            if (error.isLeft()) {
                PlannedOrder e = error.getLeft();
                DBItemDailyPlanHistory item = items.get(e.getFreshOrder().getId());
                if (item != null) {
                    result += dbLayer.setHistorySubmitted(item.getId(), false, e.getExceptionMessage());
                }
            }
        }
        return result;
    }
}
