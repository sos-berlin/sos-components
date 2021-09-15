package com.sos.joc.classes.order;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.auth.rest.SOSShiroFolderPermissions;
import com.sos.controller.model.order.OrderItem;
import com.sos.controller.model.order.OrderModeType;
import com.sos.controller.model.workflow.HistoricOutcome;
import com.sos.controller.model.workflow.WorkflowId;
import com.sos.inventory.model.common.Variables;
import com.sos.inventory.model.workflow.ListParameter;
import com.sos.inventory.model.workflow.ListParameters;
import com.sos.inventory.model.workflow.Parameter;
import com.sos.inventory.model.workflow.Requirements;
import com.sos.joc.Globals;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.classes.audit.JocAuditLog;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.JsonConverter;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.db.history.common.HistorySeverity;
import com.sos.joc.db.orders.DBItemDailyPlanOrders;
import com.sos.joc.db.orders.DBItemDailyPlanWithHistory;
import com.sos.joc.exceptions.BulkError;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;
import com.sos.joc.exceptions.ControllerConnectionResetException;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocFolderPermissionsException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.model.common.Err419;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.dailyplan.DailyPlanModifyOrder;
import com.sos.joc.model.dailyplan.DailyPlanOrderStateText;
import com.sos.joc.model.order.AddOrder;
import com.sos.joc.model.order.ModifyOrders;
import com.sos.joc.model.order.OrderMark;
import com.sos.joc.model.order.OrderMarkText;
import com.sos.joc.model.order.OrderState;
import com.sos.joc.model.order.OrderStateText;
import com.sos.joc.model.order.OrderV;
import com.sos.sign.model.workflow.OrderPreparation;
import com.sos.sign.model.workflow.Workflow;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.agent.AgentPath;
import js7.data.order.Order;
import js7.data.order.OrderId;
import js7.data.order.OrderMark.Cancelling;
import js7.data.order.OrderMark.Resuming;
import js7.data.order.OrderMark.Suspending;
import js7.data.value.BooleanValue;
import js7.data.value.ListValue;
import js7.data.value.NumberValue;
import js7.data.value.ObjectValue;
import js7.data.value.StringValue;
import js7.data.value.Value;
import js7.data.workflow.WorkflowPath;
import js7.data_for_java.command.JCancellationMode;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JFreshOrder;
import js7.data_for_java.order.JOrder;
import js7.data_for_java.workflow.JWorkflow;
import js7.data_for_java.workflow.position.JPosition;
import js7.proxy.javaapi.JControllerApi;
import js7.proxy.javaapi.JControllerProxy;
import reactor.core.publisher.Flux;
import scala.Option;

public class OrdersHelper {

    public static final Map<Class<? extends Order.State>, OrderStateText> groupByStateClasses = Collections.unmodifiableMap(
            new HashMap<Class<? extends Order.State>, OrderStateText>() {

                private static final long serialVersionUID = 1L;

                {
                    put(Order.Fresh$.class, OrderStateText.SCHEDULED);
                    put(Order.ExpectingNotice.class, OrderStateText.WAITING);
                    put(Order.DelayedAfterError.class, OrderStateText.WAITING);
                    put(Order.Forked.class, OrderStateText.WAITING);
                    // put(Order.Offering.class, OrderStateText.WAITING);
                    put(Order.WaitingForLock$.class, OrderStateText.WAITING);
                    put(Order.Broken.class, OrderStateText.FAILED);
                    put(Order.Failed$.class, OrderStateText.FAILED);
                    put(Order.FailedInFork$.class, OrderStateText.FAILED);
                    put(Order.FailedWhileFresh$.class, OrderStateText.FAILED);
                    put(Order.Ready$.class, OrderStateText.INPROGRESS);
                    put(Order.Processed$.class, OrderStateText.INPROGRESS);
                    put(Order.Processing$.class, OrderStateText.RUNNING);
                    put(Order.Finished$.class, OrderStateText.FINISHED);
                    put(Order.ProcessingKilled$.class, OrderStateText.CANCELLED);
                    put(Order.Cancelled$.class, OrderStateText.CANCELLED);
                    put(Order.Prompting.class, OrderStateText.PROMPTING);
                }
            });

