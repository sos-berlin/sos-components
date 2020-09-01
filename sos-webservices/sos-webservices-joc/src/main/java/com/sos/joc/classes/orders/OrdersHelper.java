package com.sos.joc.classes.orders;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.jobscheduler.model.order.OrderItem;
import com.sos.jobscheduler.model.workflow.HistoricOutcome;
import com.sos.joc.Globals;
import com.sos.joc.exceptions.JobSchedulerBadRequestException;
import com.sos.joc.exceptions.JobSchedulerConflictException;
import com.sos.joc.exceptions.JobSchedulerServiceUnavailableException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.order.OrderState;
import com.sos.joc.model.order.OrderStateText;
import com.sos.joc.model.order.OrderV;

import js7.base.problem.Problem;
import js7.data.order.Order;
import js7.proxy.javaapi.data.order.JOrder;

public class OrdersHelper {

    public static final Map<Class<? extends Order.State>, OrderStateText> groupByStateClasses = Collections.unmodifiableMap(
            new HashMap<Class<? extends Order.State>, OrderStateText>() {

                private static final long serialVersionUID = 1L;

                {
                    put(Order.Fresh.class, OrderStateText.PENDING);
                    put(Order.Awaiting.class, OrderStateText.WAITING);
                    put(Order.DelayedAfterError.class, OrderStateText.WAITING);
                    put(Order.Forked.class, OrderStateText.WAITING);
                    put(Order.Offering.class, OrderStateText.WAITING);
                    put(Order.Broken.class, OrderStateText.FAILED);
                    put(Order.Failed.class, OrderStateText.FAILED);
                    put(Order.FailedInFork.class, OrderStateText.FAILED);
                    put(Order.FailedWhileFresh$.class, OrderStateText.FAILED);
                    put(Order.Ready$.class, OrderStateText.RUNNING);
                    put(Order.Processed$.class, OrderStateText.RUNNING);
                    put(Order.Processing$.class, OrderStateText.RUNNING);
                    put(Order.Finished$.class, OrderStateText.FINISHED);
                    put(Order.Cancelled$.class, OrderStateText.FINISHED);
                    put(Order.ProcessingCancelled$.class, OrderStateText.FINISHED);
                }
            });

    public static final Map<String, OrderStateText> groupByStates = Collections.unmodifiableMap(new HashMap<String, OrderStateText>() {

        private static final long serialVersionUID = 1L;

        {
            put("Fresh", OrderStateText.PENDING);
            put("Awaiting", OrderStateText.WAITING);
            put("DelayedAfterError", OrderStateText.WAITING);
            put("Forked", OrderStateText.WAITING);
            put("Offering", OrderStateText.WAITING);
            put("Broken", OrderStateText.FAILED);
            put("Failed", OrderStateText.FAILED);
            put("FailedInFork", OrderStateText.FAILED);
            put("FailedWhileFresh", OrderStateText.FAILED);
            put("Ready", OrderStateText.RUNNING);
            put("Processed", OrderStateText.RUNNING);
            put("Processing", OrderStateText.RUNNING);
            put("Suspended", OrderStateText.SUSPENDED);
            put("Finished", OrderStateText.FINISHED);
            put("Cancelled", OrderStateText.FINISHED);
            put("ProcessingCancelled", OrderStateText.FINISHED);
            put("Blocked", OrderStateText.BLOCKED);
        }
    });
    
    public static final Map<OrderStateText, Integer> severityByGroupedStates = Collections.unmodifiableMap(new HashMap<OrderStateText, Integer>() {
        // consider 'blocked' as further grouped state
        private static final long serialVersionUID = 1L;

        {
            put(OrderStateText.PENDING, 1);
            put(OrderStateText.WAITING, 3);
            put(OrderStateText.FAILED, 2);
            put(OrderStateText.SUSPENDED, 2);
            put(OrderStateText.RUNNING, 0);
            put(OrderStateText.FINISHED, 0);
            put(OrderStateText.BLOCKED, 3);
            put(OrderStateText.UNKNOWN, 4);
        }
    });
    
    public static OrderStateText getGroupedState(Class<? extends Order.State> state) {
        OrderStateText groupedState = groupByStateClasses.get(state);
        if (groupedState == null) {
            return OrderStateText.UNKNOWN;
        }
        return groupedState;
    }
    
    public static OrderStateText getGroupedState(String state) {
        if (state == null) {
            return OrderStateText.UNKNOWN;
        }
        OrderStateText groupedState = groupByStates.get(state);
        if (groupedState == null) {
            return OrderStateText.UNKNOWN;
        }
        return groupedState;
    }
    
    private static OrderState getState(String state) {
        OrderState oState = new OrderState();
        OrderStateText groupedState = getGroupedState(state);
        oState.set_text(groupedState);
        oState.setSeverity(severityByGroupedStates.get(groupedState));
        return oState;
    }
    
    public static OrderV mapJOrderToOrderV(JOrder jOrder, Boolean compact, Long surveyDateMillis, boolean withDates) throws JsonParseException, JsonMappingException,
            IOException {
        OrderItem oItem = Globals.objectMapper.readValue(jOrder.toJson(), OrderItem.class);
        OrderV o = new OrderV();
        o.setArguments(oItem.getArguments());
        o.setAttachedState(oItem.getAttachedState());
        o.setOrderId(oItem.getId());
        List<HistoricOutcome> outcomes = oItem.getHistoricOutcomes();
        if (outcomes != null && !outcomes.isEmpty()) {
            o.setLastOutcome(outcomes.get(outcomes.size() - 1).getOutcome());
        }
        if (compact != Boolean.TRUE) {
            o.setHistoricOutcome(outcomes);
        }
        o.setPosition(oItem.getWorkflowPosition().getPosition());
        Long scheduledFor = oItem.getState().getScheduledFor();
        if (scheduledFor != null && scheduledFor < surveyDateMillis) {
            o.setState(getState("Blocked"));
        } else {
            o.setState(getState(oItem.getState().getTYPE()));
        }
        o.setScheduledFor(scheduledFor);
        o.setWorkflowId(oItem.getWorkflowPosition().getWorkflowId());
        if (withDates) {
            o.setSurveyDate(Date.from(Instant.ofEpochMilli(surveyDateMillis)));
            o.setDeliveryDate(Date.from(Instant.now()));
        }
        return o;
    }
    
    public static void checkResponse(Problem problem) throws JocException {
        switch (problem.httpStatusCode()) {
        case 200:
        case 201:
            break;
        case 409:
            // duplicate orders are ignored by controller -> 409 is no longer transmitted
            throw new JobSchedulerConflictException(getErrorMessage(problem));
        case 503:
            throw new JobSchedulerServiceUnavailableException(getErrorMessage(problem));
        default:
            throw new JobSchedulerBadRequestException(getErrorMessage(problem));
        }
    }

    public static String getErrorMessage(Problem problem) {
        return String.format("%s%s", (problem.codeOrNull() != null) ? problem.codeOrNull() + ": " : "", problem
                .message());
    }
    

}
