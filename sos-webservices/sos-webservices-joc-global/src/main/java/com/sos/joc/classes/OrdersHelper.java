package com.sos.joc.classes;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.controller.model.common.Variables;
import com.sos.controller.model.order.OrderItem;
import com.sos.controller.model.workflow.HistoricOutcome;
import com.sos.controller.model.workflow.WorkflowId;
import com.sos.inventory.model.workflow.Requirements;
import com.sos.inventory.model.workflow.Parameter;
import com.sos.joc.Globals;
import com.sos.joc.db.history.common.HistorySeverity;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.model.order.OrderState;
import com.sos.joc.model.order.OrderStateText;
import com.sos.joc.model.order.OrderV;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.agent.AgentId;
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
                    put(Order.WaitingForLock$.class, OrderStateText.WAITING);
                    put(Order.Broken.class, OrderStateText.FAILED);
                    put(Order.Failed$.class, OrderStateText.FAILED);
                    put(Order.FailedInFork$.class, OrderStateText.FAILED);
                    put(Order.FailedWhileFresh$.class, OrderStateText.FAILED);
                    put(Order.Ready$.class, OrderStateText.INPROGRESS);
                    put(Order.Processed$.class, OrderStateText.INPROGRESS);
                    put(Order.Processing$.class, OrderStateText.RUNNING);
                    put(Order.Finished$.class, OrderStateText.FINISHED);
                    put(Order.Cancelled$.class, OrderStateText.CANCELLED);
                    put(Order.ProcessingKilled$.class, OrderStateText.CANCELLED);
                }
            });

    public static final Map<String, OrderStateText> groupByStates = Collections.unmodifiableMap(new HashMap<String, OrderStateText>() {

        private static final long serialVersionUID = 1L;

        {
            put("Planned", OrderStateText.PLANNED);
            put("Fresh", OrderStateText.PENDING);
            put("Awaiting", OrderStateText.WAITING);
            put("DelayedAfterError", OrderStateText.WAITING);
            put("Forked", OrderStateText.WAITING);
            put("Offering", OrderStateText.WAITING);
            put("WaitingForLock", OrderStateText.WAITING);
            put("Broken", OrderStateText.FAILED);
            put("Failed", OrderStateText.FAILED);
            put("FailedInFork", OrderStateText.FAILED);
            put("FailedWhileFresh", OrderStateText.FAILED);
            put("Ready", OrderStateText.INPROGRESS);
            put("Processed", OrderStateText.INPROGRESS);
            put("Processing", OrderStateText.RUNNING);
            put("Suspended", OrderStateText.SUSPENDED);
            put("Finished", OrderStateText.FINISHED);
            put("Cancelled", OrderStateText.CANCELLED);
            put("ProcessingCancelled", OrderStateText.CANCELLED);
            put("Blocked", OrderStateText.BLOCKED);
            put("Calling", OrderStateText.CALLING);
        }
    });

    public static final Map<OrderStateText, Integer> severityByGroupedStates = Collections.unmodifiableMap(new HashMap<OrderStateText, Integer>() {

        // consider 'blocked' as further grouped state
        private static final long serialVersionUID = 1L;

        {
            put(OrderStateText.PLANNED, 4);
            put(OrderStateText.PENDING, 1);
            put(OrderStateText.WAITING, 8);
            put(OrderStateText.FAILED, 2);
            put(OrderStateText.SUSPENDED, 5);
            put(OrderStateText.CANCELLED, 2);
            put(OrderStateText.BROKEN, 2);
            put(OrderStateText.RUNNING, 0);
            put(OrderStateText.INPROGRESS, 3);
            put(OrderStateText.FINISHED, 6);
            put(OrderStateText.BLOCKED, 7);
            put(OrderStateText.CALLING, 9);
            put(OrderStateText.UNKNOWN, 2);
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

    private static OrderState getState(String state, Boolean isSuspended) {
        OrderState oState = new OrderState();
        if (isSuspended == Boolean.TRUE) {
            state = "Suspended";
        }
        OrderStateText groupedState = getGroupedState(state);
        oState.set_text(groupedState);
        oState.setSeverity(severityByGroupedStates.get(groupedState));
        return oState;
    }

    public static OrderState getState(OrderStateText st) {
        OrderState state = new OrderState();
        state.set_text(st);
        state.setSeverity(OrdersHelper.severityByGroupedStates.get(state.get_text()));
        if (state.getSeverity() == null) {
            state.setSeverity(HistorySeverity.FAILED);
        }
        return state;
    }

    public static OrderV mapJOrderToOrderV(JOrder jOrder, Boolean compact, Map<String, String> namePathMap, Long surveyDateMillis, boolean withDates)
            throws JsonParseException, JsonMappingException, IOException {
        // TODO mapping without ObjectMapper
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
        Either<Problem, AgentId> opt = jOrder.attached();
        if (opt.isRight()) {
            o.setAgentId(opt.get().string());
        }
        o.setPosition(oItem.getWorkflowPosition().getPosition());
        Long scheduledFor = oItem.getState().getScheduledFor();
        if (scheduledFor != null && surveyDateMillis != null && scheduledFor < surveyDateMillis) {
            o.setState(getState("Blocked", oItem.getIsSuspended()));
        } else {
            o.setState(getState(oItem.getState().getTYPE(), oItem.getIsSuspended()));
        }
        o.setScheduledFor(scheduledFor);
        if (namePathMap != null) {
            WorkflowId wId = oItem.getWorkflowPosition().getWorkflowId();
            wId.setPath(namePathMap.getOrDefault(wId.getPath(), wId.getPath()));
            o.setWorkflowId(wId);
        } else {
            o.setWorkflowId(oItem.getWorkflowPosition().getWorkflowId());
        }
        if (withDates && surveyDateMillis != null) {
            o.setSurveyDate(Date.from(Instant.ofEpochMilli(surveyDateMillis)));
            o.setDeliveryDate(Date.from(Instant.now()));
        }
        return o;
    }

    public static Variables checkArguments(Variables arguments, Requirements orderRequirements) throws JocMissingRequiredParameterException,
            JocConfigurationException {
        final Map<String, Parameter> params = (orderRequirements != null && orderRequirements.getParameters() != null) ? orderRequirements
                .getParameters().getAdditionalProperties() : Collections.emptyMap();
        Map<String, Object> args = (arguments != null) ? arguments.getAdditionalProperties() : Collections.emptyMap();

        Set<String> keys = args.keySet().stream().filter(arg -> !params.containsKey(arg)).collect(Collectors.toSet());
        if (!keys.isEmpty()) {
            if (keys.size() == 1) {
                throw new JocMissingRequiredParameterException("Variable " + keys.iterator().next() + " isn't declared in the workflow");
            }
            throw new JocMissingRequiredParameterException("Variables " + keys.toString() + " aren't declared in the workflow");
        }
        
        boolean invalid = false;
        for (Map.Entry<String, Parameter> param : params.entrySet()) {
            if (param.getValue().getDefault() == null && !args.containsKey(param.getKey())) { // required
                throw new JocMissingRequiredParameterException("Variable '" + param.getKey() + "' is missing but required");
            }
            if (args.containsKey(param.getKey())) {
                Object curArg = args.get(param.getKey());
                switch (param.getValue().getType()) {
                case String:
                    if ((curArg instanceof String) == false) {
                        invalid = true;
                    }
                    break;
                case Boolean:
                    if ((curArg instanceof Boolean) == false) {
                        if (curArg instanceof String) {
                            String strArg = (String) curArg;
                            if ("true".equals(strArg)) {
                                arguments.setAdditionalProperty(param.getKey(), Boolean.TRUE);
                            } else if ("false".equals(strArg)) {
                                arguments.setAdditionalProperty(param.getKey(), Boolean.FALSE);
                            } else {
                                invalid = true;
                            }
                        } else {
                            invalid = true;
                        }
                    }
                    break;
                case Number:
                    if (curArg instanceof Boolean) {
                        invalid = true;
                    } else if (curArg instanceof String) {
                        try {
                            BigDecimal number = new BigDecimal((String) curArg);
                            arguments.setAdditionalProperty(param.getKey(), number);
                        } catch (NumberFormatException e) {
                            invalid = true;
                        }
                    }
                    break;
                }
                if (invalid) {
                    throw new JocConfigurationException(String.format("Variable '%s': Wrong data type %s (%s is expected).", param
                            .getKey(), curArg.getClass().getSimpleName(), param.getValue().getType().value()));
                }
            }
        }
        return arguments;
    }

}