    public static final Map<String, OrderStateText> groupByStates = Collections.unmodifiableMap(new HashMap<String, OrderStateText>() {

        private static final long serialVersionUID = 1L;

        {
            put("Planned", OrderStateText.PLANNED);
            put("Fresh", OrderStateText.SCHEDULED);
            put("Pending", OrderStateText.PENDING);
            put("Awaiting", OrderStateText.WAITING); // obsolete?
            put("DelayedAfterError", OrderStateText.WAITING);
            put("Forked", OrderStateText.WAITING);
            put("Offering", OrderStateText.WAITING); // obsolete?
            put("ExpectingNotice", OrderStateText.WAITING);
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
            put("ProcessingKilled", OrderStateText.CANCELLED);
            put("ProcessingCancelled", OrderStateText.CANCELLED); // obsolete?
            put("Blocked", OrderStateText.BLOCKED);
            put("Calling", OrderStateText.CALLING); // obsolete?
            put("Prompting", OrderStateText.PROMPTING);
        }
    });

    public static final Map<OrderStateText, Integer> severityByGroupedStates = Collections.unmodifiableMap(new HashMap<OrderStateText, Integer>() {

        // consider 'blocked' as further grouped state
        private static final long serialVersionUID = 1L;

        {
            put(OrderStateText.PLANNED, 4);
            put(OrderStateText.PENDING, 10);
            put(OrderStateText.SCHEDULED, 1);
            put(OrderStateText.WAITING, 8);
            put(OrderStateText.FAILED, 2);
            put(OrderStateText.SUSPENDED, 5);
            put(OrderStateText.CANCELLED, 2);
            put(OrderStateText.BROKEN, 2);
            put(OrderStateText.RUNNING, 0);
            put(OrderStateText.INPROGRESS, 3);
            put(OrderStateText.FINISHED, 6);
            put(OrderStateText.BLOCKED, 7);
            put(OrderStateText.CALLING, 9); // obsolete?
            put(OrderStateText.PROMPTING, 12);
            put(OrderStateText.UNKNOWN, 2);
        }
    });

    public static final Map<DailyPlanOrderStateText, Integer> severityByGroupedDailyPlanStates = Collections.unmodifiableMap(
            new HashMap<DailyPlanOrderStateText, Integer>() {

                // consider 'blocked' as further grouped state
                private static final long serialVersionUID = 1L;

                {
                    put(DailyPlanOrderStateText.PLANNED, 4);
                    put(DailyPlanOrderStateText.SUBMITTED, 5);
                    put(DailyPlanOrderStateText.FINISHED, 6);
                }
            });

    public static final Map<Class<? extends js7.data.order.OrderMark>, OrderMarkText> groupByMarkClasses = Collections.unmodifiableMap(
            new HashMap<Class<? extends js7.data.order.OrderMark>, OrderMarkText>() {

                private static final long serialVersionUID = 1L;

                {
                    put(Resuming.class, OrderMarkText.RESUMING);
                    put(Suspending.class, OrderMarkText.SUSPENDING);
                    put(Cancelling.class, OrderMarkText.CANCELLING);
                }
            });

