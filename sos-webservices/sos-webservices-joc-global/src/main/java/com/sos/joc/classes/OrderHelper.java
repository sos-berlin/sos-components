package com.sos.joc.classes;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.db.orders.DBItemDailyPlanOrders;
import com.sos.joc.db.orders.DBItemDailyPlanWithHistory;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;
import com.sos.joc.exceptions.ControllerConnectionResetException;
import com.sos.joc.exceptions.ControllerNoResponseException;
import com.sos.joc.exceptions.JocConfigurationException;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.order.OrderId;
import js7.data_for_java.command.JCancellationMode;
import js7.data_for_java.order.JOrder;
import js7.data_for_java.order.JOrderPredicates;

public class OrderHelper {

    public OrderHelper() {
        super();
    }

    public static void removeFromJobSchedulerController(String controllerId, List<DBItemDailyPlanOrders> listOfPlannedOrders)
            throws JsonProcessingException, ControllerConnectionResetException, ControllerConnectionRefusedException, DBMissingDataException,
            JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException, InterruptedException,
            ExecutionException {

        try {
            Either<Problem, Void> response = ControllerApi.of(controllerId).cancelOrders(listOfPlannedOrders.stream().filter(dbItem->dbItem.getSubmitted()).map(dbItem -> OrderId.of(dbItem
                    .getOrderId())).collect(Collectors.toSet()), JCancellationMode.freshOnly()).get(99, TimeUnit.SECONDS);
            ProblemHelper.throwProblemIfExist(response);
        } catch (TimeoutException e1) {
            throw new ControllerNoResponseException(String.format("No response from controller '%s' after %ds", controllerId, 99));
        }
    }

    public static void removeFromJobSchedulerControllerWithHistory(String controllerId, List<DBItemDailyPlanWithHistory> listOfPlannedOrders)
            throws JsonProcessingException, ControllerConnectionResetException, ControllerConnectionRefusedException, DBMissingDataException,
            JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException, InterruptedException,
            ExecutionException {

        try {
            Either<Problem, Void> response = ControllerApi.of(controllerId).cancelOrders(listOfPlannedOrders.stream().map(dbItem -> OrderId.of(dbItem
                    .getOrderId())).collect(Collectors.toSet()), JCancellationMode.freshOnly()).get(99, TimeUnit.SECONDS);
            ProblemHelper.throwProblemIfExist(response);
        } catch (TimeoutException e1) {
            throw new ControllerNoResponseException(String.format("No response from controller '%s' after %ds", controllerId, 99));
        }
    }

    public static Set<JOrder> getListOfJOrdersFromController(String controllerId) throws ControllerConnectionResetException,
            ControllerConnectionRefusedException, DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException,
            DBConnectionRefusedException, ExecutionException {
        // see com.sos.joc.orders.impl.OrdersResourceImpl
        return Proxy.of(controllerId).currentState().ordersBy(JOrderPredicates.any()).collect(Collectors.toSet());
    }

}
