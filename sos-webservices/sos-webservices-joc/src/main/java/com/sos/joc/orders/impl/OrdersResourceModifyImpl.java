package com.sos.joc.orders.impl;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.controller.model.order.OrderModeType;
import com.sos.controller.model.workflow.WorkflowId;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.OrdersHelper;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.order.ModifyOrders;
import com.sos.joc.model.order.OrderStateText;
import com.sos.joc.orders.resource.IOrdersResourceModify;
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
import js7.data_for_java.command.JCancelMode;
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

    private enum Action {
        CANCEL, SUSPEND, RESUME, REMOVE_WHEN_TERMINATED
    }

    @Override
    public JOCDefaultResponse postOrdersSuspend(String accessToken, byte[] filterBytes) {
        try {
            ModifyOrders modifyOrders = initRequest(Action.SUSPEND, accessToken, filterBytes);
            boolean perm = getPermissonsJocCockpit(modifyOrders.getControllerId(), accessToken).getOrder().getExecute().isSuspend();
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
            boolean perm = getPermissonsJocCockpit(modifyOrders.getControllerId(), accessToken).getOrder().getExecute().isResume();
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
            // TODO permissions
            boolean perm = getPermissonsJocCockpit(modifyOrders.getControllerId(), accessToken).getOrder().getExecute().isSuspend();
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
    public JOCDefaultResponse postOrdersRemoveWhenTerminated(String accessToken, byte[] filterBytes) {
        try {
            ModifyOrders modifyOrders = initRequest(Action.REMOVE_WHEN_TERMINATED, accessToken, filterBytes);
            // TODO permissions
            boolean perm = getPermissonsJocCockpit(modifyOrders.getControllerId(), accessToken).getOrder().getExecute().isSuspend();
            JOCDefaultResponse jocDefaultResponse = initPermissions(modifyOrders.getControllerId(), perm);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
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
        checkRequiredComment(modifyOrders.getAuditLog());

        List<String> orders = modifyOrders.getOrderIds();
        List<WorkflowId> workflowIds = modifyOrders.getWorkflowIds();
        // final Set<Folder> permittedFolders = folderPermissions.getListOfFolders();

        JControllerState currentState = Proxy.of(modifyOrders.getControllerId()).currentState();
        Stream<OrderId> orderStream = Stream.empty();
        Stream<OrderId> cyclicOrderStream = Stream.empty();
        
        // TODO folder permissions

        if (orders != null && !orders.isEmpty()) {
            orderStream = currentState.ordersBy(o -> orders.contains(o.id().string())).map(JOrder::id);
            // determine possibly fresh cyclic orders in case of CANCEL
            if (Action.CANCEL.equals(action)) {
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
            orderStream = currentState.ordersBy(workflowFilter).map(JOrder::id);
        }

        callCommand(action, modifyOrders, orderStream.collect(Collectors.toSet())).thenAccept(either -> ProblemHelper.postProblemEventIfExist(either,
                getAccessToken(), getJocError(), modifyOrders.getControllerId()));
    }
    
    private static Stream<OrderId> cyclicFreshOrderIds(List<String> orderIds, JControllerState currentState) {
        Stream<OrderId> cyclicOrderStream = Stream.empty();
        // determine cyclic ids
        Set<String> freshCyclicIds = orderIds.stream().filter(s -> s.matches(".*#C[0-9]{10}-.*")).map(s -> currentState.idToOrder(OrderId.of(
                s))).filter(Optional::isPresent).map(Optional::get).filter(o -> Order.Fresh.class.isInstance(o.asScala().state())).map(o -> o
                        .id().string().substring(0, 24)).collect(Collectors.toSet());
        if (!freshCyclicIds.isEmpty()) {
            Function1<Order<Order.State>, Object> cyclicOrderFilter = JOrderPredicates.and(JOrderPredicates.byOrderState(Order.Fresh.class),
                    o -> freshCyclicIds.contains(o.id().string().substring(0, 24)));
            cyclicOrderStream = currentState.ordersBy(cyclicOrderFilter).map(JOrder::id);
        }
        return cyclicOrderStream;
    }

    private static void updateDailyPlan(List<String> orderIds) throws SOSHibernateException {
        SOSHibernateSession sosHibernateSession = null;

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
    }

    private static CompletableFuture<Either<Problem, Void>> callCommand(Action action, ModifyOrders modifyOrders, Set<OrderId> oIds)
            throws SOSHibernateException {
        
        switch (action) {
        case CANCEL:

            // TODO This update must be removed when dailyplan service receives events for order state changes
            // this is the wrong place anyway -> must be in thenAccept if "Either" right
            updateDailyPlan(oIds.stream().map(OrderId::string).filter(s -> !s.matches(".*#T[0-9]+-.*")).collect(Collectors.toList()));

            // TODO a fresh order should cancelled by a dailyplan method!
            JCancelMode cancelMode = null;
            if (OrderModeType.FRESH_ONLY.equals(modifyOrders.getOrderType())) {
                cancelMode = JCancelMode.freshOnly();
            } else if (modifyOrders.getKill() == Boolean.TRUE) {
                cancelMode = JCancelMode.kill(true);
            } else {
                cancelMode = JCancelMode.kill();
            }
            return ControllerApi.of(modifyOrders.getControllerId()).cancelOrders(oIds, cancelMode);
        case RESUME:
            if (oIds.size() == 1) { // position and historicOutcome only for one Order!
                Optional<List<JHistoricOutcome>> historyOutcomes = Optional.empty(); // TODO parameter resp. historicOutcome
                Optional<JPosition> position = Optional.empty();
                if (modifyOrders.getPosition() != null) {
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
            return ControllerApi.of(modifyOrders.getControllerId()).removeOrdersWhenTerminated(oIds);
        }
    }

    // private static Err419 getBulkError(String orderId, String controllerId, JocError jocError) {
    // String errMsg = String.format("Order '%s' doesn't exist in controller '%s'", orderId, controllerId);
    // return new BulkError().get(new JobSchedulerObjectNotExistException(errMsg), jocError, orderId);
    // }
    //
    // private static WorkflowPosition getWorkflowPosition(ModifyOrder mOrder, WorkflowId workflowId) {
    // WorkflowPosition pos = null;
    // if (mOrder.getPosition() != null && !mOrder.getPosition().isEmpty()) {
    // pos = new WorkflowPosition(workflowId, mOrder.getPosition());
    // }
    // return pos;
    // }
    //
    // private static boolean getKillImmediately(ModifyOrder mOrder) {
    // return mOrder.getKill() == Boolean.TRUE;
    // }
    //
    // private static OrderMode getOrderMode(ModifyOrder mOrder, WorkflowId workflowId) {
    // OrderMode orderMode = new OrderMode(mOrder.getOrderType(), null);
    // WorkflowPosition pos = getWorkflowPosition(mOrder, workflowId);
    // if (pos != null || getKillImmediately(mOrder)) {
    // orderMode.setKill(new Kill(getKillImmediately(mOrder), pos));
    // }
    // return orderMode;
    // }

    // private static boolean orderIsPermitted(Order<Order.State> order, Set<Folder> listOfFolders) {
    // TODO order.workflowId().path().string() is only a name
    // return true;
    // if (listOfFolders == null || listOfFolders.isEmpty()) {
    // return true;
    // }
    // return folderIsPermitted(Paths.get(order.workflowId().path().string()).getParent().toString().replace('\\', '/'), listOfFolders);
    // }

    // private static boolean folderIsPermitted(String folder, Set<Folder> listOfFolders) {
    // Predicate<Folder> filter = f -> f.getFolder().equals(folder) || (f.getRecursive() && ("/".equals(f.getFolder()) || folder.startsWith(f
    // .getFolder() + "/")));
    // return listOfFolders.stream().parallel().anyMatch(filter);
    // }

    private ModifyOrders initRequest(Action action, String accessToken, byte[] filterBytes) throws SOSJsonSchemaException, IOException {
        initLogging(API_CALL + "/" + action.name().toLowerCase(), filterBytes, accessToken);
        JsonValidator.validate(filterBytes, ModifyOrders.class);
        return Globals.objectMapper.readValue(filterBytes, ModifyOrders.class);
    }

}