    public static final Map<OrderMarkText, Integer> severityByMarks = Collections.unmodifiableMap(new HashMap<OrderMarkText, Integer>() {

        private static final long serialVersionUID = 1L;

        {
            put(OrderMarkText.RESUMING, 0);
            put(OrderMarkText.SUSPENDING, 5);
            put(OrderMarkText.CANCELLING, 2);
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

    public static OrderStateText getState(JOrder order) {
        Order<Order.State> o = order.asScala();
        if (o.isSuspended()) {
            return OrderStateText.SUSPENDED;
        }
        return getGroupedState(o.state().getClass());
    }

    public static boolean isSuspendedOrFailed(JOrder order) {
        Order<Order.State> o = order.asScala();
        if (o.isSuspended()) {
            return true;
        }
        return OrderStateText.FAILED.equals(getGroupedState(o.state().getClass()));
    }

    public static boolean isPendingOrScheduledOrBlocked(JOrder order) {
        Order<Order.State> o = order.asScala();
        if (o.isSuspended()) {
            return false;
        }
        return OrderStateText.SCHEDULED.equals(getGroupedState(order.asScala().state().getClass()));
    }

    public static boolean isPrompting(JOrder order) {
        Order<Order.State> o = order.asScala();
        if (o.isSuspended()) {
            return false;
        }
        return OrderStateText.PROMPTING.equals(getGroupedState(order.asScala().state().getClass()));
    }

    public static OrderState getState(String state, Boolean isSuspended) {
        OrderState oState = new OrderState();
        if (isSuspended == Boolean.TRUE) {
            state = "Suspended";
        }
        OrderStateText groupedState = getGroupedState(state);
        oState.set_text(groupedState);
        oState.setSeverity(severityByGroupedStates.get(groupedState));
        return oState;
    }

    public static OrderState getHistoryState(OrderStateText st) {
        OrderState state = new OrderState();
        state.set_text(st);
        state.setSeverity(severityByGroupedStates.get(state.get_text()));
        if (state.getSeverity() == null) {
            state.setSeverity(HistorySeverity.FAILED);
        }
        return state;
    }

    public static Integer getHistoryStateSeverity(OrderStateText st) {
        Integer severity = severityByGroupedStates.get(st);
        if (severity == null) {
            return HistorySeverity.FAILED;
        }
        return severity;
    }

    // private static OrderMark getMark(Order<Order.State> o) {
    // OrderMarkText markText = null;
    // if (o.isCancelling()) {
    // markText = OrderMarkText.CANCELLING;
    // } else if (o.isSuspending() || o.isSuspendingWithKill()) {
    // markText = OrderMarkText.SUSPENDING;
    // } else if (o.isResuming()) {
    // markText = OrderMarkText.RESUMING;
    // }
    // if (markText != null) {
    // OrderMark mark = new OrderMark();
    // mark.set_text(markText);
    // mark.setSeverity(severityByMarks.get(markText));
    // return mark;
    // }
    // return null;
    // }

    private static OrderMark getMark(Option<js7.data.order.OrderMark> opt) {
        OrderMarkText markText = null;
        if (opt.nonEmpty()) {
            markText = groupByMarkClasses.get(opt.get().getClass());
        }
        if (markText != null) {
            OrderMark mark = new OrderMark();
            mark.set_text(markText);
            mark.setSeverity(severityByMarks.get(markText));
            return mark;
        }
        return null;
    }

    public static OrderV mapJOrderToOrderV(JOrder jOrder, Boolean compact, Set<Folder> listOfFolders, Long surveyDateMillis)
            throws JsonParseException, JsonMappingException, IOException, JocFolderPermissionsException {
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
        Either<Problem, AgentPath> opt = jOrder.attached();
        if (opt.isRight()) {
            o.setAgentId(opt.get().string());
        }
        o.setPosition(oItem.getWorkflowPosition().getPosition());
        o.setPositionString(JPosition.apply(jOrder.asScala().position()).toString());
        Long scheduledFor = oItem.getScheduledFor();
        if (scheduledFor != null && surveyDateMillis != null && scheduledFor < surveyDateMillis && "Fresh".equals(oItem.getState().getTYPE())) {
            o.setState(getState("Blocked", oItem.getIsSuspended()));
        } else if (scheduledFor != null && JobSchedulerDate.NEVER_MILLIS.equals(scheduledFor) && "Fresh".equals(oItem.getState().getTYPE())) {
            o.setState(getState("Pending", oItem.getIsSuspended()));
        } else {
            o.setState(getState(oItem.getState().getTYPE(), oItem.getIsSuspended()));
        }
        if ("Prompting".equals(oItem.getState().getTYPE())) {
            o.setQuestion(((Order.Prompting) jOrder.asScala().state()).question().convertToString());
        }
        o.setMarked(getMark(jOrder.asScala().mark()));
        o.setScheduledFor(scheduledFor);
        o.setScheduledNever(JobSchedulerDate.NEVER_MILLIS.equals(scheduledFor));
        if (scheduledFor == null && surveyDateMillis != null && OrderStateText.SCHEDULED.equals(o.getState().get_text())) {
            o.setScheduledFor(surveyDateMillis);
        }
        WorkflowId wId = oItem.getWorkflowPosition().getWorkflowId();
        wId.setPath(WorkflowPaths.getPath(oItem.getWorkflowPosition().getWorkflowId()));
        if (listOfFolders != null && !canAdd(wId.getPath(), listOfFolders)) {
            throw new JocFolderPermissionsException("Access denied for folder: " + getParent(wId.getPath()));
        }
        o.setWorkflowId(wId);
        return o;
    }

    public static OrderPreparation getOrderPreparation(JOrder jOrder, JControllerState currentState) throws JsonParseException, JsonMappingException,
            IOException {
        Either<Problem, JWorkflow> eW = currentState.repo().idToWorkflow(jOrder.workflowId());
        ProblemHelper.throwProblemIfExist(eW);
        return Globals.objectMapper.readValue(eW.get().toJson(), Workflow.class).getOrderPreparation();
    }

    public static Requirements getRequirements(JOrder jOrder, JControllerState currentState) throws JsonParseException, JsonMappingException,
            IOException {
        return JsonConverter.signOrderPreparationToInvOrderPreparation(getOrderPreparation(jOrder, currentState));
    }

    @SuppressWarnings("unchecked")
    public static Variables checkArguments(Variables arguments, Requirements orderRequirements) throws JocMissingRequiredParameterException,
            JocConfigurationException {
        final Map<String, Parameter> params = (orderRequirements != null && orderRequirements.getParameters() != null) ? orderRequirements
                .getParameters().getAdditionalProperties() : Collections.emptyMap();
        Map<String, Object> args = (arguments != null) ? arguments.getAdditionalProperties() : Collections.emptyMap();

        boolean allowUndeclared = false;
        if (orderRequirements == null || orderRequirements.getAllowUndeclared() == Boolean.TRUE) {
            allowUndeclared = true;
        }

        if (!allowUndeclared) {
            Set<String> keys = args.keySet().stream().filter(arg -> !params.containsKey(arg)).collect(Collectors.toSet());
            if (!keys.isEmpty()) {
                if (keys.size() == 1) {
                    throw new JocMissingRequiredParameterException("Variable '" + keys.iterator().next() + "' isn't declared in the workflow");
                }
                throw new JocMissingRequiredParameterException("Variables '" + keys.toString() + "' aren't declared in the workflow");
            }
        }

        boolean invalid = false;
        for (Map.Entry<String, Parameter> param : params.entrySet()) {
            if (param.getValue().getFinal() != null) {
                continue;
            }
            if (param.getValue().getDefault() == null && !args.containsKey(param.getKey())) { // required
                throw new JocMissingRequiredParameterException("Variable '" + param.getKey() + "' is missing but required");
            }
            if (args.containsKey(param.getKey())) {
                Object curArg = args.get(param.getKey());
                switch (param.getValue().getType()) {
                case String:
                    if ((curArg instanceof String) == false) {
                        invalid = true;
                    } else {
                        String strArg = (String) curArg;
                        if ((strArg == null || strArg.isEmpty()) && param.getValue().getDefault() == null) {
                            throw new JocMissingRequiredParameterException("Variable '" + param.getKey() + "' is empty but required");
                        }
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
                    } else if (curArg instanceof List) {
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
                case List:
                    if ((curArg instanceof List) == false) {
                        invalid = true;
                    }
                    if (!invalid) {
                        checkListArguments((List<Map<String, Object>>) curArg, param.getValue().getListParameters(), param.getKey());
                    }
                    break;
                }
                if (invalid) {
                    throw new JocConfigurationException(String.format("Variable '%s': Wrong data type %s (%s is expected).", param.getKey(), curArg
                            .getClass().getSimpleName().replaceFirst("Array", ""), param.getValue().getType().value()));
                }
            }
        }
        return arguments;
    }
    
    private static List<Map<String, Object>> checkListArguments(List<Map<String, Object>> listVariables, ListParameters listParameters,
            String listKey) throws JocMissingRequiredParameterException, JocConfigurationException {
        boolean invalid = false;
        final Map<String, ListParameter> listParams = (listParameters != null) ? listParameters.getAdditionalProperties() : Collections.emptyMap();
        for (Map<String, Object> listVariable : listVariables) {
            Set<String> listKeys = listVariable.keySet().stream().filter(arg -> !listParams.containsKey(arg)).collect(Collectors.toSet());
            if (!listKeys.isEmpty()) {
                if (listKeys.size() == 1) {
                    throw new JocMissingRequiredParameterException("Variable '" + listKeys.iterator().next() + "' of the list variable '" + listKey
                            + "' isn't declared in the workflow");
                }
                throw new JocMissingRequiredParameterException("Variables '" + listKeys.toString() + "' of the list variable '" + listKey
                        + "' aren't declared in the workflow");
            }
            for (Map.Entry<String, ListParameter> p : listParams.entrySet()) {
                if (!listVariable.containsKey(p.getKey())) { // required
                    throw new JocMissingRequiredParameterException("Variable '" + p.getKey() + "' of list variable '" + listKey
                            + "' is missing but required");
                }

                Object curListArg = listVariable.get(p.getKey());
                switch (p.getValue().getType()) {
                case String:
                    if ((curListArg instanceof String) == false) {
                        invalid = true;
                    } else {
                        String strListArg = (String) curListArg;
                        if ((strListArg == null || strListArg.isEmpty())) {
                            throw new JocMissingRequiredParameterException("Variable '" + p.getKey() + "' of list variable '" + listKey
                                    + "' is empty but required");
                        }
                    }
                    break;
                case Boolean:
                    if ((curListArg instanceof Boolean) == false) {
                        if (curListArg instanceof String) {
                            String strArg = (String) curListArg;
                            if ("true".equals(strArg)) {
                                listVariable.put(p.getKey(), Boolean.TRUE);
                            } else if ("false".equals(strArg)) {
                                listVariable.put(p.getKey(), Boolean.FALSE);
                            } else {
                                invalid = true;
                            }
                        } else {
                            invalid = true;
                        }
                    }
                    break;
                case Number:
                    if (curListArg instanceof Boolean) {
                        invalid = true;
                    } else if (curListArg instanceof String) {
                        try {
                            BigDecimal number = new BigDecimal((String) curListArg);
                            listVariable.put(p.getKey(), number);
                        } catch (NumberFormatException e) {
                            invalid = true;
                        }
                    }
                    break;
                }
                if (invalid) {
                    throw new JocConfigurationException(String.format("Variable '%s' of list variable '%s': Wrong data type %s (%s is expected).", p
                            .getKey(), listKey, curListArg.getClass().getSimpleName(), p.getValue().getType().value()));
                }
            }
        }
        return listVariables;
    }

    public static List<Err419> cancelAndAddFreshOrder(Set<String> temporaryOrderIds, DailyPlanModifyOrder dailyplanModifyOrder, String accessToken,
            JocError jocError, Long auditlogId, SOSShiroFolderPermissions folderPermissions) throws ControllerConnectionResetException,
            ControllerConnectionRefusedException, DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException,
            DBConnectionRefusedException, ExecutionException {

        if (temporaryOrderIds.isEmpty()) {
            return Collections.emptyList();
        }
        String controllerId = dailyplanModifyOrder.getControllerId();
        JControllerProxy proxy = Proxy.of(controllerId);
        JControllerState currentState = proxy.currentState();
        Instant now = Instant.now();
        List<AuditLogDetail> auditLogDetails = new ArrayList<>();

        Function<JOrder, Either<Err419, FreshOrder>> mapper = order -> {
            Either<Err419, FreshOrder> either = null;
            try {
                Map<String, Value> args = order.arguments();
                Either<Problem, JWorkflow> e = currentState.repo().idToWorkflow(order.workflowId());
                ProblemHelper.throwProblemIfExist(e);
                String workflowPath = WorkflowPaths.getPath(e.get().id());

                // TODO order.asScala().deleteWhenTerminated() == true then ControllerApi.deleteOrdersWhenTerminated will not be necessary

                // modify parameters if necessary
                if ((dailyplanModifyOrder.getVariables() != null && !dailyplanModifyOrder.getVariables().getAdditionalProperties().isEmpty())
                        || (dailyplanModifyOrder.getRemoveVariables() != null && !dailyplanModifyOrder.getRemoveVariables().getAdditionalProperties()
                                .isEmpty())) {
                    Variables vars = scalaValuedArgumentsToVariables(args);
                    if (!folderPermissions.isPermittedForFolder(Paths.get(workflowPath).getParent().toString().replace('\\', '/'))) {
                        throw new JocFolderPermissionsException(workflowPath);
                    }
                    Workflow workflow = Globals.objectMapper.readValue(e.get().toJson(), Workflow.class);
                    if (dailyplanModifyOrder.getVariables() != null && !dailyplanModifyOrder.getVariables().getAdditionalProperties().isEmpty()) {
                        vars.getAdditionalProperties().putAll(dailyplanModifyOrder.getVariables().getAdditionalProperties());
                    }
                    if (dailyplanModifyOrder.getRemoveVariables() != null && !dailyplanModifyOrder.getRemoveVariables().getAdditionalProperties()
                            .isEmpty()) {
                        dailyplanModifyOrder.getRemoveVariables().getAdditionalProperties().forEach((k, v) -> {
                            vars.getAdditionalProperties().remove(k);
                        });
                    }
                    args = variablesToScalaValuedArguments(checkArguments(vars, JsonConverter.signOrderPreparationToInvOrderPreparation(workflow
                            .getOrderPreparation())));
                }
                // modify scheduledFor if necessary
                Optional<Instant> scheduledFor = order.scheduledFor();
                if (dailyplanModifyOrder.getScheduledFor() != null) {
                    scheduledFor = JobSchedulerDate.getScheduledForInUTC(dailyplanModifyOrder.getScheduledFor(), dailyplanModifyOrder.getTimeZone());
                }
                if (scheduledFor.isPresent() && scheduledFor.get().isBefore(now)) {
                    scheduledFor = Optional.empty();
                }

                FreshOrder o = new FreshOrder(order.id(), order.workflowId().path(), args, scheduledFor);
                // JFreshOrder o = mapToFreshOrder(order.id(), order.workflowId().path(), args, scheduledFor);
                auditLogDetails.add(new AuditLogDetail(workflowPath, order.id().string()));
                either = Either.right(o);
            } catch (Exception ex) {
                either = Either.left(new BulkError().get(ex, jocError, order.workflowId().path().string() + "/" + order.id().string()));
            }
            return either;
        };

        Map<Boolean, Set<Either<Err419, FreshOrder>>> addOrders = currentState.ordersBy(o -> temporaryOrderIds.contains(o.id().string())).map(mapper)
                .collect(Collectors.groupingBy(Either::isRight, Collectors.toSet()));

        ModifyOrders modifyOrders = new ModifyOrders();
        modifyOrders.setControllerId(controllerId);
        modifyOrders.setOrderType(OrderModeType.FRESH_ONLY);

        if (addOrders.containsKey(true) && !addOrders.get(true).isEmpty()) {
            final Map<OrderId, JFreshOrder> freshOrders = addOrders.get(true).stream().map(Either::get).collect(Collectors.toMap(
                    FreshOrder::getOldOrderId, FreshOrder::getJFreshOrderWithDeleteOrderWhenTerminated));

            proxy.api().deleteOrdersWhenTerminated(freshOrders.keySet()).thenAccept(either -> {
                ProblemHelper.postProblemEventIfExist(either, accessToken, jocError, controllerId);
                if (either.isRight()) {
                    cancelOrders(proxy.api(), modifyOrders, freshOrders.keySet()).thenAccept(either2 -> {
                        ProblemHelper.postProblemEventIfExist(either2, accessToken, jocError, controllerId);
                        if (either2.isRight()) {
                            proxy.api().addOrders(Flux.fromIterable(freshOrders.values())).thenAccept(either3 -> {
                                ProblemHelper.postProblemEventIfExist(either3, accessToken, jocError, controllerId);
                                if (either3.isRight()) {
                                    // proxy.api().deleteOrdersWhenTerminated(Flux.fromStream(freshOrders.values().stream().map(JFreshOrder::id)))
                                    // .thenAccept(either4 -> ProblemHelper.postProblemEventIfExist(either4, accessToken, jocError,
                                    // controllerId));
                                    // auditlog is written even deleteOrdersWhenTerminated has a problem
                                    storeAuditLogDetails(auditLogDetails, auditlogId).thenAccept(either5 -> ProblemHelper.postExceptionEventIfExist(
                                            either5, accessToken, jocError, controllerId));
                                }
                            });
                        }
                    });
                }
            });
        }
        if (addOrders.containsKey(false) && !addOrders.get(false).isEmpty()) {
            return addOrders.get(false).stream().map(Either::getLeft).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public static CompletableFuture<Either<Problem, Void>> cancelOrders(JControllerApi controllerApi, ModifyOrders modifyOrders,
            Collection<OrderId> oIds) {
        if (OrderModeType.FRESH_ONLY.equals(modifyOrders.getOrderType())) {
            return controllerApi.cancelOrders(oIds, JCancellationMode.freshOnly());
        } else if (modifyOrders.getKill() == Boolean.TRUE) {
            return controllerApi.cancelOrders(oIds, JCancellationMode.kill(false));
        } else {
            // JCancellationMode.freshOrStarted()
            return controllerApi.cancelOrders(oIds);
        }
    }

    public static CompletableFuture<Either<Problem, Void>> cancelOrders(ModifyOrders modifyOrders, Collection<OrderId> oIds) {
        return cancelOrders(ControllerApi.of(modifyOrders.getControllerId()), modifyOrders, oIds);
    }

    public static JFreshOrder mapToFreshOrder(AddOrder order, String yyyymmdd) {
        String uniqueId = Long.valueOf(Instant.now().toEpochMilli()).toString().substring(3);
        String orderId = String.format("#%s#T%s-%s", yyyymmdd, uniqueId, order.getOrderName());
        return mapToFreshOrder(OrderId.of(orderId), WorkflowPath.of(JocInventory.pathToName(order.getWorkflowPath())),
                variablesToScalaValuedArguments(order.getArguments()), JobSchedulerDate.getScheduledForInUTC(order.getScheduledFor(), order
                        .getTimeZone()));
    }

    private static JFreshOrder mapToFreshOrder(OrderId orderId, WorkflowPath workflowPath, Map<String, Value> args, Optional<Instant> scheduledFor) {
        return JFreshOrder.of(orderId, workflowPath, scheduledFor, args, true);
    }

    public static Map<String, Value> variablesToScalaValuedArguments(Variables vars) {
        if (vars != null) {
            return variablesToScalaValuedArguments(vars.getAdditionalProperties());
        }
        return Collections.emptyMap();
    }

    public static Map<String, Value> variablesToScalaValuedArguments(Map<String, Object> vars) {
        Map<String, Value> arguments = new HashMap<>();
        if (vars != null) {
            vars.forEach((key, val) -> {
                if (val instanceof String) {
                    arguments.put(key, StringValue.of((String) val));
                } else if (val instanceof Boolean) {
                    arguments.put(key, BooleanValue.of((Boolean) val));
                } else if (val instanceof Integer) {
                    arguments.put(key, NumberValue.of((Integer) val));
                } else if (val instanceof Long) {
                    arguments.put(key, NumberValue.of((Long) val));
                } else if (val instanceof Double) {
                    arguments.put(key, NumberValue.of(BigDecimal.valueOf((Double) val)));
                } else if (val instanceof BigDecimal) {
                    arguments.put(key, NumberValue.of(((BigDecimal) val)));
                } else if (val instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Value> valueList = ((List<Map<String, Object>>) val).stream().map(m -> ObjectValue.of(variablesToScalaValuedArguments(m)))
                            .collect(Collectors.toList());
                    arguments.put(key, ListValue.of(valueList));
                }
            });
        }
        return arguments;
    }

    // public static scala.collection.immutable.Map<String, Value> toScalaImmutableMap(Map<String, Value> jmap) {
    // return scala.collection.immutable.Map.from(scala.jdk.CollectionConverters.MapHasAsScala(jmap).asScala());
    // }

    public static Variables scalaValuedArgumentsToVariables(Map<String, Value> args) {
        Variables variables = new Variables();
        if (args != null) {
            args.forEach((k, v) -> variables.setAdditionalProperty(k, v.toJava()));
        }
        return variables;
    }

    public static CompletableFuture<Either<Exception, Void>> storeAuditLogDetails(Collection<AuditLogDetail> auditLogDetails, Long auditlogId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                JocAuditLog.storeAuditLogDetails(auditLogDetails, auditlogId);
                return Either.right(null);
            } catch (Exception e) {
                return Either.left(e);
            }
        });
    }

    public static CompletableFuture<Either<Exception, Void>> storeAuditLogDetailsFromJOrders(Collection<JOrder> jOrders, Long auditlogId) {
        return storeAuditLogDetails(jOrders.stream().map(o -> new AuditLogDetail(WorkflowPaths.getPath(o.workflowId().path().string()), o.id()
                .string())).collect(Collectors.toList()), auditlogId);
    }

    public static CompletableFuture<Either<Exception, Void>> storeAuditLogDetailsFromJOrder(JOrder jOrder, Long auditlogId) {
        return storeAuditLogDetails(Collections.singleton(new AuditLogDetail(WorkflowPaths.getPath(jOrder.workflowId().path().string()), jOrder.id()
                .string())), auditlogId);
    }

    public static boolean canAdd(String path, Set<Folder> listOfFolders) {
        if (path == null || !path.startsWith("/")) {
            return false;
        }
        return SOSShiroFolderPermissions.isPermittedForFolder(getParent(path), listOfFolders);
    }

    private static String getParent(String path) {
        Path p = Paths.get(path).getParent();
        if (p == null) {
            return null;
        } else {
            return p.toString().replace('\\', '/');
        }
    }

    public static CompletableFuture<Either<Problem, Void>> removeFromJobSchedulerController(String controllerId,
            List<DBItemDailyPlanOrders> listOfDailyPlanOrders) throws ControllerConnectionResetException, ControllerConnectionRefusedException,
            DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException,
            ExecutionException {

        Set<OrderId> setOfOrderIds = listOfDailyPlanOrders.stream().filter(dbItem -> dbItem.getSubmitted()).map(dbItem -> OrderId.of(dbItem
                .getOrderId())).collect(Collectors.toSet());

        JControllerProxy proxy = Proxy.of(controllerId);
        return proxy.api().cancelOrders(proxy.currentState().ordersBy(o -> setOfOrderIds.contains(o.id())).map(JOrder::id).collect((Collectors
                .toSet())), JCancellationMode.freshOnly());
    }

    public static CompletableFuture<Either<Problem, Void>> removeFromJobSchedulerControllerWithHistory(String controllerId,
            List<DBItemDailyPlanWithHistory> listOfPlannedOrders) {
        return ControllerApi.of(controllerId).cancelOrders(listOfPlannedOrders.stream().map(dbItem -> OrderId.of(dbItem.getOrderId())).collect(
                Collectors.toSet()), JCancellationMode.freshOnly());
    }

}
