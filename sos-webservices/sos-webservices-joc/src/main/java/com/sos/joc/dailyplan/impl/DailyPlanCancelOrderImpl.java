package com.sos.joc.dailyplan.impl;

import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.dailyplan.common.DailyPlanSettings;
import com.sos.joc.dailyplan.common.DailyPlanUtils;
import com.sos.joc.dailyplan.common.JOCOrderResourceImpl;
import com.sos.joc.dailyplan.db.DBLayerDailyPlannedOrders;
import com.sos.joc.dailyplan.db.FilterDailyPlannedOrders;
import com.sos.joc.dailyplan.resource.IDailyPlanCancelOrder;
import com.sos.joc.db.dailyplan.DBItemDailyPlanOrder;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.dailyplan.DailyPlanEvent;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;
import com.sos.joc.exceptions.ControllerConnectionResetException;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocAccessDeniedException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.dailyplan.DailyPlanOrderFilterDef;
import com.sos.joc.orders.impl.OrdersResourceModifyImpl;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import jakarta.ws.rs.Path;
import js7.base.problem.Problem;
import js7.data.order.OrderId;
import js7.data_for_java.command.JCancellationMode;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JOrder;
import js7.proxy.javaapi.JControllerProxy;

@Path(WebservicePaths.DAILYPLAN)
public class DailyPlanCancelOrderImpl extends JOCOrderResourceImpl implements IDailyPlanCancelOrder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanCancelOrderImpl.class);

    @Override
    public JOCDefaultResponse postCancelOrder(String accessToken, byte[] filterBytes) {
        try {
            initLogging(IMPL_PATH, filterBytes, accessToken);
            // validation without required dailyPlanDateFrom
            JsonValidator.validateFailFast(filterBytes, "orderManagement/dailyplan/dailyPlanOrdersFilterDef-schema.json");
            DailyPlanOrderFilterDef in = Globals.objectMapper.readValue(filterBytes, DailyPlanOrderFilterDef.class);

            JOCDefaultResponse response = initPermissions(null, true);
            if (response != null) {
                return response;
            }
            try {
                cancelOrders(in, accessToken, true, true);
            } catch (JocAccessDeniedException e) {
                return accessDeniedResponse();
            }

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    public Map<String, List<DBItemDailyPlanOrder>> getSubmittedOrderIdsFromDailyplanDate(DailyPlanOrderFilterDef in, String accessToken,
            boolean withAudit, boolean withEvent) throws SOSHibernateException, ControllerConnectionResetException,
            ControllerConnectionRefusedException, DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException,
            DBConnectionRefusedException, ExecutionException {

        setSettings(IMPL_PATH);
        Map<String, List<DBItemDailyPlanOrder>> ordersPerControllerIds = DailyPlanUtils.getOrderIdsFromDailyplanDate(in, getSettings(), true,
                IMPL_PATH);

        if (!ordersPerControllerIds.isEmpty()) {
            ordersPerControllerIds = ordersPerControllerIds.entrySet().stream().filter(availableController -> getControllerPermissions(
                    availableController.getKey(), accessToken).getOrders().getCancel()).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

            if (ordersPerControllerIds.keySet().isEmpty()) {
                throw new JocAccessDeniedException("No permissions to cancel dailyplan orders");
            }
        } else {
            initGetPermissions(accessToken);
        }
        return ordersPerControllerIds;
    }

    public Map<String, CompletableFuture<Either<Problem, Void>>> cancelOrders(Map<String, List<DBItemDailyPlanOrder>> ordersPerController,
            String accessToken, AuditParams auditLog, boolean withAudit, boolean withEvent) throws SOSHibernateException,
            ControllerConnectionResetException, ControllerConnectionRefusedException, DBMissingDataException, JocConfigurationException,
            DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException, ExecutionException {

        Map<String, CompletableFuture<Either<Problem, Void>>> futures = new HashMap<>();

        Long auditLogId = withAudit ? storeAuditLog(auditLog, CategoryType.DAILYPLAN).getId() : 0L;

        if (folderPermissions == null) {
            folderPermissions = jobschedulerUser.getSOSAuthCurrentAccount().getSosAuthFolderPermissions();
        }

        for (Map.Entry<String, List<DBItemDailyPlanOrder>> entry : ordersPerController.entrySet()) {
            String controllerId = entry.getKey();
            List<DBItemDailyPlanOrder> orders = entry.getValue();
            final Set<Folder> permittedFolders = folderPermissions.getListOfFolders(controllerId);
            final Set<String> orderIds = orders.stream().filter(o -> folderIsPermitted(o.getWorkflowFolder(), permittedFolders)).map(
                    DBItemDailyPlanOrder::getOrderId).collect(Collectors.toSet());

            final JControllerProxy proxy = Proxy.of(controllerId);
            final JControllerState currentState = proxy.currentState();

            Stream<JOrder> orderStream = Stream.empty();
            if (!orderIds.isEmpty()) {
                orderStream = currentState.ordersBy(o -> orderIds.contains(o.id().string()));
                // determine possibly fresh cyclic orders
                orderStream = Stream.concat(orderStream, OrdersResourceModifyImpl.cyclicFreshOrderIds(orderIds, currentState));
            }
            final Set<JOrder> jOrders = orderStream.filter(OrdersHelper::isPendingOrScheduledOrBlocked).collect(Collectors.toSet());
            final Set<String> oIds = jOrders.stream().map(JOrder::id).map(OrderId::string).collect(Collectors.toSet());

            updateUnknownOrders(controllerId, orderIds, oIds, getSettings(), withEvent);

            if (orderIds != null) {
                oIds.forEach(o -> orderIds.remove(o));

                updateDailyPlan("command", orderIds, withEvent);
            }

            if (!jOrders.isEmpty()) {
                futures.put(controllerId, proxy.api().cancelOrders(jOrders.stream().map(JOrder::id).collect(Collectors.toSet()), JCancellationMode
                        .freshOnly()).thenApply(either -> {
                            if (either.isRight()) {
                                try {
                                    updateDailyPlan("updateDailyPlan", oIds, withEvent);
                                    if (withAudit) {
                                        OrdersHelper.storeAuditLogDetailsFromJOrders(jOrders, auditLogId, controllerId).thenAccept(either2 -> {
                                            if (withEvent) {
                                                ProblemHelper.postExceptionEventIfExist(either2, accessToken, getJocError(), controllerId);
                                            }
                                        });
                                    }
                                } catch (Exception e) {
                                    if (withEvent) {
                                        ProblemHelper.postExceptionEventIfExist(Either.left(e), accessToken, getJocError(), controllerId);
                                    }
                                    either = Either.left(Problem.pure(e.toString()));
                                }
                            } else {
                                if (withEvent) {
                                    ProblemHelper.postProblemEventIfExist(either, accessToken, getJocError(), controllerId);
                                }
                            }
                            return either;
                        }));
            }
        }

        return futures;
    }

    public Map<String, CompletableFuture<Either<Problem, Void>>> cancelOrders(DailyPlanOrderFilterDef in, String accessToken, boolean withAudit,
            boolean withEvent) throws SOSHibernateException, ControllerConnectionResetException, ControllerConnectionRefusedException,
            DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException,
            ExecutionException {

        return cancelOrders(getSubmittedOrderIdsFromDailyplanDate(in, accessToken, withAudit, withEvent), accessToken, in.getAuditLog(), withAudit,
                withEvent);
    }

    private void updateUnknownOrders(String controllerId, Set<String> orders, Set<String> oIds, DailyPlanSettings settings, boolean withEvent)
            throws SOSHibernateException {
        if (orders != null && !orders.isEmpty()) {
            SOSHibernateSession session = null;
            try {
                session = Globals.createSosHibernateStatelessConnection(IMPL_PATH + " updateUnknownOrders");
                DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);

                FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
                filter.setControllerId(controllerId);

                Set<String> orderIds = new HashSet<>();
                for (String orderId : orders) {
                    filter.setOrderId(orderId);
                    if (dbLayer.getUniqueDailyPlan(filter) != null) {
                        orderIds.add(orderId);
                    }
                }
                Globals.disconnect(session);
                session = null; // to avoid nested openSessions

                for (String orderId : orders) {
                    dbLayer.addCyclicOrderIds(orderIds, orderId, controllerId, settings.getTimeZone(), settings.getPeriodBegin());
                }
                int orderIdsSize = orderIds.size();

                orderIds.removeAll(oIds);

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("[updateUnknownOrders][jOrderIds=%s][orderIds(before remove jOrderIds=%s)=%s]", oIds.size(),
                            orderIdsSize, orderIds.size()));
                }
                updateDailyPlan("updateUnknownOrders", orderIds, withEvent);
            } finally {
                Globals.disconnect(session);
            }
        }
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
