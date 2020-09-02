package com.sos.joc.orders.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.jobscheduler.model.command.CancelOrder;
import com.sos.jobscheduler.model.command.Command;
import com.sos.jobscheduler.model.command.JSBatchCommands;
import com.sos.jobscheduler.model.command.KillSignal;
import com.sos.jobscheduler.model.command.ResumeOrder;
import com.sos.jobscheduler.model.command.SuspendOrder;
import com.sos.jobscheduler.model.order.Kill;
import com.sos.jobscheduler.model.order.OrderMode;
import com.sos.jobscheduler.model.order.OrderModeType;
import com.sos.jobscheduler.model.workflow.WorkflowId;
import com.sos.jobscheduler.model.workflow.WorkflowPosition;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.ModifyOrderAudit;
import com.sos.joc.classes.orders.OrdersHelper;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.exceptions.BulkError;
import com.sos.joc.exceptions.JobSchedulerInvalidResponseDataException;
import com.sos.joc.exceptions.JobSchedulerNoResponseException;
import com.sos.joc.exceptions.JobSchedulerObjectNotExistException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.model.common.Err419;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.order.ModifyOrder;
import com.sos.joc.model.order.ModifyOrders;
import com.sos.joc.orders.resource.IOrdersResourceModify;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.controller.data.ControllerCommand;
import js7.data.order.Order;
import js7.data.order.OrderId;
import js7.proxy.javaapi.JControllerProxy;
import js7.proxy.javaapi.data.common.JPredicates;
import js7.proxy.javaapi.data.controller.JControllerCommand;
import js7.proxy.javaapi.data.controller.JControllerState;
import js7.proxy.javaapi.data.order.JOrder;

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
            JsonValidator.validateFailFast(filterBytes, ModifyOrders.class);
            ModifyOrders modifyOrders = Globals.objectMapper.readValue(filterBytes, ModifyOrders.class);

            JOCDefaultResponse jocDefaultResponse = init(API_CALL + "/" + action.name().toLowerCase(), modifyOrders, accessToken, modifyOrders
                    .getJobschedulerId(), getPermissonsJocCockpit(modifyOrders.getJobschedulerId(), accessToken).getOrder().getExecute().isStart());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            checkRequiredComment(modifyOrders.getAuditLog());
            if (modifyOrders.getOrders().isEmpty()) {
                throw new JocMissingRequiredParameterException("undefined 'orders'");
            }

            final Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
            final Map<String, ModifyOrder> orderIds = modifyOrders.getOrders().stream().collect(Collectors.toMap(ModifyOrder::getOrderId, Function
                    .identity()));
            Predicate<Order<Order.State>> permissions = o -> orderIds.containsKey(o.id().string()) && canAdd(o.workflowId().path().string(),
                    permittedFolders);

            Function<JOrder, Command> mapper = jOrder -> {
                ModifyOrder mOrder = orderIds.remove(jOrder.id().string());
                WorkflowId workflowId = new WorkflowId(jOrder.workflowId().path().string(), jOrder.workflowId().versionId().string());
                ModifyOrderAudit orderAudit = new ModifyOrderAudit(mOrder, workflowId.getPath(), modifyOrders);
                logAuditMessage(orderAudit);
                storeAuditLogEntry(orderAudit);
                switch (action) {
                case CANCEL:
                    return new CancelOrder(mOrder.getOrderId(), getOrderMode(mOrder, workflowId));
                case SUSPEND:
                    return new SuspendOrder(mOrder.getOrderId(), getOrderMode(mOrder, workflowId));
                case RESUME:
                    return new ResumeOrder(mOrder.getOrderId(), getWorkflowPosition(mOrder, workflowId), mOrder.getArguments());
                default:
                    return null;
                }
            };

            JControllerProxy proxy = Proxy.of(modifyOrders.getJobschedulerId());
            JControllerState currentState = proxy.currentState();
            List<Command> commands = currentState.ordersBy(JPredicates.toScalaPredicate(permissions)).map(mapper).filter(Objects::nonNull).collect(
                    Collectors.toList());
            if (commands != null && !commands.isEmpty()) {
                Either<Problem, JControllerCommand> contollerCommand = JControllerCommand.fromJson(Globals.objectMapper.writeValueAsString(
                        new JSBatchCommands(commands)));
                if (contollerCommand.isRight()) {
                    try {
                        Either<Problem, ControllerCommand.Response> response = proxy.api().executeCommand(contollerCommand.get()).get(
                                Globals.httpSocketTimeout, TimeUnit.SECONDS);
                        if (response.isLeft()) {
                            OrdersHelper.checkResponse(response.getLeft());
                        }
                    } catch (TimeoutException e) {
                        throw new JobSchedulerNoResponseException(String.format("No response from controller '%s' after %ds", modifyOrders
                                .getJobschedulerId(), Globals.httpSocketTimeout));
                    }
                } else {
                    throw new JobSchedulerInvalidResponseDataException(OrdersHelper.getErrorMessage(contollerCommand.getLeft()));
                }
            }

            if (orderIds != null && !orderIds.isEmpty()) {
                List<Err419> bulkErrors = orderIds.keySet().stream().filter(o -> currentState.idToCheckedOrder(OrderId.of(o)).isLeft()).map(
                        o -> getBulkError(o, modifyOrders.getJobschedulerId(), getJocError())).collect(Collectors.toList());
                if (bulkErrors != null && !bulkErrors.isEmpty()) {
                    return JOCDefaultResponse.responseStatus419(bulkErrors);
                }
            }
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    private static Err419 getBulkError(String orderId, String controllerId, JocError jocError) {
        String errMsg = String.format("Order '%s' doesn't exist in controller '%s'", orderId, controllerId);
        return new BulkError().get(new JobSchedulerObjectNotExistException(errMsg), jocError, orderId);
    }

    private static WorkflowPosition getWorkflowPosition(ModifyOrder mOrder, WorkflowId workflowId) {
        WorkflowPosition pos = null;
        if (mOrder.getPosition() != null && !mOrder.getPosition().isEmpty()) {
            pos = new WorkflowPosition(workflowId, mOrder.getPosition());
        }
        return pos;
    }

    private static KillSignal getSignal(ModifyOrder mOrder) {
        KillSignal signal = null;
        if (mOrder.getSignal() != null && KillSignal.SIGKILL.equals(mOrder.getSignal())) {
            signal = KillSignal.SIGKILL;
        }
        return signal;
    }

    private static OrderMode getOrderMode(ModifyOrder mOrder, WorkflowId workflowId) {
        OrderMode orderMode = new OrderMode(OrderModeType.FRESH_OR_STARTED, null);
        KillSignal signal = getSignal(mOrder);
        WorkflowPosition pos = getWorkflowPosition(mOrder, workflowId);
        if (pos != null || signal != null) {
            orderMode.setKill(new Kill(signal, pos));
        }
        return orderMode;
    }
}