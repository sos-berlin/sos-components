package com.sos.js7.order.initiator.classes;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.exception.SOSException;
import com.sos.jobscheduler.model.order.FreshOrder;
import com.sos.joc.Globals;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.exceptions.BulkError;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;
import com.sos.joc.exceptions.JobSchedulerNoResponseException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.model.common.Err419;
import com.sos.joc.model.order.AddOrder;
import com.sos.joc.model.order.AddOrders;
import com.sos.js7.order.initiator.OrderInitiatorSettings;
import com.sos.js7.order.initiator.OrderListSynchronizer;
import com.sos.webservices.order.initiator.model.NameValuePair;
import com.sos.webservices.order.initiator.model.Schedule;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.controller.data.ControllerCommand.AddOrdersResponse;
import js7.data.order.OrderId;
import js7.data.value.Value;
import js7.data.workflow.WorkflowPath;
import js7.proxy.javaapi.data.order.JFreshOrder;
import js7.proxy.javaapi.data.order.JOrder;
import js7.proxy.javaapi.data.order.JOrderPredicates;
import reactor.core.publisher.Flux;

public class OrderApi {

    public static void addOrders(AddOrders startOrders, String userAccount) throws JocConfigurationException, DBConnectionRefusedException,
            DBOpenSessionException, JobSchedulerConnectionResetException, JobSchedulerConnectionRefusedException, DBMissingDataException,
            DBInvalidDataException, JsonProcessingException, SOSException, URISyntaxException, ParseException, InterruptedException,
            ExecutionException, TimeoutException {
        OrderInitiatorSettings orderInitiatorSettings = new OrderInitiatorSettings();
        orderInitiatorSettings.setUserAccount(userAccount);

        orderInitiatorSettings.setTimeZone(Globals.sosCockpitProperties.getProperty("daily_plan_timezone", Globals.DEFAULT_TIMEZONE_DAILY_PLAN));
        orderInitiatorSettings.setPeriodBegin(Globals.sosCockpitProperties.getProperty("daily_plan_period_begin", Globals.DEFAULT_PERIOD_DAILY_PLAN));
        orderInitiatorSettings.setControllerId(startOrders.getControllerId());

        OrderListSynchronizer orderListSynchronizer = new OrderListSynchronizer();

        for (AddOrder startOrder : startOrders.getOrders()) {
            PlannedOrder plannedOrder = new PlannedOrder();
            plannedOrder.setControllerId(OrderInitiatorGlobals.orderInitiatorSettings.getControllerId());
            Schedule schedule = new Schedule();
            schedule.setPath(startOrder.getOrderName());
            schedule.setVariables(new ArrayList<NameValuePair>());
            schedule.setSubmitOrderToControllerWhenPlanned(true);
            schedule.setWorkflowPath(startOrder.getWorkflowPath());
            for (Entry<String, String> v : startOrder.getArguments().getAdditionalProperties().entrySet()) {
                NameValuePair nameValuePair = new NameValuePair();
                nameValuePair.setName(v.getKey());
                nameValuePair.setValue(v.getValue());
                schedule.getVariables().add(nameValuePair);
            }

            plannedOrder.setSchedule(schedule);
            FreshOrder freshOrder = new FreshOrder();
            freshOrder.setId(startOrder.getOrderName());
            Optional<Instant> scheduledFor = JobSchedulerDate.getScheduledForInUTC(startOrder.getScheduledFor(), startOrder.getTimeZone());
            scheduledFor.ifPresent(instant -> freshOrder.setScheduledFor(instant.toEpochMilli()));
            freshOrder.setWorkflowPath(startOrder.getWorkflowPath());
            freshOrder.setArguments(startOrder.getArguments());

            plannedOrder.setFreshOrder(freshOrder);
            orderListSynchronizer.add(plannedOrder);
        }

        orderListSynchronizer.addPlannedOrderToControllerAndDB(true);
    }

    private static JFreshOrder mapToFreshOrder(FreshOrder order) {
        OrderId orderId = OrderId.of(order.getId());
        Map<String, Value> arguments = new HashMap<>();
        if (order.getArguments() != null) {
            Map<String, String> a = order.getArguments().getAdditionalProperties();
            for (String key : a.keySet()) {
                arguments.put(key, Value.of(a.get(key)));
            }
        }
        Optional<Instant> scheduledFor = Optional.empty();
        if (order.getScheduledFor() != null) {
            scheduledFor = Optional.of(Instant.ofEpochMilli(order.getScheduledFor()));
        }
        return JFreshOrder.of(orderId, WorkflowPath.of(order.getWorkflowPath()), scheduledFor, arguments);
    }

    public static Set<PlannedOrder> addOrderToController(Set<PlannedOrder> orders) throws JobSchedulerConnectionResetException,
            JobSchedulerConnectionRefusedException, DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException,
            DBConnectionRefusedException, InterruptedException, ExecutionException {
        JocError jocError = new JocError();
        Function<PlannedOrder, Either<Err419, JFreshOrder>> mapper = order -> {
            Either<Err419, JFreshOrder> either = null;
            try {
                either = Either.right(mapToFreshOrder(order.getFreshOrder()));
            } catch (Exception ex) {
                either = Either.left(new BulkError().get(ex, jocError, order.getFreshOrder().getId()));
            }
            return either;
        };

        Map<Boolean, Set<Either<Err419, JFreshOrder>>> freshOrders = orders.stream().map(mapper).collect(Collectors.groupingBy(Either::isRight,
                Collectors.toSet()));

        if (freshOrders.containsKey(true) && !freshOrders.get(true).isEmpty()) {
            try {
                Either<Problem, AddOrdersResponse> response = Proxy.of(OrderInitiatorGlobals.orderInitiatorSettings.getControllerId()).addOrders(Flux
                        .fromStream(freshOrders.get(true).stream().map(Either::get))).get(99, TimeUnit.SECONDS);
                // TODO: response.getLeft() in die DB
                ProblemHelper.throwProblemIfExist(response);

            } catch (TimeoutException e) {
                throw new JobSchedulerNoResponseException(String.format("No response from controller '%s' after %ds",
                        OrderInitiatorGlobals.orderInitiatorSettings.getControllerId(), 99));
            }
        }
        return orders;
    }

    public static Set<OrderId> getNotMarkWithRemoveOrdersWhenTerminated() throws JobSchedulerConnectionResetException,
            JobSchedulerConnectionRefusedException, DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException,
            DBConnectionRefusedException, ExecutionException {
        return Proxy.of(OrderInitiatorGlobals.orderInitiatorSettings.getControllerId()).currentState().ordersBy(JOrderPredicates
                .markedAsRemoveWhenTerminated(false)).map(JOrder::id).collect(Collectors.toSet());
    }

    public static void setRemoveOrdersWhenTerminated(Set<OrderId> activeOrderIds) throws JobSchedulerConnectionResetException,
            JobSchedulerConnectionRefusedException, DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException,
            DBConnectionRefusedException, InterruptedException, ExecutionException, TimeoutException {
        ControllerApi.of(OrderInitiatorGlobals.orderInitiatorSettings.getControllerId()).removeOrdersWhenTerminated(activeOrderIds).get(99,
                TimeUnit.SECONDS);
    }
}
