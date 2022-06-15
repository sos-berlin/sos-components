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
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.auth.classes.SOSAuthFolderPermissions;
import com.sos.controller.model.order.OrderCycleState;
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
import com.sos.joc.db.dailyplan.DBItemDailyPlanOrder;
import com.sos.joc.db.dailyplan.DBItemDailyPlanWithHistory;
import com.sos.joc.db.history.common.HistorySeverity;
import com.sos.joc.exceptions.BulkError;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;
import com.sos.joc.exceptions.ControllerConnectionResetException;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocBadRequestException;
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
import com.sos.joc.model.order.OrderIdMap;
import com.sos.joc.model.order.OrderMark;
import com.sos.joc.model.order.OrderMarkText;
import com.sos.joc.model.order.OrderState;
import com.sos.joc.model.order.OrderStateText;
import com.sos.joc.model.order.OrderV;
import com.sos.joc.model.order.OrderWaitingReason;
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
import js7.data.subagent.SubagentId;
import js7.data.value.BooleanValue;
import js7.data.value.ListValue;
import js7.data.value.NumberValue;
import js7.data.value.ObjectValue;
import js7.data.value.StringValue;
import js7.data.value.Value;
import js7.data.workflow.WorkflowPath;
import js7.data.workflow.instructions.ImplicitEnd;
import js7.data.workflow.position.Position;
import js7.data_for_java.command.JCancellationMode;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JFreshOrder;
import js7.data_for_java.order.JOrder;
import js7.data_for_java.order.JOrderObstacle;
import js7.data_for_java.order.JOrderPredicates;
import js7.data_for_java.workflow.JWorkflow;
import js7.data_for_java.workflow.JWorkflowId;
import js7.data_for_java.workflow.position.JPosition;
import js7.proxy.javaapi.JControllerApi;
import js7.proxy.javaapi.JControllerProxy;
import reactor.core.publisher.Flux;
import scala.Function1;
import scala.Option;

public class OrdersHelper {

    private static AtomicInteger no = new AtomicInteger(0);
    public static final int mainOrderIdLength = 25;
    public static final String mainOrderIdControllerPattern = "replaceAll(\"$js7EpochMilli\", '^.*([0-9]{9})$', '$1')";
    public static final Pattern orderIdPattern = Pattern.compile("#(\\d{4}-\\d{2}-\\d{2})#\\D(\\d{9})\\d{2}-.*");
    public static final String cyclicOrderIdRegex = "#\\d{4}-\\d{2}-\\d{2}#C[0-9]+-.*";

    public static final Map<Class<? extends Order.State>, OrderStateText> groupByStateClasses = Collections.unmodifiableMap(
            new HashMap<Class<? extends Order.State>, OrderStateText>() {

                private static final long serialVersionUID = 1L;

                {
                    put(Order.Fresh$.class, OrderStateText.SCHEDULED);
                    put(Order.ExpectingNotice.class, OrderStateText.WAITING); // only until 2.3
                    put(Order.ExpectingNotices.class, OrderStateText.WAITING);
                    put(Order.DelayedAfterError.class, OrderStateText.WAITING);
                    put(Order.Forked.class, OrderStateText.WAITING);
                    put(Order.WaitingForLock$.class, OrderStateText.WAITING);
                    put(Order.BetweenCycles.class, OrderStateText.WAITING);
                    put(Order.Broken.class, OrderStateText.FAILED);
                    put(Order.Failed$.class, OrderStateText.FAILED);
                    put(Order.FailedInFork$.class, OrderStateText.FAILED);
                    put(Order.FailedWhileFresh$.class, OrderStateText.FAILED);
                    put(Order.Ready$.class, OrderStateText.INPROGRESS);
                    put(Order.Processed$.class, OrderStateText.INPROGRESS);
                    put(Order.Processing.class, OrderStateText.RUNNING);
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
            put("DelayedAfterError", OrderStateText.WAITING);
            put("Forked", OrderStateText.WAITING);
            put("ExpectingNotice", OrderStateText.WAITING);
            put("ExpectingNotices", OrderStateText.WAITING);
            put("WaitingForLock", OrderStateText.WAITING);
            put("BetweenCycles", OrderStateText.WAITING);
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
            put("Blocked", OrderStateText.BLOCKED);
            put("Prompting", OrderStateText.PROMPTING);
        }
    });

