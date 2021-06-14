package com.sos.joc.orders.impl;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.controller.model.workflow.WorkflowId;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.OrdersHelper;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals.DefaultSections;
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.db.orders.DBItemDailyPlanOrders;
import com.sos.joc.exceptions.ControllerObjectNotExistException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.order.CancelDailyPlanOrders;
import com.sos.joc.model.order.ModifyOrders;
import com.sos.joc.orders.resource.IOrdersResourceModify;
import com.sos.js7.order.initiator.OrderInitiatorSettings;
import com.sos.js7.order.initiator.classes.GlobalSettingsReader;
import com.sos.js7.order.initiator.classes.PlannedOrder;
import com.sos.js7.order.initiator.classes.PlannedOrderKey;
import com.sos.js7.order.initiator.db.DBLayerDailyPlannedOrders;
import com.sos.js7.order.initiator.db.FilterDailyPlannedOrders;
import com.sos.schema.JsonValidator;
import com.sos.schema.exception.SOSJsonSchemaException;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.item.VersionedItemId;
import js7.data.order.Order;
import js7.data.order.OrderId;
import js7.data.workflow.WorkflowPath;
import js7.data_for_java.command.JSuspendMode;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JHistoricOutcome;
import js7.data_for_java.order.JOrder;
import js7.data_for_java.order.JOrderPredicates;
import js7.data_for_java.workflow.JWorkflowId;
import js7.data_for_java.workflow.position.JPosition;
import scala.Function1;

@Path("orders")
public class OrdersResourceModifyImpl extends JOCResourceImpl implements IOrdersResourceModify {

    private static final String API_CALL = "./orders";
    private static final Logger LOGGER = LoggerFactory.getLogger(OrdersResourceModifyImpl.class);

    private enum Action {
        CANCEL, CANCEL_DAILYPLAN, SUSPEND, RESUME, REMOVE_WHEN_TERMINATED
    }

