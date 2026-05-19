package com.sos.joc.dailyplan.impl;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.classes.controller.ControllerCommandResponse;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.dailyplan.common.DailyPlanSettings;
import com.sos.joc.dailyplan.common.DailyPlanUtils;
import com.sos.joc.dailyplan.common.JOCOrderResourceImpl;
import com.sos.joc.dailyplan.db.DBLayerDailyPlannedOrders;
import com.sos.joc.dailyplan.db.FilterDailyPlannedOrders;
import com.sos.joc.dailyplan.resource.IDailyPlanCancelOrder;
import com.sos.joc.db.dailyplan.DBItemDailyPlanOrder;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.dailyplan.DailyPlanEvent;
import com.sos.joc.event.bean.problem.ProblemEvent;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;
import com.sos.joc.exceptions.ControllerConnectionResetException;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocAccessDeniedException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.dailyplan.DailyPlanOrderFilterDef;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import jakarta.ws.rs.Path;
import js7.base.problem.Problem;
import js7.data.order.Order;
import js7.data.order.OrderId;
import js7.data_for_java.command.JCancellationMode;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JOrder;
import js7.data_for_java.order.JOrderPredicates;
import js7.proxy.javaapi.JControllerProxy;
import reactor.core.publisher.Flux;

@Path(WebservicePaths.DAILYPLAN)
public class DailyPlanCancelOrderImpl extends JOCOrderResourceImpl implements IDailyPlanCancelOrder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanCancelOrderImpl.class);

    @Override
    public JOCDefaultResponse postCancelOrder(String accessToken, byte[] filterBytes) {
        try {
            filterBytes = initLogging(IMPL_PATH, filterBytes, accessToken, CategoryType.DAILYPLAN);
            // validation without required dailyPlanDateFrom
            JsonValidator.validateFailFast(filterBytes, "orderManagement/dailyplan/dailyPlanOrdersFilterDef-schema.json");
            DailyPlanOrderFilterDef in = Globals.objectMapper.readValue(filterBytes, DailyPlanOrderFilterDef.class);

            JOCDefaultResponse response = initPermissions(null, true);
            if (response != null) {
                return response;
            }
            try {
                cancelOrders(in, accessToken);
            } catch (JocAccessDeniedException e) {
                return accessDeniedResponse();
            }

            return responseStatusJSOk(Date.from(Instant.now()));

        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

    public Map<String, List<DBItemDailyPlanOrder>> getSubmittedOrderIdsFromDailyplanDate(DailyPlanOrderFilterDef in, String accessToken)
            throws SOSHibernateException, ControllerConnectionResetException, ControllerConnectionRefusedException, DBMissingDataException,
            JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException, ExecutionException {

        setSettings(IMPL_PATH);
        Map<String, List<DBItemDailyPlanOrder>> ordersPerControllerIds = DailyPlanUtils.getOrderIdsFromDailyplanDate(in, getSettings(), IMPL_PATH)
                .stream().collect(Collectors.groupingBy(DBItemDailyPlanOrder::getControllerId));

        if (!ordersPerControllerIds.isEmpty()) {
            ordersPerControllerIds = ordersPerControllerIds.entrySet().stream().filter(availableController -> getBasicControllerPermissions(
                    availableController.getKey(), accessToken).getOrders().getCancel()).collect(Collectors.toMap(Map.Entry::getKey,
                            Map.Entry::getValue));

            if (ordersPerControllerIds.keySet().isEmpty()) {
                throw new JocAccessDeniedException("No permissions to cancel dailyplan orders");
            }
        } else {
            initGetPermissions(accessToken);
        }
        return ordersPerControllerIds;
    }

    public synchronized Map<String, CompletableFuture<ControllerCommandResponse>> cancelOrders(
            Map<String, List<DBItemDailyPlanOrder>> ordersPerController, String accessToken) throws SOSHibernateException,
            ControllerConnectionResetException, ControllerConnectionRefusedException, DBMissingDataException, JocConfigurationException,
            DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException, ExecutionException {
        
        Map<String, CompletableFuture<ControllerCommandResponse>> futures = new HashMap<>();
        for (Map.Entry<String, List<DBItemDailyPlanOrder>> entry : ordersPerController.entrySet()) {
            String controllerId = entry.getKey();
            List<DBItemDailyPlanOrder> orders = entry.getValue();
            final Set<String> orderIds = orders.stream().map(DBItemDailyPlanOrder::getOrderId).collect(Collectors.toSet());
            final JControllerProxy proxy = Proxy.of(controllerId);
            
            CompletableFuture<ControllerCommandResponse> response = cancelRecursively(proxy, orderIds, controllerId, futures, Either.right(null), 0)
            .thenApply(either -> {
                if (either.isRight()) {
                    try {
                        updateDailyPlan("cancelOrders", orderIds, false);
                        return new ControllerCommandResponse(controllerId);
                    } catch (Exception ex) {
                        return new ControllerCommandResponse(controllerId, Optional.of(ex));
                    }
                } else {
                    return new ControllerCommandResponse(controllerId, Optional.of(ProblemHelper.getExceptionOfProblem(either.getLeft())));
                }
            });
            futures.put(controllerId, response);
        }
        return futures;
    }
    
    private CompletableFuture<Either<Problem, Void>> cancelRecursively(JControllerProxy proxy, Set<String> orderIds, String controllerId,
            Map<String, CompletableFuture<ControllerCommandResponse>> futures, Either<Problem, Void> either, int count) {
        if(count > 0) {
            LOGGER.info("error occurred. retry cancel order. " + count + " try. " + ProblemHelper.getErrorMessage(either.getLeft()));
        }
        if (count >= 3) {
            return CompletableFuture.completedFuture(either);
        }
        final JControllerState currentState = proxy.currentState();
        Stream<JOrder> orderStream = Stream.empty();
        if (!orderIds.isEmpty()) {
            orderStream = currentState.ordersBy(JOrderPredicates.and(JOrderPredicates.byOrderState(Order.Fresh.class), o -> orderIds.contains(o
                    .id().string())));
        }
        final Set<OrderId> oIds = orderStream.map(JOrder::id).collect(Collectors.toSet());
        CompletableFuture<Either<Problem, Void>> future = oIds.isEmpty() ? CompletableFuture.completedFuture(Either.right(null)) :  
            proxy.api().deleteOrdersWhenTerminated(Flux.fromIterable(oIds)).thenCompose(e -> proxy.api().cancelOrders(
                    oIds, JCancellationMode.freshOnly()));
        int newCount = count + 1;
        return future.thenCompose(e -> {
            if (e.isLeft() && ProblemHelper.getErrorMessage(e.getLeft()).matches(".*(" + ProblemHelper.UNKNOWN_ORDER +"|" 
                    + ProblemHelper.CANCEL_STARTED_ORDER + "):.*")) {
                return cancelRecursively(proxy, orderIds, controllerId, futures, e, newCount);
            }
            return CompletableFuture.completedFuture(e);
        });
    }

    private synchronized Collection<CompletableFuture<ControllerCommandResponse>> cancelOrders(Map<String, List<DBItemDailyPlanOrder>> ordersPerController,
            String accessToken, AuditParams auditLog) throws SOSHibernateException,
            ControllerConnectionResetException, ControllerConnectionRefusedException, DBMissingDataException, JocConfigurationException,
            DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException, ExecutionException {

        Map<String, CompletableFuture<ControllerCommandResponse>> futures = new HashMap<>();

        Long auditLogId = storeAuditLog(auditLog).getId();

        if (folderPermissions == null) {
            folderPermissions = jobschedulerUser.getSOSAuthCurrentAccount().getSosAuthFolderPermissions();
        }

        for (Map.Entry<String, List<DBItemDailyPlanOrder>> entry : ordersPerController.entrySet()) {
            String controllerId = entry.getKey();
            List<DBItemDailyPlanOrder> orders = entry.getValue();
            
            final Set<Folder> permittedFolders = folderPermissions.getListOfFolders(controllerId);
            Set<DBItemDailyPlanOrder> permittedOrders = orders.stream().filter(o -> folderIsPermitted(o.getWorkflowFolder(), permittedFolders))
                    .collect(Collectors.toSet());
            final Set<String> orderIds = permittedOrders.stream().map(DBItemDailyPlanOrder::getOrderId).collect(Collectors.toSet());
            
            final JControllerProxy proxy = Proxy.of(controllerId);
            
            CompletableFuture<ControllerCommandResponse> response = cancelRecursively(proxy, orderIds, controllerId, futures, Either.right(null), 0)
            .thenApply(either -> {
                if (either.isRight()) {
                    try {
                        updateDailyPlan("cancelOrders", orderIds, true);
                        return new ControllerCommandResponse(controllerId);
                    } catch (Exception ex) {
                        return new ControllerCommandResponse(controllerId, Optional.of(ex));
                    }
                } else {
                    return new ControllerCommandResponse(controllerId, Optional.of(ProblemHelper.getExceptionOfProblem(either.getLeft())));
                }
            }).thenCompose(ccr -> {
                if (ccr.getException().isEmpty()) {
                    return OrdersHelper.storeAuditLogDetails(permittedOrders.stream()
                            .map(o -> new AuditLogDetail(WorkflowPaths.getPath(o.getWorkflowPath()), o.getOrderId(), controllerId))
                            .toList(), auditLogId).thenApply(e -> {
                        if (e.isRight()) {
                            return ccr; 
                        } else {
                            return new ControllerCommandResponse(controllerId, Optional.of(e.getLeft()));
                        }
                    }); 
                } else {
                    return CompletableFuture.completedStage(ccr);
                }
            });
            futures.put(controllerId, response);
        }

        return futures.values();
    }

    private void cancelOrders(DailyPlanOrderFilterDef in, String accessToken) throws SOSHibernateException, ControllerConnectionResetException,
            ControllerConnectionRefusedException, DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException,
            DBConnectionRefusedException, ExecutionException {
        
        Map<String, List<DBItemDailyPlanOrder>> ordersPerController = getSubmittedOrderIdsFromDailyplanDate(in, accessToken);
        
        if (in.getOrderIds() != null && !in.getOrderIds().isEmpty()) {
            Set<String> cyclicOrderIdMainParts = in.getOrderIds().stream().filter(OrdersHelper::isCyclicOrderId).map(
                    OrdersHelper::getCyclicOrderIdMainPart).collect(Collectors.toSet());
            if (!cyclicOrderIdMainParts.isEmpty()) {
                SortedSet<String> dailyPlanDatesOfOrderIds = cyclicOrderIdMainParts.stream().map(oId -> oId.substring(1, 11)).collect(Collectors
                        .toCollection(TreeSet::new));
                String dateFrom = dailyPlanDatesOfOrderIds.first(); // dateFrom
                String dateTo = dailyPlanDatesOfOrderIds.last(); // dateTo
                if (in.getDailyPlanDateFrom() != null) {
                    dateFrom = Arrays.asList(dateFrom, in.getDailyPlanDateFrom()).stream().sorted().toList().get(1);
                }
                if (in.getDailyPlanDateTo() != null) {
                    dateTo = Arrays.asList(dateTo, in.getDailyPlanDateTo()).stream().sorted().toList().get(0);
                }
                FilterDailyPlannedOrders filter = DailyPlanUtils.getFilterDailyPlannedOrders(in, null); // TODO null: see TODO in method getFilterDailyPlannedOrders
                filter.setOrderIds(null);
                filter.setStartMode(1); // only all cyclic orders
                filter.setSubmissionForDateFrom(JobSchedulerDate.getDateFrom(dateFrom + "T00:00:00Z", "UTC"));
                filter.setSubmissionForDateTo(JobSchedulerDate.getDateFrom(dateTo + "T00:00:00Z", "UTC"));
                Map<String, List<DBItemDailyPlanOrder>> ordersPerController2 = DailyPlanUtils.getOrderIdsFromDailyplanDate(filter, IMPL_PATH).stream()
                        .filter(item -> cyclicOrderIdMainParts.contains(OrdersHelper.getCyclicOrderIdMainPart(item.getOrderId()))).collect(Collectors
                                .groupingBy(DBItemDailyPlanOrder::getControllerId));

                ordersPerController.forEach((k, v) -> ordersPerController.put(k, Stream.concat(v.stream(), ordersPerController2.getOrDefault(k,
                        Collections.emptyList()).stream()).distinct().toList()));
            }
        }

        Collection<CompletableFuture<ControllerCommandResponse>> cancelOrderResponsePerController = cancelOrders(ordersPerController, accessToken, in
                .getAuditLog());

        CompletableFuture.allOf(cancelOrderResponsePerController.toArray(CompletableFuture[]::new)).thenRun(() -> {
            String message = cancelOrderResponsePerController.stream().map(CompletableFuture::join).filter(ControllerCommandResponse::hasException)
                    .peek(ccr -> {
                        getJocErrorWithPrintMetaInfoAndClear(LOGGER);
                        LOGGER.error(ccr.getControllerId(), ccr.getException().get());
                    }).map(c -> c.getControllerId() + ": " + c.getException().get().toString()).collect(Collectors.joining(System.lineSeparator()));
            if (!message.isEmpty()) {
                EventBus.getInstance().post(new ProblemEvent(accessToken, null, message));
            }
        });
    }

    private static void updateDailyPlan(String caller, Collection<String> orderIds, boolean withEvent) throws SOSHibernateException {
        // SOSClassUtil.printStackTrace(true, LOGGER);
        SOSHibernateSession session = null;
        if (!orderIds.isEmpty()) {
            try {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("[updateDailyPlan][caller=%s][orderIds=%s]%s", caller, orderIds.size(), String.join(",", orderIds)));
                }

                DailyPlanSettings settings = JOCOrderResourceImpl.getDailyPlanSettings(IMPL_PATH);

                FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
                filter.setOrderIds(orderIds);
                filter.setSubmitted(false);
                filter.setSortMode(null);
                filter.setOrderCriteria(null);

                session = Globals.createSosHibernateStatelessConnection(IMPL_PATH + "(" + caller + ")");
                session.setAutoCommit(false);
                DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);
                Globals.beginTransaction(session);
                dbLayer.setSubmitted(filter);
                Globals.commit(session);
                List<DBItemDailyPlanOrder> items = dbLayer.getDailyPlanList(filter, 0);
                Globals.disconnect(session);
                session = null;

                Set<String> days = new HashSet<String>();
                for (DBItemDailyPlanOrder item : items) {
                    String date = item.getDailyPlanDate(settings.getTimeZone(), settings.getPeriodBegin());
                    if (!days.contains(date)) {
                        days.add(date);
                        if (withEvent) {
                            EventBus.getInstance().post(new DailyPlanEvent(item.getControllerId(), date));
                        }
                    }
                }

            } catch (Exception e) {
                Globals.rollback(session);
                throw e;
            } finally {
                Globals.disconnect(session);
            }
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[updateDailyPlan][caller=%s]No orderIds to be updated in daily plan", caller));
            }
        }
    }

}
