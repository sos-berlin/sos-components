package com.sos.joc.orders.impl;

import java.nio.file.Paths;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
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

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.item.ItemId;
import js7.data.order.Order;
import js7.data.order.OrderId;
import js7.data.workflow.WorkflowPath;
import js7.proxy.javaapi.data.command.JCancelMode;
import js7.proxy.javaapi.data.controller.JControllerState;
import js7.proxy.javaapi.data.order.JOrder;
import js7.proxy.javaapi.data.workflow.JWorkflowId;
import js7.proxy.javaapi.data.workflow.position.JPosition;

@Path("orders")
public class OrdersResourceModifyImpl extends JOCResourceImpl implements IOrdersResourceModify {

    private static final String API_CALL = "./orders";

    private enum Action {
        CANCEL, SUSPEND, RESUME
    }

    @Override
    public JOCDefaultResponse postOrdersSuspend(String accessToken, byte[] filterBytes) {
        return postOrdersModify(Action.SUSPEND, accessToken, filterBytes);
    }

    @Override
    public JOCDefaultResponse postOrdersResume(String accessToken, byte[] filterBytes) {
        return postOrdersModify(Action.RESUME, accessToken, filterBytes);
    }

    @Override
    public JOCDefaultResponse postOrdersCancel(String accessToken, byte[] filterBytes) {
        return postOrdersModify(Action.CANCEL, accessToken, filterBytes);
    }

    public JOCDefaultResponse postOrdersModify(Action action, String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL + "/" + action.name().toLowerCase(), filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, ModifyOrders.class);
            ModifyOrders modifyOrders = Globals.objectMapper.readValue(filterBytes, ModifyOrders.class);
            // TODO permissions
            JOCDefaultResponse jocDefaultResponse = initPermissions(modifyOrders.getJobschedulerId(), getPermissonsJocCockpit(modifyOrders
                    .getJobschedulerId(), accessToken).getOrder().getExecute().isStart());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            checkRequiredComment(modifyOrders.getAuditLog());

            List<String> orders = modifyOrders.getOrderIds();
            List<WorkflowId> workflowIds = modifyOrders.getWorkflowIds();
            final Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
            
            JControllerState currentState = Proxy.of(modifyOrders.getJobschedulerId()).currentState();
            Stream<OrderId> orderStream = null;
            
            if (orders != null && !orders.isEmpty()) {
                orderStream = currentState.ordersBy(o -> orders.contains(o.id().string()) && orderIsPermitted(o, permittedFolders)).map(JOrder::id);
            } else if (workflowIds != null && !workflowIds.isEmpty()) {
                Set<ItemId<WorkflowPath>> workflowPaths = workflowIds.stream().map(w -> JWorkflowId.of(w.getPath(), w.getVersionId()).asScala())
                        .collect(Collectors.toSet());
                orderStream = currentState.ordersBy(o -> workflowPaths.contains(o.workflowId()) && orderIsPermitted(o, permittedFolders)).map(JOrder::id);
            }
            
            
            
            
//            final Map<String, ModifyOrder> orderIds = modifyOrders.getOrderIds().stream().collect(Collectors.toMap(ModifyOrder::getOrderId, Function
//                    .identity()));
//            Predicate<Order<Order.State>> permissions = o -> orderIds.containsKey(o.id().string()) && canAdd(o.workflowId().path().string(),
//                    permittedFolders);

//            Function<JOrder, Command> mapper = jOrder -> {
//                ModifyOrder mOrder = orderIds.remove(jOrder.id().string());
//                WorkflowId workflowId = new WorkflowId(jOrder.workflowId().path().string(), jOrder.workflowId().versionId().string());
//                ModifyOrderAudit orderAudit = new ModifyOrderAudit(mOrder, workflowId.getPath(), modifyOrders);
//                logAuditMessage(orderAudit);
//                storeAuditLogEntry(orderAudit);
//                switch (action) {
//                case CANCEL:
//                    return new CancelOrder(mOrder.getOrderId(), getOrderMode(mOrder, workflowId));
//                case SUSPEND:
//                    return new SuspendOrder(mOrder.getOrderId(), getOrderMode(mOrder, workflowId));
//                case RESUME:
//                    return new ResumeOrder(mOrder.getOrderId(), getWorkflowPosition(mOrder, workflowId), mOrder.getArguments());
//                default:
//                    return null;
//                }
//            };

//            JControllerProxy proxy = Proxy.of(modifyOrders.getJobschedulerId());
//            JControllerState currentState = proxy.currentState();
//            Set<OrderId> oIds = currentState.ordersBy(JPredicates.toScalaPredicate(permissions)).map(o -> o.id()).collect(Collectors.toSet());
            