    @Override
    public JOCDefaultResponse postOrdersSuspend(String accessToken, byte[] filterBytes) {
        try {
            ModifyOrders modifyOrders = initRequest(Action.SUSPEND, accessToken, filterBytes);
            boolean perm = getControllerPermissions(modifyOrders.getControllerId(), accessToken).getOrders().getSuspendResume();
            JOCDefaultResponse jocDefaultResponse = initPermissions(modifyOrders.getControllerId(), perm);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            postOrdersModify(Action.SUSPEND, modifyOrders);
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    @Override
    public JOCDefaultResponse postOrdersResume(String accessToken, byte[] filterBytes) {
        try {
            ModifyOrders modifyOrders = initRequest(Action.RESUME, accessToken, filterBytes);
            boolean perm = getControllerPermissions(modifyOrders.getControllerId(), accessToken).getOrders().getSuspendResume();
            JOCDefaultResponse jocDefaultResponse = initPermissions(modifyOrders.getControllerId(), perm);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            postOrdersModify(Action.RESUME, modifyOrders);
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    @Override
    public JOCDefaultResponse postOrdersCancel(String accessToken, byte[] filterBytes) {
        try {
            ModifyOrders modifyOrders = initRequest(Action.CANCEL, accessToken, filterBytes);
            boolean perm = getControllerPermissions(modifyOrders.getControllerId(), accessToken).getOrders().getCancel();
            JOCDefaultResponse jocDefaultResponse = initPermissions(modifyOrders.getControllerId(), perm);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            postOrdersModify(Action.CANCEL, modifyOrders);
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    @Override
    public JOCDefaultResponse postOrdersDailyPlanCancel(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL + "/dailyplan/cancel" , filterBytes, accessToken);
            JsonValidator.validate(filterBytes, CancelDailyPlanOrders.class);
            CancelDailyPlanOrders cancelDailyPlanOrders = Globals.objectMapper.readValue(filterBytes, CancelDailyPlanOrders.class);
            
            boolean perm = getControllerPermissions(cancelDailyPlanOrders.getControllerId(), accessToken).getOrders().getCancel();
            JOCDefaultResponse jocDefaultResponse = initPermissions(cancelDailyPlanOrders.getControllerId(), perm);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            addSubmittedOrderIdsFromDailyplanDate(cancelDailyPlanOrders);
            
            // @Uwe: you have only pending orders then you don't need
            // arguments, kill, position, orderType in CancelDailyPlanOrders schema
//            ModifyOrders modifyOrders = new ModifyOrders();
//            modifyOrders.setControllerId(cancelDailyPlanOrders.getControllerId());
//            modifyOrders.setOrderIds(cancelDailyPlanOrders.getOrderIds());
//            modifyOrders.setOrderType(OrderModeType.FRESH_ONLY);
//            modifyOrders.setAuditLog(cancelDailyPlanOrders.getAuditLog());
            
            ModifyOrders modifyOrders = new ModifyOrders();
            modifyOrders.setControllerId(cancelDailyPlanOrders.getControllerId());
            modifyOrders.setKill(cancelDailyPlanOrders.getKill());
            modifyOrders.setOrderIds(cancelDailyPlanOrders.getOrderIds());
            modifyOrders.setOrderType(cancelDailyPlanOrders.getOrderType());
            modifyOrders.setAuditLog(cancelDailyPlanOrders.getAuditLog());
            
            postOrdersModify(Action.CANCEL_DAILYPLAN, modifyOrders);            
            
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    @Override
    public JOCDefaultResponse postOrdersRemoveWhenTerminated(String accessToken, byte[] filterBytes) {
        try {
            ModifyOrders modifyOrders = initRequest(Action.REMOVE_WHEN_TERMINATED, accessToken, filterBytes);
            boolean perm = getControllerPermissions(modifyOrders.getControllerId(), accessToken).getOrders().getView();
            JOCDefaultResponse jocDefaultResponse = initPermissions(modifyOrders.getControllerId(), perm);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            // for AuditLog
            modifyOrders.setArguments(null);
            modifyOrders.setKill(null);
            modifyOrders.setOrderType(null);
            modifyOrders.setPosition(null);
            postOrdersModify(Action.REMOVE_WHEN_TERMINATED, modifyOrders);
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    public void postOrdersModify(Action action, ModifyOrders modifyOrders) throws Exception {
        CategoryType category = CategoryType.CONTROLLER;
        if (Action.CANCEL_DAILYPLAN.equals(action)) {
            category = CategoryType.DAILYPLAN;
        }
        DBItemJocAuditLog dbAuditLog = storeAuditLog(modifyOrders.getAuditLog(), modifyOrders.getControllerId(), category);

        Set<String> orders = modifyOrders.getOrderIds();

        List<WorkflowId> workflowIds = modifyOrders.getWorkflowIds();
        if (workflowIds != null && workflowIds.isEmpty()) {
            modifyOrders.setWorkflowIds(null); // for AuditLog 
        }
        
        String controllerId = modifyOrders.getControllerId();
        JControllerState currentState = Proxy.of(controllerId).currentState();
        Stream<JOrder> orderStream = Stream.empty();

        if (orders != null && !orders.isEmpty()) {
            orderStream = currentState.ordersBy(o -> orders.contains(o.id().string()));
            // determine possibly fresh cyclic orders in case of CANCEL
            if (Action.CANCEL.equals(action) || Action.CANCEL_DAILYPLAN.equals(action)) {
                // determine cyclic ids
                orderStream = Stream.concat(orderStream, cyclicFreshOrderIds(orders, currentState));
            }
        } else if (workflowIds != null && !workflowIds.isEmpty()) {
            Predicate<WorkflowId> versionNotEmpty = w -> w.getVersionId() != null && !w.getVersionId().isEmpty();
            Set<VersionedItemId<WorkflowPath>> workflowPaths = workflowIds.stream().filter(versionNotEmpty).map(w -> JWorkflowId.of(JocInventory
                    .pathToName(w.getPath()), w.getVersionId()).asScala()).collect(Collectors.toSet());
            Set<WorkflowPath> workflowPaths2 = workflowIds.stream().filter(w -> !versionNotEmpty.test(w)).map(w -> WorkflowPath.of(JocInventory
                    .pathToName(w.getPath()))).collect(Collectors.toSet());
            Function1<Order<Order.State>, Object> workflowFilter = o -> (workflowPaths.contains(o.workflowId()) || workflowPaths2.contains(o
                    .workflowId().path()));
            orderStream = currentState.ordersBy(workflowFilter);
        }
        
        final Set<JOrder> jOrders = getJOrders(action, orderStream, controllerId, folderPermissions.getListOfFolders());
        
        if (!jOrders.isEmpty() || Action.CANCEL_DAILYPLAN.equals(action)) {
            command(currentState, action, modifyOrders, dbAuditLog, jOrders.stream().map(JOrder::id).collect(Collectors.toSet())).thenAccept(either -> {
                ProblemHelper.postProblemEventIfExist(either, getAccessToken(), getJocError(), controllerId);
                if (either.isRight()) {
                    OrdersHelper.storeAuditLogDetailsFromJOrders(jOrders, dbAuditLog.getId()).thenAccept(either2 -> ProblemHelper
                            .postExceptionEventIfExist(either2, getAccessToken(), getJocError(), controllerId));
                }
            });
        } else {
            throwControllerObjectNotExistException(action);
        }
    }
    
    private void throwControllerObjectNotExistException(Action action) throws ControllerObjectNotExistException {
        switch (action) {
        case RESUME:
            throw new ControllerObjectNotExistException("No failed or suspended orders found.");
        default:
            throw new ControllerObjectNotExistException("No orders found.");
        }
        
    }

    private Set<JOrder> getJOrders(Action action, Stream<JOrder> orderStream, String controllerId) {
        if (Action.RESUME.equals(action)) {
            Map<Boolean, Set<JOrder>> suspendedOrFailedOrders = orderStream.collect(Collectors.groupingBy(o -> OrdersHelper.isSuspendedOrFailed(o),
                    Collectors.toSet()));
            if (suspendedOrFailedOrders.containsKey(Boolean.FALSE)) {
                String msg = suspendedOrFailedOrders.get(Boolean.FALSE).stream().map(o -> o.id().string()).collect(Collectors.joining("', '",
                        "Orders '", "' not failed or suspended"));
                // ProblemHelper.postProblemEventIfExist(Either.left(Problem.pure(msg)), getAccessToken(), getJocError(), controllerId);
                LOGGER.info(getJocError().printMetaInfo());
                LOGGER.warn(msg);
                getJocError().clearMetaInfo();

            }
            return suspendedOrFailedOrders.getOrDefault(Boolean.TRUE, Collections.emptySet());
        }
        return orderStream.collect(Collectors.toSet());
    }
    
    private Set<JOrder> getJOrders(Action action, Stream<JOrder> orderStream, String controllerId, Set<Folder> permittedFolders) {
        final Set<JOrder> jOrders = getJOrders(action, orderStream, controllerId);
        return jOrders.stream().filter(o -> canAdd(WorkflowPaths.getPath(o.workflowId().path().string()), permittedFolders)).collect(Collectors.toSet());
    }

    private static Stream<JOrder> cyclicFreshOrderIds(Collection<String> orderIds, JControllerState currentState) {
        Stream<JOrder> cyclicOrderStream = Stream.empty();
        // determine cyclic ids
        Set<String> freshCyclicIds = orderIds.stream().filter(s -> s.matches(".*#C[0-9]+-.*")).map(s -> currentState.idToOrder(OrderId.of(s))).filter(
                Optional::isPresent).map(Optional::get).filter(o -> Order.Fresh$.class.isInstance(o.asScala().state())).map(o -> o.id().string()
                        .substring(0, 24)).collect(Collectors.toSet());
        if (!freshCyclicIds.isEmpty()) {
            cyclicOrderStream = currentState.ordersBy(JOrderPredicates.and(JOrderPredicates.byOrderState(Order.Fresh$.class), o -> freshCyclicIds
                    .contains(o.id().string().substring(0, 24))));
        }
        return cyclicOrderStream;
    }

    private CompletableFuture<Either<Problem, Void>> command(JControllerState currentState, Action action, ModifyOrders modifyOrders,DBItemJocAuditLog dbAuditLog, Set<OrderId> oIds) {

        switch (action) {
        case CANCEL_DAILYPLAN:
            
            if (modifyOrders.getOrderIds() != null) {
                Set<String> orders = modifyOrders.getOrderIds().stream().filter(s -> !s.matches(".*#(T|F)[0-9]+-.*")).collect(Collectors.toSet());
                orders.removeAll(oIds.stream().map(OrderId::string).collect(Collectors.toSet()));
                try {
                    updateDailyPlan(orders);
                    // TODO @Uwe auditLog stuff for these orders
                } catch (SOSHibernateException e1) {
                    // TODO @Uwe Why catch with ProblemHelper.postExceptionEventIfExist instead of throw Exception
                    ProblemHelper.postExceptionEventIfExist(Either.left(e1), getAccessToken(), getJocError(), modifyOrders.getControllerId());
                }
            }
            
            if (oIds.isEmpty()) {
                return CompletableFuture.supplyAsync(() -> Either.right(null));
            } else {
                return OrdersHelper.cancelOrders(modifyOrders, oIds).thenApply(either -> {
                    if (either.isRight()) {
                        updateDailyPlan(oIds, modifyOrders.getControllerId());
                    }
                    return either;
                });
            }
        case CANCEL:
            return OrdersHelper.cancelOrders(modifyOrders, oIds).thenApply(either -> {
                // TODO This update must be removed when dailyplan service receives events for order state changes
                if (either.isRight()) {
                    updateDailyPlan(oIds, modifyOrders.getControllerId());
                }
                return either;
            });

        case RESUME:
            if (oIds.size() == 1) { // position and historicOutcome only for one Order!
                Optional<List<JHistoricOutcome>> historyOutcomes = Optional.empty(); // TODO parameter resp. historicOutcome
                Optional<JPosition> position = Optional.empty();
                if (modifyOrders.getPosition() != null && !modifyOrders.getPosition().isEmpty()) {
                    Either<Problem, JPosition> posEither = JPosition.fromList(modifyOrders.getPosition());
                    ProblemHelper.throwProblemIfExist(posEither);
                    position = Optional.of(posEither.get());
                }
                return ControllerApi.of(modifyOrders.getControllerId()).resumeOrder(oIds.iterator().next(), position, historyOutcomes);
            }
            return ControllerApi.of(modifyOrders.getControllerId()).resumeOrders(oIds);
        case SUSPEND:
            JSuspendMode suspendMode = null;
            if (modifyOrders.getKill() == Boolean.TRUE) {
                suspendMode = JSuspendMode.kill(true);
            } else {
                suspendMode = JSuspendMode.kill();
            }
            return ControllerApi.of(modifyOrders.getControllerId()).suspendOrders(oIds, suspendMode);
        default: // case REMOVE_WHEN_TERMINATED
            return ControllerApi.of(modifyOrders.getControllerId()).deleteOrdersWhenTerminated(oIds);
        }
    }
    
    private void updateDailyPlan(Set<OrderId> oIds, String controllerId) {
        try {
            // only for non-temporary and non-file orders
            LOGGER.debug("Cancel orders. Calling updateDailyPlan");
            updateDailyPlan(oIds.stream().map(OrderId::string).filter(s -> !s.matches(".*#(T|F)[0-9]+-.*")).collect(Collectors.toSet()));
        } catch (Exception e) {
            ProblemHelper.postExceptionEventIfExist(Either.left(e), getAccessToken(), getJocError(), controllerId);
        }
    }

    private static void updateDailyPlan(Collection<String> orderIds) throws SOSHibernateException {
        SOSHibernateSession sosHibernateSession = null;
        if (!orderIds.isEmpty()) {
            try {
                sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL + "/cancel");
                sosHibernateSession.setAutoCommit(false);
                DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders = new DBLayerDailyPlannedOrders(sosHibernateSession);
                Globals.beginTransaction(sosHibernateSession);
                FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
                filter.setListOfOrders(orderIds);
                filter.setSubmitted(false);
                dbLayerDailyPlannedOrders.setSubmitted(filter);

                Globals.commit(sosHibernateSession);
            } catch (Exception e) {
                Globals.rollback(sosHibernateSession);
                throw e;
            } finally {
                Globals.disconnect(sosHibernateSession);
            }
        } else {
            LOGGER.debug("No orderIds to be updated in daily plan");
        }
    }

    private ModifyOrders initRequest(Action action, String accessToken, byte[] filterBytes) throws SOSJsonSchemaException, IOException {
        initLogging(API_CALL + "/" + action.name().toLowerCase(), filterBytes, accessToken);
        JsonValidator.validate(filterBytes, ModifyOrders.class);
        return Globals.objectMapper.readValue(filterBytes, ModifyOrders.class);
    }
    
    private void addSubmittedOrderIdsFromDailyplanDate(CancelDailyPlanOrders cancelDailyPlanOrders) throws Exception {
        if (cancelDailyPlanOrders.getDailyPlanDate() != null) {
            SOSHibernateSession sosHibernateSession = null;
            if (cancelDailyPlanOrders.getOrderIds() == null) {
                cancelDailyPlanOrders.setOrderIds(new LinkedHashSet<String>());
            }

            try {
                sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
                sosHibernateSession.setAutoCommit(false);
                GlobalSettingsReader reader = new GlobalSettingsReader();
                OrderInitiatorSettings settings;
                if (Globals.configurationGlobals != null) {

                    AConfigurationSection configuration = Globals.configurationGlobals.getConfigurationSection(DefaultSections.dailyplan);
                    settings = reader.getSettings(configuration);
                } else {
                    settings = new OrderInitiatorSettings();
                    settings.setTimeZone("Europe/Berlin");
                    settings.setPeriodBegin("00:00");
                }
                DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders = new DBLayerDailyPlannedOrders(sosHibernateSession);

                FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
                filter.setControllerId(cancelDailyPlanOrders.getControllerId());
                filter.setDailyPlanDate(cancelDailyPlanOrders.getDailyPlanDate(), settings.getTimeZone(), settings.getPeriodBegin());
                filter.setSubmitted(true);

                List<DBItemDailyPlanOrders> listOfPlannedOrders = dbLayerDailyPlannedOrders.getDailyPlanList(filter, 0);
                if (listOfPlannedOrders != null) {
                    cancelDailyPlanOrders.getOrderIds().addAll(listOfPlannedOrders.stream().map(DBItemDailyPlanOrders::getOrderId).collect(Collectors
                            .toSet()));
                }
            } finally {
                Globals.disconnect(sosHibernateSession);
            }
        }
    }

}