package com.sos.js7.order.initiator.classes;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
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
import com.sos.joc.classes.CheckJavaVariableName;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.ProblemHelper;
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
import com.sos.joc.model.order.StartOrder;
import com.sos.joc.model.order.StartOrders;
import com.sos.js7.order.initiator.OrderInitiatorRunner;
import com.sos.js7.order.initiator.OrderInitiatorSettings;
import com.sos.webservices.order.initiator.model.NameValuePair;
import com.sos.webservices.order.initiator.model.OrderTemplate;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.order.OrderId;
import js7.data.workflow.WorkflowPath;
import js7.proxy.javaapi.data.order.JFreshOrder;
import reactor.core.publisher.Flux;

public class OrderApi {

    public static void addOrders(StartOrders startOrders, String userAccount) throws JocConfigurationException, DBConnectionRefusedException,
            DBOpenSessionException, JobSchedulerConnectionResetException, JobSchedulerConnectionRefusedException, DBMissingDataException,
            DBInvalidDataException, JsonProcessingException, SOSException, URISyntaxException, ParseException, InterruptedException,
            ExecutionException {
        OrderInitiatorSettings orderInitiatorSettings = new OrderInitiatorSettings();
        orderInitiatorSettings.setUserAccount(userAccount);

        orderInitiatorSettings.setTimeZone(Globals.sosCockpitProperties.getProperty("daily_plan_timezone"));
        orderInitiatorSettings.setPeriodBegin(Globals.sosCockpitProperties.getProperty("daily_plan_period_begin"));
        orderInitiatorSettings.setControllerId(startOrders.getJobschedulerId());

        OrderInitiatorRunner orderInitiatorRunner = new OrderInitiatorRunner(orderInitiatorSettings, false);
        Set<PlannedOrder> plannedOrders = new HashSet<PlannedOrder>();
        for (StartOrder startOrder : startOrders.getOrders()) {
            PlannedOrder plannedOrder = new PlannedOrder();
            OrderTemplate orderTemplate = new OrderTemplate();
            orderTemplate.setOrderTemplatePath(startOrder.getOrderId());
            orderTemplate.setVariables(new ArrayList<NameValuePair>());
            orderTemplate.setSubmitOrderToControllerWhenPlanned(true);
            orderTemplate.setWorkflowPath(startOrder.getWorkflowPath());
            for (Entry<String, String> v : startOrder.getArguments().getAdditionalProperties().entrySet()) {
                NameValuePair nameValuePair = new NameValuePair();
                nameValuePair.setName(v.getKey());
                nameValuePair.setValue(v.getValue());
                orderTemplate.getVariables().add(nameValuePair);
            }

            plannedOrder.setOrderTemplate(orderTemplate);
            FreshOrder freshOrder = new FreshOrder();
            freshOrder.setId(startOrder.getOrderId());
            Optional<Instant> scheduledFor = JobSchedulerDate.getScheduledForInUTC(startOrder.getScheduledFor(), startOrder.getTimeZone());

            freshOrder.setScheduledFor(scheduledFor.get().getEpochSecond());
            freshOrder.setWorkflowPath(startOrder.getWorkflowPath());
            freshOrder.setArguments(startOrder.getArguments());

            plannedOrder.setFreshOrder(freshOrder);
            plannedOrders.add(plannedOrder);
        }

        // Set<PlannedOrder> result = startOrders.getOrders().stream().map(mapper).collect(java.util.stream.Collectors.toSet());

        orderInitiatorRunner.submittOrders(plannedOrders);

    }

    

    private static JFreshOrder mapToFreshOrder(PlannedOrder order) {
        OrderId orderId = OrderId.of(order.getFreshOrder().getId());
        Map<String, String> arguments = Collections.emptyMap();
        if (order.getFreshOrder().getArguments() != null) {
            arguments = order.getFreshOrder().getArguments().getAdditionalProperties();
        }
        Date d = new Date(order.getFreshOrder().getScheduledFor());
        Optional<Instant> scheduledFor = Optional.of(d.toInstant());
        return JFreshOrder.of(orderId, WorkflowPath.of(order.getFreshOrder().getWorkflowPath()), scheduledFor, arguments);
    }

    public static void addOrderToController(Map<PlannedOrderKey, PlannedOrder> listOfPlannedOrders) throws JobSchedulerConnectionResetException,
            JobSchedulerConnectionRefusedException, DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException,
            DBConnectionRefusedException, InterruptedException, ExecutionException {
        JocError jocError = new JocError();
        Function<PlannedOrder, Either<Err419, JFreshOrder>> mapper = order -> {
            Either<Err419, JFreshOrder> either = null;
            try {
                CheckJavaVariableName.test("orderId", order.getOrderTemplate().getOrderTemplatePath());
                either = Either.right(mapToFreshOrder(order));
            } catch (Exception ex) {
                either = Either.left(new BulkError().get(ex, jocError, order.getFreshOrder().getId()));
            }
            return either;
        };

        Set<PlannedOrder> orders = new HashSet<PlannedOrder>();
        for (PlannedOrder p : listOfPlannedOrders.values()) {
            if (p.isStoredInDb()) {
                orders.add(p);
            }
        }

        Map<Boolean, Set<Either<Err419, JFreshOrder>>> result = orders.stream().map(mapper).collect(Collectors.groupingBy(Either::isRight, Collectors
                .toSet()));

        if (result.containsKey(true) && !result.get(true).isEmpty()) {
            try {
                Either<Problem, Void> response = Proxy.of(OrderInitiatorGlobals.orderInitiatorSettings.getControllerId()).api().addOrders(Flux
                        .fromStream(result.get(true).stream().map(Either::get))).get(Globals.httpSocketTimeout, TimeUnit.SECONDS);
                if (response.isLeft()) {
                    ProblemHelper.checkResponse(response.getLeft());
                    //TODO: response.getLeft() in die DB
                }
            } catch (TimeoutException e) {
                throw new JobSchedulerNoResponseException(String.format("No response from controller '%s' after %ds",
                        OrderInitiatorGlobals.orderInitiatorSettings.getControllerId(), Globals.httpSocketTimeout));
            }
        }
    }

}