    public static final Map<String, OrderWaitingReason> waitingReasons = Collections.unmodifiableMap(new HashMap<String, OrderWaitingReason>() {

        private static final long serialVersionUID = 1L;

        {
            put("DelayedAfterError", OrderWaitingReason.DELAYED_AFTER_ERROR);
            put("Forked", OrderWaitingReason.FORKED);
            put("ExpectingNotice", OrderWaitingReason.EXPECTING_NOTICE);
            put("ExpectingNotices", OrderWaitingReason.EXPECTING_NOTICE); // TODO introduce plural in OrderWaitingReason??
            put("WaitingForLock", OrderWaitingReason.WAITING_FOR_LOCK);
            put("BetweenCycles", OrderWaitingReason.BETWEEN_CYCLES);
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
    
    public static boolean isFresh(JOrder order) {
        Order<Order.State> o = order.asScala();
        return OrderStateText.SCHEDULED.equals(getGroupedState(o.state().getClass()));
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
        OrderStateText groupedState = getGroupedState(state);
        if (isSuspended == Boolean.TRUE && !(OrderStateText.CANCELLED.equals(groupedState) || OrderStateText.FINISHED.equals(groupedState))) {
            groupedState = OrderStateText.SUSPENDED;
        }
        oState.set_text(groupedState);
        oState.setSeverity(severityByGroupedStates.get(groupedState));
        oState.set_reason(waitingReasons.get(state));
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

    public static OrderV mapJOrderToOrderV(JOrder jOrder, OrderItem oItem, JControllerState controllerState, Boolean compact,
            Set<Folder> listOfFolders, Set<OrderId> blockedButWaitingForAdmissionOrderIds, Map<JWorkflowId, Collection<String>> finalParameters,
            Long surveyDateMillis) throws IOException, JocFolderPermissionsException {
        OrderV o = new OrderV();
        WorkflowId wId = oItem.getWorkflowPosition().getWorkflowId();
        if (finalParameters != null) {
            o.setArguments(removeFinalParameters(oItem.getArguments(), finalParameters.get(jOrder.workflowId())));
        }
        o.setAttachedState(oItem.getAttachedState());
        o.setOrderId(oItem.getId());

        List<HistoricOutcome> outcomes = oItem.getHistoricOutcomes();
        if (outcomes != null && !outcomes.isEmpty()) {
            o.setLastOutcome(outcomes.get(outcomes.size() - 1).getOutcome());
            if (compact != Boolean.TRUE) {
                o.setHistoricOutcome(outcomes);
            }
        } else {
            o.setHistoricOutcome(null);
            o.setLastOutcome(null);
        }

        Either<Problem, AgentPath> opt = jOrder.attached();
        if (opt.isRight()) {
            o.setAgentId(opt.get().string());
        }
        // o.setPosition(oItem.getWorkflowPosition().getPosition());
        // o.setPositionString(JPosition.apply(jOrder.asScala().position()).toString());
        JPosition origPos = JPosition.apply(jOrder.asScala().position());
        String jsonPos = origPos.toJson().replaceAll("\"(try|catch|cycle)\\+?[^\"]*", "\"$1");
        JPosition pos = JPosition.fromJson(jsonPos).get();
        o.setPosition(pos.toList());
        o.setPositionString(pos.toString());
        
        if (!jOrder.asScala().stopPosition().isEmpty()) {
            JPosition origStopPos = JPosition.apply(jOrder.asScala().stopPosition().get());
            String jsonStopPos = origStopPos.toJson().replaceAll("\"(try|catch|cycle)\\+?[^\"]*", "\"$1");
            JPosition stopPos = JPosition.fromJson(jsonStopPos).get();
            o.setEndPosition(stopPos.toList());
            o.setEndPositionString(stopPos.toString());
        }
        
        o.setCycleState(oItem.getState().getCycleState());
        int positionsSize = o.getPosition().size();
        if ("Processing".equals(oItem.getState().getTYPE())) {
            Option<SubagentId> subAgentId = ((Order.Processing) jOrder.asScala().state()).subagentId();
            if (subAgentId.nonEmpty()) {
                String sId = subAgentId.get().string();
                if (o.getAgentId() != null && sId.equals(o.getAgentId())) {
                   sId = sId.replaceFirst("-1$", ""); 
                }
                o.setSubagentId(sId);
            }
            if (positionsSize > 2) {
                try {
                    String lastPosition = (String) origPos.toList().get(positionsSize - 2);
                    if (lastPosition.startsWith("cycle+")) {
                        lastPosition = "{" + lastPosition.substring(6).replaceAll("(i|end|next)=", "\"$1\":").replaceFirst("i", "index").replaceFirst(
                                "next", "since") + "}";
                        o.setCycleState(Globals.objectMapper.readValue(lastPosition, OrderCycleState.class));
                    }
                } catch (Exception e) {
                    //
                }
            }
        } else if (oItem.getId().contains("|")) { // is (not running) child order
            orderIsInImplicitEnd(jOrder, controllerState).ifPresent(b -> o.setPositionIsImplicitEnd(b ? true : null));
        }
        Long scheduledFor = getScheduledForMillis(jOrder);
        if (scheduledFor != null && surveyDateMillis != null && scheduledFor < surveyDateMillis && "Fresh".equals(oItem.getState().getTYPE())) {
            if (blockedButWaitingForAdmissionOrderIds != null && blockedButWaitingForAdmissionOrderIds.contains(jOrder.id())) {
                o.setState(getState("Ready", oItem.getIsSuspended()));
            } else {
                o.setState(getState("Blocked", oItem.getIsSuspended()));
            }
        } else if (scheduledFor != null && JobSchedulerDate.NEVER_MILLIS.equals(scheduledFor) && "Fresh".equals(oItem.getState().getTYPE())) {
            o.setState(getState("Pending", oItem.getIsSuspended()));
        } else {
            o.setState(getState(oItem.getState().getTYPE(), oItem.getIsSuspended()));
        }
        if ("Prompting".equals(oItem.getState().getTYPE())) {
            o.setQuestion(((Order.Prompting) jOrder.asScala().state()).question().convertToString());
        }
        o.setMarked(getMark(jOrder.asScala().mark()));
        // o.setIsCancelable(jOrder.asScala().isCancelable() ? null : false);
        // o.setIsSuspendible(jOrder.asScala().isSuspendible() ? null : false);
        o.setScheduledFor(scheduledFor);
        o.setScheduledNever(JobSchedulerDate.NEVER_MILLIS.equals(scheduledFor));
        if (scheduledFor == null && surveyDateMillis != null && OrderStateText.SCHEDULED.equals(o.getState().get_text())) {
            o.setScheduledFor(surveyDateMillis);
        }
        wId.setPath(WorkflowPaths.getPath(oItem.getWorkflowPosition().getWorkflowId()));
        if (listOfFolders != null && !canAdd(wId.getPath(), listOfFolders)) {
            throw new JocFolderPermissionsException("Access denied for folder: " + getParent(wId.getPath()));
        }
        o.setWorkflowId(wId);
        return o;
    }

    public static OrderV mapJOrderToOrderV(JOrder jOrder, JControllerState controllerState, Boolean compact, Set<Folder> listOfFolders,
            Set<OrderId> blockedButWaitingForAdmissionOrderIds, Map<JWorkflowId, Collection<String>> finalParameters, Long surveyDateMillis)
            throws JsonParseException, JsonMappingException, IOException, JocFolderPermissionsException {
        // TODO mapping without ObjectMapper
        OrderItem oItem = Globals.objectMapper.readValue(jOrder.toJson(), OrderItem.class);
        return mapJOrderToOrderV(jOrder, oItem, controllerState, compact, listOfFolders, blockedButWaitingForAdmissionOrderIds, finalParameters,
                surveyDateMillis);
    }

    public static OrderV mapJOrderToOrderV(JOrder jOrder, Boolean compact, Map<JWorkflowId, Collection<String>> finalParameters,
            Long surveyDateMillis) throws JsonParseException, JsonMappingException, IOException, JocFolderPermissionsException {
        // TODO mapping without ObjectMapper
        OrderItem oItem = Globals.objectMapper.readValue(jOrder.toJson(), OrderItem.class);
        return mapJOrderToOrderV(jOrder, oItem, null, compact, null, null, finalParameters, surveyDateMillis);
    }

    public static OrderPreparation getOrderPreparation(JOrder jOrder, JControllerState currentState) throws JsonParseException, JsonMappingException,
            IOException {
        Either<Problem, JWorkflow> eW = currentState.repo().idToCheckedWorkflow(jOrder.workflowId());
        ProblemHelper.throwProblemIfExist(eW);
        return Globals.objectMapper.readValue(eW.get().toJson(), Workflow.class).getOrderPreparation();
    }

    public static Requirements getRequirements(JOrder jOrder, JControllerState currentState) throws JsonParseException, JsonMappingException,
            IOException {
        return JsonConverter.signOrderPreparationToInvOrderPreparation(getOrderPreparation(jOrder, currentState), false);
    }

    public static List<String> getFinalParameters(JWorkflowId jWorkflowId, JControllerState currentState) {
        try {
            Either<Problem, JWorkflow> eW = currentState.repo().idToCheckedWorkflow(jWorkflowId);
            ProblemHelper.throwProblemIfExist(eW);
            OrderPreparation op = Globals.objectMapper.readValue(eW.get().toJson(), Workflow.class).getOrderPreparation();
            if (op != null && op.getParameters() != null && op.getParameters().getAdditionalProperties() != null) {
                op.getParameters().getAdditionalProperties().forEach((k, v) -> {
                    if (v.getFinal() != null) {

                    }
                });
                return op.getParameters().getAdditionalProperties().entrySet().parallelStream().filter(e -> e.getValue().getFinal() != null).map(
                        Map.Entry::getKey).collect(Collectors.toList());
            }
            return Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public static Variables removeFinalParameters(Variables variables, Collection<String> finalParameters) {
        if (finalParameters != null && variables != null && variables.getAdditionalProperties() != null) {
            finalParameters.forEach(p -> variables.removeAdditionalProperty(p));
        }
        return variables;
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
                if (args.containsKey(param.getKey())) {
                    arguments.removeAdditionalProperty(param.getKey());
                }
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
        if (listVariables.isEmpty() && !listParams.isEmpty()) {
            Set<String> listKeys = listParams.keySet();
            if (listKeys.size() == 1) {
                throw new JocMissingRequiredParameterException("Variable '" + listKeys.iterator().next() + "' of the list variable '" + listKey
                        + "' isn't declared in the workflow");
            }
            throw new JocMissingRequiredParameterException("Variables '" + listKeys.toString() + "' of the list variable '" + listKey
                    + "' aren't declared in the workflow");
        }
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

    public static Either<List<Err419>, OrderIdMap> cancelAndAddFreshOrder(Set<String> temporaryOrderIds, DailyPlanModifyOrder dailyplanModifyOrder,
            String accessToken, JocError jocError, Long auditlogId, SOSAuthFolderPermissions folderPermissions)
            throws ControllerConnectionResetException, ControllerConnectionRefusedException, DBMissingDataException, JocConfigurationException,
            DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException, ExecutionException {

        Either<List<Err419>, OrderIdMap> result = Either.right(new OrderIdMap());
        if (temporaryOrderIds.isEmpty()) {
            return result;
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
                Either<Problem, JWorkflow> e = currentState.repo().idToCheckedWorkflow(order.workflowId());
                ProblemHelper.throwProblemIfExist(e);
                String workflowPath = WorkflowPaths.getPath(e.get().id());
                if (!folderPermissions.isPermittedForFolder(Paths.get(workflowPath).getParent().toString().replace('\\', '/'))) {
                    throw new JocFolderPermissionsException(workflowPath);
                }

                // TODO order.asScala().deleteWhenTerminated() == true then ControllerApi.deleteOrdersWhenTerminated will not be necessary

                Variables vars = scalaValuedArgumentsToVariables(args);
                Workflow workflow = Globals.objectMapper.readValue(e.get().toJson(), Workflow.class);
                if (dailyplanModifyOrder.getVariables() != null && !dailyplanModifyOrder.getVariables().getAdditionalProperties().isEmpty()) {
                    vars.setAdditionalProperties(dailyplanModifyOrder.getVariables().getAdditionalProperties());
                }
                if (dailyplanModifyOrder.getRemoveVariables() != null && !dailyplanModifyOrder.getRemoveVariables().getAdditionalProperties()
                        .isEmpty()) {
                    dailyplanModifyOrder.getRemoveVariables().getAdditionalProperties().forEach((k, v) -> {
                        vars.removeAdditionalProperty(k);
                    });
                }
                args = variablesToScalaValuedArguments(checkArguments(vars, JsonConverter.signOrderPreparationToInvOrderPreparation(workflow
                        .getOrderPreparation())));

                // modify scheduledFor if necessary
                Optional<Instant> scheduledFor = order.scheduledFor();
                if (dailyplanModifyOrder.getScheduledFor() != null) {
                    scheduledFor = JobSchedulerDate.getScheduledForInUTC(dailyplanModifyOrder.getScheduledFor(), dailyplanModifyOrder.getTimeZone());
                }
                if (!scheduledFor.isPresent()) {
                    scheduledFor = Optional.of(now);
                }
                // modify start/end positions
                Set<String> reachablePositions = CheckedAddOrdersPositions.getReachablePositions(e.get());
                Optional<JPosition> startPos = getStartPosition(dailyplanModifyOrder.getStartPosition(), dailyplanModifyOrder
                        .getStartPositionString(), reachablePositions, order.workflowPosition().position());
                Optional<JPosition> endPos = getEndPosition(dailyplanModifyOrder.getEndPosition(), dailyplanModifyOrder.getEndPositionString(),
                        reachablePositions, order.asScala().stopPosition());

                FreshOrder o = new FreshOrder(order.id(), order.workflowId().path(), args, scheduledFor, startPos, endPos);
                auditLogDetails.add(new AuditLogDetail(workflowPath, order.id().string(), controllerId));
                either = Either.right(o);
            } catch (Exception ex) {
                either = Either.left(new BulkError().get(ex, jocError, order.workflowId().path().string() + "/" + order.id().string()));
            }
            return either;
        };

        Function1<Order<Order.State>, Object> freshAndExistsFilter = JOrderPredicates.and(o -> temporaryOrderIds.contains(o.id().string()),
                JOrderPredicates.byOrderState(Order.Fresh$.class));
        Map<Boolean, Set<Either<Err419, FreshOrder>>> addOrders = currentState.ordersBy(freshAndExistsFilter).parallel().map(mapper).collect(
                Collectors.groupingBy(Either::isRight, Collectors.toSet()));

        ModifyOrders modifyOrders = new ModifyOrders();
        modifyOrders.setControllerId(controllerId);
        modifyOrders.setOrderType(OrderModeType.FRESH_ONLY);

        if (addOrders.containsKey(true) && !addOrders.get(true).isEmpty()) {
            final Map<OrderId, JFreshOrder> freshOrders = addOrders.get(true).stream().parallel().map(Either::get).collect(Collectors.toMap(
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

            OrderIdMap orderIdMap = new OrderIdMap();
            freshOrders.forEach((oldOrder, jFreshOrder) -> orderIdMap.setAdditionalProperty(oldOrder.string(), jFreshOrder.id().string()));

            result = Either.right(orderIdMap);
        } else if (addOrders.containsKey(false) && !addOrders.get(false).isEmpty()) {
            result = Either.left(addOrders.get(false).stream().parallel().map(Either::getLeft).collect(Collectors.toList()));
        }
        return result;
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

    public static String getUniqueOrderId() {
        int n = no.getAndUpdate(x -> x == Integer.MAX_VALUE ? 0 : x + 1);
        return Long.valueOf((Instant.now().toEpochMilli() * 100) + (n % 100)).toString().substring(4);
    }

    public static String generateNewFromOldOrderId(String oldOrderId, String newDailyPlanDate) {
        // #2021-10-12#C4038226057-00012-12-dailyplan_shedule_cyclic
        // #2021-10-25#C1234567890-00012-12-dailyplan_shedule_cyclic
        return generateNewFromOldOrderId(getWithNewDateFromOldOrderId(oldOrderId, newDailyPlanDate));
    }
    
    public static String getWithNewDateFromOldOrderId(String oldOrderId, String newDailyPlanDate) {
        return "#" + newDailyPlanDate + oldOrderId.substring(11);
    }

    public static String generateNewFromOldOrderId(String oldOrderId) {
        return getNewFromOldOrderId(oldOrderId, OrdersHelper.getUniqueOrderId());
    }

    public static String getNewFromOldOrderId(String oldOrderId, String newUniqueOrderIdPart) {
        // #2021-10-12#C4038226057-00012-12-dailyplan_shedule_cyclic
        // replace 4038226057 with the new part
        return oldOrderId.replaceFirst("^(#\\d{4}-\\d{2}-\\d{2}#[A-Z])\\d{10,11}(-.+)$", "$1" + newUniqueOrderIdPart + "$2");
    }

    public static JFreshOrder mapToFreshOrder(AddOrder order, String yyyymmdd, Optional<JPosition> startPos, Optional<JPosition> endPos) {
        String orderId = String.format("#%s#T%s-%s", yyyymmdd, getUniqueOrderId(), order.getOrderName());
        Optional<Instant> scheduledFor = JobSchedulerDate.getScheduledForInUTC(order.getScheduledFor(), order.getTimeZone());
        // if (!scheduledFor.isPresent()) {
        // scheduledFor = Optional.of(Instant.now());
        // }
        return mapToFreshOrder(OrderId.of(orderId), WorkflowPath.of(JocInventory.pathToName(order.getWorkflowPath())),
                variablesToScalaValuedArguments(order.getArguments()), scheduledFor, startPos, endPos);
    }

    private static JFreshOrder mapToFreshOrder(OrderId orderId, WorkflowPath workflowPath, Map<String, Value> args, Optional<Instant> scheduledFor,
            Optional<JPosition> startPos, Optional<JPosition> endPos) {
        return JFreshOrder.of(orderId, workflowPath, scheduledFor, args, true, startPos, endPos);
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

    public static CompletableFuture<Either<Exception, Void>> storeAuditLogDetailsFromJOrders(Collection<JOrder> jOrders, Long auditlogId,
            String controllerId) {
        return storeAuditLogDetails(jOrders.stream().parallel().map(o -> new AuditLogDetail(WorkflowPaths.getPath(o.workflowId().path().string()), o
                .id().string(), controllerId)).collect(Collectors.toList()), auditlogId);
    }

    public static CompletableFuture<Either<Exception, Void>> storeAuditLogDetailsFromJOrder(JOrder jOrder, Long auditlogId, String controllerId) {
        return storeAuditLogDetails(Collections.singleton(new AuditLogDetail(WorkflowPaths.getPath(jOrder.workflowId().path().string()), jOrder.id()
                .string(), controllerId)), auditlogId);
    }

    public static boolean canAdd(String path, Set<Folder> listOfFolders) {
        if (path == null || !path.startsWith("/")) {
            return false;
        }
        return SOSAuthFolderPermissions.isPermittedForFolder(getParent(path), listOfFolders);
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
            List<DBItemDailyPlanOrder> listOfDailyPlanOrders) throws ControllerConnectionResetException, ControllerConnectionRefusedException,
            DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException,
            ExecutionException {

        Set<OrderId> setOfOrderIds = listOfDailyPlanOrders.stream().parallel().filter(dbItem -> dbItem.getSubmitted()).map(dbItem -> OrderId.of(dbItem
                .getOrderId())).collect(Collectors.toSet());

        JControllerProxy proxy = Proxy.of(controllerId);
        return proxy.api().cancelOrders(proxy.currentState().ordersBy(o -> setOfOrderIds.contains(o.id())).parallel().map(JOrder::id).collect(
                (Collectors.toSet())), JCancellationMode.freshOnly());
    }

    public static CompletableFuture<Either<Problem, Void>> removeFromJobSchedulerControllerWithHistory(String controllerId,
            List<DBItemDailyPlanWithHistory> listOfPlannedOrders) {
        return ControllerApi.of(controllerId).cancelOrders(listOfPlannedOrders.stream().parallel().map(dbItem -> OrderId.of(dbItem.getOrderId()))
                .collect(Collectors.toSet()), JCancellationMode.freshOnly());
    }

    // #2021-10-12#C4038226057-00012-12-dailyplan_shedule_cyclic
    // #2021-10-12#C4038226057-
    public static String getCyclicOrderIdMainPart(String orderId) {
        return orderId.substring(0, mainOrderIdLength);
    }

    public static String getDateFromOrderId(String orderId) {
        return orderId.substring(1, 11);
    }

    public static boolean isCyclicOrderId(String orderId) {
        return orderId.matches(cyclicOrderIdRegex);
    }

    public static Set<OrderId> getWaitingForAdmissionOrderIds(Collection<OrderId> blockedOrderIds, JControllerState controllerState) {
        if (!blockedOrderIds.isEmpty()) {
            Either<Problem, Map<OrderId, Set<JOrderObstacle>>> obstaclesE = controllerState.ordersToObstacles(blockedOrderIds, controllerState
                    .instant());
            if (obstaclesE.isRight()) {
                Map<OrderId, Set<JOrderObstacle>> obstacles = obstaclesE.get();
                return obstacles.entrySet().stream().filter(e -> e.getValue().parallelStream().anyMatch(
                        ob -> (ob instanceof JOrderObstacle.WaitingForAdmission))).map(Map.Entry::getKey).collect(Collectors.toSet());
            }
        }
        return Collections.emptySet();
    }

    public static ConcurrentMap<OrderId, JOrder> getWaitingForAdmissionOrders(Collection<JOrder> blockedOrders, JControllerState controllerState) {
        Set<OrderId> ids = getWaitingForAdmissionOrderIds(blockedOrders.stream().map(JOrder::id).collect(Collectors.toSet()), controllerState);
        return blockedOrders.parallelStream().filter(o -> ids.contains(o.id())).collect(Collectors.toConcurrentMap(JOrder::id, Function.identity()));
    }

    public static Optional<Boolean> orderIsInImplicitEnd(JOrder o, JControllerState controllerState) {
        if (controllerState == null || o == null) {
            return Optional.empty();
        }
        return Optional.of(controllerState.asScala().instruction(o.asScala().workflowPosition()) instanceof ImplicitEnd);
    }

    public static Long getScheduledForMillis(String orderId, Long defaultMillis) {
        try {
            Matcher m = orderIdPattern.matcher(orderId);
            if (m.find()) {
                return ((Instant.parse(m.group(1) + "T00:00:00Z").toEpochMilli() / 1000000000L) * 1000000000L) + Long.valueOf(m.group(2));
            }
        } catch (Exception e) {
        }
        return defaultMillis;
    }

    public static Long getScheduledForMillis(JOrder order) {
        return getScheduledForMillis(order, null);
    }

    public static Long getScheduledForMillis(JOrder order, Long defaultMillis) {
        if (order.scheduledFor().isPresent()) {
            return order.scheduledFor().get().toEpochMilli();
        } else {
            return getScheduledForMillis(order.id().string(), defaultMillis);
        }
    }

    public static Long getScheduledForMillis(Order<Order.State> order, Long defaultMillis) {
        if (!order.scheduledFor().isEmpty()) {
            return order.scheduledFor().get().toEpochMilli();
        } else {
            return getScheduledForMillis(order.id().string(), defaultMillis);
        }
    }

    public static Instant getScheduledForInstant(JOrder order) {
        if (order.scheduledFor().isPresent()) {
            return order.scheduledFor().get();
        } else {
            Long millis = getScheduledForMillis(order.id().string(), null);
            if (millis != null) {
                return Instant.ofEpochMilli(millis);
            }
            return null;
        }
    }

    public static ToLongFunction<JOrder> getCompareScheduledFor(long surveyDateMillis) {
        return o -> getScheduledForMillis(o, surveyDateMillis);
    }
    
    public static List<Object> stringPositionToList(String pos) {
        if (pos == null || pos.isEmpty()) {
            return null;
        }
        String[] posArr = pos.split("/:");
        List<Object> posList = null;
        if (posArr.length == 1) {
            posList = Collections.singletonList(Integer.valueOf(posArr[0]));
        } else {
            posList = new ArrayList<>();
            for (int i = 0; i < posArr.length; i++) {
                if (i % 2 == 0) {
                    posList.add(Integer.valueOf(posArr[i]));
                } else {
                    posList.add(posArr[i]);
                }
            }
        }
        return posList;
    }
    
    public static Optional<JPosition> getStartPosition(List<Object> pos, String posString, Set<String> reachablePositions) {
        return getStartPosition(pos, posString, reachablePositions, null);
    }

    public static Optional<JPosition> getStartPosition(List<Object> pos, String posString, Set<String> reachablePositions,
            JPosition defaultPosition) {
        Optional<JPosition> posOpt = Optional.empty();
        if (defaultPosition != null && !JPosition.apply(Position.First()).equals(defaultPosition)) {
            posOpt = Optional.of(defaultPosition);
        }
        if ((pos == null || pos.isEmpty())) {
            pos = stringPositionToList(posString);
        }
        if (pos != null && !pos.isEmpty()) {
            Either<Problem, JPosition> posE = JPosition.fromList(pos);
            ProblemHelper.throwProblemIfExist(posE);
            if (!JPosition.apply(Position.First()).equals(posE.get())) {
                if (reachablePositions.contains(posE.get().toString())) {
                    return Optional.of(posE.get());
                } else {
                    throw new JocBadRequestException("Invalid start position '" + pos.toString() + "'");
                }
            }
        }
        return posOpt;
    }
    
    public static Optional<JPosition> getEndPosition(List<Object> pos, String posString, Set<String> reachablePositions) {
        return getEndPosition(pos, posString, reachablePositions, Option.empty());
    }

    public static Optional<JPosition> getEndPosition(List<Object> pos, String posString, Set<String> reachablePositions,
            Option<Position> defaultPosition) {
        Optional<JPosition> posOpt = Optional.empty();
        if (!defaultPosition.isEmpty()) {
            posOpt = Optional.of(JPosition.apply(defaultPosition.get()));
        }
        if ((pos == null || pos.isEmpty())) {
            pos = stringPositionToList(posString);
        }
        if (pos != null && !pos.isEmpty()) {
            Either<Problem, JPosition> posE = JPosition.fromList(pos);
            ProblemHelper.throwProblemIfExist(posE);
            if (reachablePositions.contains(posE.get().toString())) {
                return Optional.of(posE.get());
            } else {
                throw new JocBadRequestException("Invalid end position '" + pos.toString() + "'");
            }
        }
        return posOpt;
    }

}
