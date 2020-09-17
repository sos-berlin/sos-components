package com.sos.joc.classes;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.jobscheduler.model.command.CancelOrder;
import com.sos.jobscheduler.model.command.Command;
import com.sos.jobscheduler.model.command.JSBatchCommands;
import com.sos.joc.Globals;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.db.orders.DBItemDailyPlanOrders;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;
import com.sos.joc.exceptions.JobSchedulerInvalidResponseDataException;
import com.sos.joc.exceptions.JobSchedulerNoResponseException;
import com.sos.joc.exceptions.JocConfigurationException;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.controller.data.ControllerCommand;
import js7.proxy.javaapi.data.controller.JControllerCommand;
import js7.proxy.javaapi.data.order.JOrder;
import js7.proxy.javaapi.data.order.JOrderPredicates;

public class OrderHelper {

    public OrderHelper() {
        super();
    }

    public static void removeFromJobSchedulerController(String controllerId, List<DBItemDailyPlanOrders> listOfPlannedOrders) throws JsonProcessingException,
            JobSchedulerConnectionResetException, JobSchedulerConnectionRefusedException, DBMissingDataException, JocConfigurationException,
            DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException, InterruptedException, ExecutionException {

        List<Command> commands = listOfPlannedOrders.stream().map(dbItem -> {
            CancelOrder cancelOrder = new CancelOrder();
            cancelOrder.setOrderId(dbItem.getOrderKey());
            return cancelOrder;
        }).collect(Collectors.toList());

        Either<Problem, JControllerCommand> controllerCommand = JControllerCommand.fromJson(Globals.objectMapper.writeValueAsString(
                new JSBatchCommands(commands)));
        if (controllerCommand.isRight()) {
            try {
                Either<Problem, ControllerCommand.Response> response = ControllerApi.of(controllerId).executeCommand(controllerCommand.get()).get(
                        99, TimeUnit.SECONDS);
                ProblemHelper.throwProblemIfExist(response);
            } catch (TimeoutException e) {
                throw new JobSchedulerNoResponseException(String.format("No response from controller '%s' after %ds", controllerId,
                        Globals.httpSocketTimeout));
            }
        } else {
            String errMsg = ProblemHelper.getErrorMessage(controllerCommand.getLeft());
            throw new JobSchedulerInvalidResponseDataException(errMsg);
        }
    }

    public static Set<JOrder> getListOfJOrdersFromController(String controllerId) throws JobSchedulerConnectionResetException,
            JobSchedulerConnectionRefusedException, DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException,
            DBConnectionRefusedException, ExecutionException {
        // see com.sos.joc.orders.impl.OrdersResourceImpl
        return Proxy.of(controllerId).currentState().ordersBy(JOrderPredicates.any()).collect(Collectors.toSet());
    }

}