            Either<Problem, Void> either = callCommand(action, modifyOrders, orderStream.collect(Collectors.toSet())).get(Globals.httpSocketTimeout,
                    TimeUnit.MILLISECONDS);
            ProblemHelper.throwProblemIfExist(either);
//            .thenApply(either -> {
//                if (either.isLeft()) {
//                    return ProblemHelper.getExceptionOfProblem(either.getLeft());
//                }
//            });
            
//            List<Command> commands = currentState.ordersBy(JPredicates.toScalaPredicate(permissions)).map(mapper).filter(Objects::nonNull).collect(
//                    Collectors.toList());
//            if (commands != null && !commands.isEmpty()) {
//                Either<Problem, JControllerCommand> contollerCommand = JControllerCommand.fromJson(Globals.objectMapper.writeValueAsString(
//                        new JSBatchCommands(commands)));
//                if (contollerCommand.isRight()) {
//                    try {
//                        Either<Problem, ControllerCommand.Response> response = proxy.api().executeCommand(contollerCommand.get()).get(
//                                Globals.httpSocketTimeout, TimeUnit.MILLISECONDS);
//                        ProblemHelper.throwProblemIfExist(response);
//                    } catch (TimeoutException e) {
//                        throw new JobSchedulerNoResponseException(String.format("No response from controller '%s' after %ds", modifyOrders
//                                .getJobschedulerId(), Globals.httpSocketTimeout));
//                    }
//                } else {
//                    throw new JobSchedulerInvalidResponseDataException(ProblemHelper.getErrorMessage(contollerCommand.getLeft()));
//                }
//            }

//            if (orderIds != null && !orderIds.isEmpty()) {
//                List<Err419> bulkErrors = orderIds.keySet().stream().filter(o -> currentState.idToCheckedOrder(OrderId.of(o)).isLeft()).map(
//                        o -> getBulkError(o, modifyOrders.getJobschedulerId(), getJocError())).collect(Collectors.toList());
//                if (bulkErrors != null && !bulkErrors.isEmpty()) {
//                    return JOCDefaultResponse.responseStatus419(bulkErrors);
//                }
//            }
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    private static CompletableFuture<Either<Problem, Void>> callCommand(Action action, ModifyOrders modifyOrders, Set<OrderId> oIds) {
        Optional<JPosition> position = Optional.empty();
        if (modifyOrders.getPosition() != null) {
            JPosition.fromList(modifyOrders.getPosition());
        }
        
        switch (action) {
        case CANCEL:
            JCancelMode cancelMode = null;
            if (OrderModeType.FRESH_ONLY.equals(modifyOrders.getOrderType())) {
                cancelMode = JCancelMode.freshOnly();
            } else if (modifyOrders.getKill() == Boolean.TRUE) {
                cancelMode = JCancelMode.kill(true);
            } else {
                cancelMode = JCancelMode.kill();
            }
            // TODO position
            return ControllerApi.of(modifyOrders.getJobschedulerId()).cancelOrders(oIds, cancelMode);
        case RESUME:
            //TODO missing parameter!
            return ControllerApi.of(modifyOrders.getJobschedulerId()).resumeOrders(oIds, position);
        default: //case SUSPEND:
            //TODO missing kill signal and position!
            return ControllerApi.of(modifyOrders.getJobschedulerId()).suspendOrders(oIds);
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
}