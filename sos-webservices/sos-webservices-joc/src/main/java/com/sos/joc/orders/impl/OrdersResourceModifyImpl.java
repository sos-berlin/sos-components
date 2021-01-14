package com.sos.joc.orders.impl;

import java.io.IOException;
import java.nio.file.Paths;
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

import com.sos.jobscheduler.model.order.OrderModeType;
import com.sos.jobscheduler.model.workflow.WorkflowId;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.order.ModifyOrders;
import com.sos.joc.orders.resource.IOrdersResourceModify;
import com.sos.schema.JsonValidator;
import com.sos.schema.exception.SOSJsonSchemaException;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.item.VersionedItemId;
import js7.data.order.Order;
import js7.data.order.OrderId;
import js7.data.workflow.WorkflowPath;
import js7.proxy.javaapi.data.command.JCancelMode;
import js7.proxy.javaapi.data.command.JSuspendMode;
import js7.proxy.javaapi.data.controller.JControllerState;
import js7.proxy.javaapi.data.order.JOrder;
import js7.proxy.javaapi.data.workflow.JWorkflowId;
import js7.proxy.javaapi.data.workflow.position.JPosition;

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
//        try {
            checkRequiredComment(modifyOrders.getAuditLog());

            List<String> orders = modifyOrders.getOrderIds();
            List<WorkflowId> workflowIds = modifyOrders.getWorkflowIds();
            final Set<Folder> permittedFolders = folderPermissions.getListOfFolders();

            JControllerState currentState = Proxy.of(modifyOrders.getControllerId()).currentState();
            Stream<OrderId> orderStream = Stream.empty();

            if (orders != null && !orders.isEmpty()) {
                orderStream = currentState.ordersBy(o -> orders.contains(o.id().string()) && orderIsPermitted(o, permittedFolders)).map(JOrder::id);
            } else if (workflowIds != null && !workflowIds.isEmpty()) {
                Set<VersionedItemId<WorkflowPath>> workflowPaths = workflowIds.stream().map(w -> JWorkflowId.of(w.getPath(), w.getVersionId()).asScala()).collect(
                        Collectors.toSet());
                orderStream = currentState.ordersBy(o -> workflowPaths.contains(o.workflowId()) && orderIsPermitted(o, permittedFolders)).map(JOrder::id);
            }
                
//            Either<Problem, Void> either = callCommand(action, modifyOrders, orderStream.collect(Collectors.toSet()))
//                    .get(Globals.httpSocketTimeout, TimeUnit.MILLISECONDS);
//            ProblemHelper.throwProblemIfExist(either);
            callCommand(action, modifyOrders, orderStream.collect(Collectors.toSet())).thenAccept(either -> ProblemHelper.postProblemEventIfExist(either,
                    getAccessToken(), getJocError(), modifyOrders.getControllerId()));
        //ProblemHelper.throwProblemIfExist(either);
            //TODO auditLog
//        } catch (TimeoutException e) {
//            // TODO
//        }
    }
    
    private static CompletableFuture<Either<Problem, Void>> callCommand(Action action, ModifyOrders modifyOrders, Set<OrderId> oIds) {
        Optional<JPosition> position = Optional.empty();
        if (modifyOrders.getPosition() != null) {
            JPosition.fromList(modifyOrders.getPosition());
        }
        
        switch (action) {
        case CANCEL:
            // TODO a fresh order should cancelled by a dailyplan method!
            JCancelMode cancelMode = null;
            if (OrderModeType.FRESH_ONLY.equals(modifyOrders.getOrderType())) {
                cancelMode = JCancelMode.freshOnly();
            } else if (modifyOrders.getKill() == Boolean.TRUE) {
                cancelMode = JCancelMode.kill(true);
            } else {
                cancelMode = JCancelMode.kill();
            }
            // TODO position! Why JWorkflowPosition instead JPosition?
            return ControllerApi.of(modifyOrders.getControllerId()).cancelOrders(oIds, cancelMode);
        case RESUME:
            //TODO missing parameter!
            // resumeOrder
//            if (oIds.size() == 1) {
//                return ControllerApi.of(modifyOrders.getControllerId()).resumeOrder(oIds.iterator().next(), position, ..);
//            }
            return ControllerApi.of(modifyOrders.getControllerId()).resumeOrders(oIds);
        case SUSPEND:
            //TODO position! Why JWorkflowPosition instead JPosition?
            JSuspendMode suspendMode = null;
            if (modifyOrders.getKill() == Boolean.TRUE) {
                suspendMode = JSuspendMode.kill(true);
            } else {
                suspendMode = JSuspendMode.kill();
            }
            return ControllerApi.of(modifyOrders.getControllerId()).suspendOrders(oIds, suspendMode);
        default: //case REMOVE_WHEN_TERMINATED
            return ControllerApi.of(modifyOrders.getControllerId()).removeOrdersWhenTerminated(oIds);
        }
    }
    
//    private static Err419 getBulkError(String orderId, String controllerId, JocError jocError) {
//        String errMsg = String.format("Order '%s' doesn't exist in controller '%s'", orderId, controllerId);
//        return new BulkError().get(new JobSchedulerObjectNotExistException(errMsg), jocError, orderId);
//    }
//
//    private static WorkflowPosition getWorkflowPosition(ModifyOrder mOrder, WorkflowId workflowId) {
//        WorkflowPosition pos = null;
//        if (mOrder.getPosition() != null && !mOrder.getPosition().isEmpty()) {
//            pos = new WorkflowPosition(workflowId, mOrder.getPosition());
//        }
//        return pos;
//    }
//
//    private static boolean getKillImmediately(ModifyOrder mOrder) {
//        return mOrder.getKill() == Boolean.TRUE;
//    }
//
//    private static OrderMode getOrderMode(ModifyOrder mOrder, WorkflowId workflowId) {
//        OrderMode orderMode = new OrderMode(mOrder.getOrderType(), null);
//        WorkflowPosition pos = getWorkflowPosition(mOrder, workflowId);
//        if (pos != null || getKillImmediately(mOrder)) {
//            orderMode.setKill(new Kill(getKillImmediately(mOrder), pos));
//        }
//        return orderMode;
//    }
    
    private static boolean orderIsPermitted(Order<Order.State> order, Set<Folder> listOfFolders) {
        if (listOfFolders == null || listOfFolders.isEmpty()) {
            return true;
        }
        return folderIsPermitted(Paths.get(order.workflowId().path().string()).getParent().toString().replace('\\', '/'), listOfFolders);
    }
    
    private static boolean folderIsPermitted(String folder, Set<Folder> listOfFolders) {
        Predicate<Folder> filter = f -> f.getFolder().equals(folder) || (f.getRecursive() && ("/".equals(f.getFolder()) || folder.startsWith(f
                .getFolder() + "/")));
        return listOfFolders.stream().parallel().anyMatch(filter);
    }
    
    private ModifyOrders initRequest(Action action, String accessToken, byte[] filterBytes) throws SOSJsonSchemaException, IOException {
        initLogging(API_CALL + "/" + action.name().toLowerCase(), filterBytes, accessToken);
        JsonValidator.validate(filterBytes, ModifyOrders.class);
        return Globals.objectMapper.readValue(filterBytes, ModifyOrders.class);
    }
    
}