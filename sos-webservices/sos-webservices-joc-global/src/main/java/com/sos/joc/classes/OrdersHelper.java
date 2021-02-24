package com.sos.joc.classes;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
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
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.controller.model.common.Variables;
import com.sos.controller.model.order.OrderItem;
import com.sos.controller.model.order.OrderModeType;
import com.sos.controller.model.workflow.HistoricOutcome;
import com.sos.controller.model.workflow.WorkflowId;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.inventory.model.workflow.Parameter;
import com.sos.inventory.model.workflow.Requirements;
import com.sos.joc.Globals;
import com.sos.joc.classes.audit.AddOrderAudit;
import com.sos.joc.classes.audit.JocAuditLog;
import com.sos.joc.classes.audit.ModifyOrderAudit;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.history.common.HistorySeverity;
import com.sos.joc.exceptions.BulkError;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.common.Err419;
import com.sos.joc.model.dailyplan.DailyPlanModifyOrder;
import com.sos.joc.model.order.AddOrder;
import com.sos.joc.model.order.AddOrders;
import com.sos.joc.model.order.ModifyOrders;
import com.sos.joc.model.order.OrderState;
import com.sos.joc.model.order.OrderStateText;
import com.sos.joc.model.order.OrderV;
import com.sos.sign.model.workflow.Workflow;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.agent.AgentId;
import js7.data.order.Order;
import js7.data.order.OrderId;
import js7.data.value.BooleanValue;
import js7.data.value.NumberValue;
import js7.data.value.StringValue;
import js7.data.value.Value;
import js7.data.workflow.WorkflowPath;
import js7.data_for_java.command.JCancelMode;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JFreshOrder;
import js7.data_for_java.order.JOrder;
import js7.data_for_java.workflow.JWorkflow;
import js7.proxy.javaapi.JControllerApi;
import js7.proxy.javaapi.JControllerProxy;
import reactor.core.publisher.Flux;

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
        state.setSeverity(severityByGroupedStates.get(state.get_text()));
        if (state.getSeverity() == null) {
            state.setSeverity(HistorySeverity.FAILED);
        }
        return state;
    }

    public static OrderV mapJOrderToOrderV(JOrder jOrder, Boolean compact, Map<String, String> namePathMap, Long surveyDateMillis)
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
        if (scheduledFor == null && surveyDateMillis != null) {
            o.setScheduledFor(surveyDateMillis);
        }
        if (namePathMap != null) {
            WorkflowId wId = oItem.getWorkflowPosition().getWorkflowId();
            wId.setPath(namePathMap.getOrDefault(wId.getPath(), wId.getPath()));
            o.setWorkflowId(wId);
        } else {
            o.setWorkflowId(oItem.getWorkflowPosition().getWorkflowId());
        }
        return o;
    }
    
    public static Requirements getRequirements(JOrder jOrder, JControllerState currentState) throws JsonParseException, JsonMappingException,
            IOException {
        Either<Problem, JWorkflow> eW = currentState.idToWorkflow(jOrder.workflowId());
        ProblemHelper.throwProblemIfExist(eW);
        return Globals.objectMapper.readValue(eW.get().toJson(), Workflow.class).getOrderRequirements();
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

    public static List<Err419> cancelAndAddFreshOrder(Set<String> temporaryOrderIds, DailyPlanModifyOrder dailyplanModifyOrder, String accessToken,
            JocError jocError, JocAuditLog jocAuditLog) throws JobSchedulerConnectionResetException, JobSchedulerConnectionRefusedException, DBMissingDataException,
            JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException, ExecutionException {

        String controllerId = dailyplanModifyOrder.getControllerId();
        JControllerProxy proxy = Proxy.of(controllerId);
        JControllerState currentState = proxy.currentState();
        Instant now = Instant.now();
        
        // TODO consider dailyplanModifyOrder.getAuditLog()

        Function<JOrder, Either<Err419, JFreshOrder>> mapper = order -> {
            Either<Err419, JFreshOrder> either = null;
            try {
                Map<String, Value> args = order.arguments();
                // modify parameters if necessary
                if ((dailyplanModifyOrder.getVariables() != null && !dailyplanModifyOrder.getVariables().getAdditionalProperties().isEmpty())
                        || (dailyplanModifyOrder.getRemoveVariables() != null && !dailyplanModifyOrder.getRemoveVariables().getAdditionalProperties()
                                .isEmpty())) {
                    Variables vars = scalaValuedArgumentsToVariables(args);
                    Either<Problem, JWorkflow> e = currentState.idToWorkflow(order.workflowId());
                    ProblemHelper.throwProblemIfExist(e);
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
                    args = variablesToScalaValuedArguments(checkArguments(vars, workflow.getOrderRequirements()));
                }
                // modify scheduledFor if necessary
                Optional<Instant> scheduledFor = Optional.empty();
                if (!order.asScala().state().maybeDelayedUntil().isEmpty()) {
                    scheduledFor = Optional.of(order.asScala().state().maybeDelayedUntil().get().toInstant());
                }
                if (dailyplanModifyOrder.getStartTime() != null) {
                    scheduledFor = Optional.of(dailyplanModifyOrder.getStartTime().toInstant());
                }
                if (scheduledFor.isPresent() && scheduledFor.get().isBefore(now)) {
                    scheduledFor = Optional.empty();
                }

                JFreshOrder o = mapToFreshOrder(order.id(), order.workflowId().path(), args, scheduledFor);
                either = Either.right(o);
            } catch (Exception ex) {
                either = Either.left(new BulkError().get(ex, jocError, order.workflowId().path().string() + "/" + order.id().string()));
            }
            return either;
        };

        Map<Boolean, Set<Either<Err419, JFreshOrder>>> addOrders = currentState.ordersBy(o -> temporaryOrderIds.contains(o.id()
                .string())).map(mapper).collect(Collectors.groupingBy(Either::isRight, Collectors.toSet()));

        ModifyOrders modifyOrders = new ModifyOrders();
        modifyOrders.setControllerId(controllerId);
        modifyOrders.setOrderType(OrderModeType.FRESH_ONLY);
        
        if (addOrders.containsKey(true) && !addOrders.get(true).isEmpty()) {
            final Map<OrderId, JFreshOrder> freshOrders = addOrders.get(true).stream().map(Either::get).collect(Collectors.toMap(JFreshOrder::id,
                    Function.identity()));
            proxy.api().removeOrdersWhenTerminated(freshOrders.keySet()).thenAccept(either -> {
                ProblemHelper.postProblemEventIfExist(either, accessToken, jocError, controllerId);
                if (either.isRight()) {
                    cancelOrders(proxy.api(), modifyOrders, freshOrders.keySet()).thenAccept(either2 -> {
                        ProblemHelper.postProblemEventIfExist(either2, accessToken, jocError, controllerId);
                        if (either2.isRight()) {
                            proxy.api().addOrders(Flux.fromIterable(freshOrders.values())).thenAccept(either3 -> {
                                ProblemHelper.postProblemEventIfExist(either3, accessToken, jocError, controllerId);
                                if (either3.isRight()) {
                                    proxy.api().removeOrdersWhenTerminated(freshOrders.keySet()).thenAccept(either4 -> ProblemHelper
                                            .postProblemEventIfExist(either4, accessToken, jocError, controllerId));
                                    // auditlog is written even removeOrdersWhenTerminated has a problem
                                    createAuditLogFromJFreshOrders(jocAuditLog, freshOrders.values(), controllerId, dailyplanModifyOrder
                                            .getAuditLog()).thenAccept(either5 -> ProblemHelper.postExceptionEventIfExist(either5, accessToken,
                                                    jocError, controllerId));
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
        JCancelMode cancelMode = null;
        if (OrderModeType.FRESH_ONLY.equals(modifyOrders.getOrderType())) {
            cancelMode = JCancelMode.freshOnly();
        } else if (modifyOrders.getKill() == Boolean.TRUE) {
            cancelMode = JCancelMode.kill(true);
        } else {
            cancelMode = JCancelMode.kill();
        }
        return controllerApi.cancelOrders(oIds, cancelMode);
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
        return JFreshOrder.of(orderId, workflowPath, scheduledFor, args);
    }
    
    public static Map<String, Value> variablesToScalaValuedArguments(Variables vars) {
        Map<String, Value> arguments = new HashMap<>();
        if (vars != null) {
            vars.getAdditionalProperties().forEach((key, val) -> {
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
                }
            });
        }
        return arguments;
    }
    
    public static Variables scalaValuedArgumentsToVariables(Map<String, Value> args) {
        Variables variables = new Variables();
        if (args != null) {
            args.forEach((k, v) -> variables.setAdditionalProperty(k, v.toJava()));
        }
        return variables;
    }
    
    public static CompletableFuture<Either<Exception, Void>> createAuditLogFromJOrders(JocAuditLog jocAuditLog, Collection<JOrder> jOrders,
            String controllerId, ModifyOrders modifyOrders) {
        return CompletableFuture.supplyAsync(() -> {
            if (jOrders != null) {
                try {
                    final SOSHibernateSession connection = Globals.createSosHibernateStatelessConnection("storeAuditLogEntry");
                    try {
                        DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(connection);
                        final Map<String, String> nameToPath = dbLayer.getNamePathMapping(controllerId, jOrders.stream().map(o -> o.workflowId()
                                .path().string()).collect(Collectors.toSet()), DeployType.WORKFLOW.intValue());
                        for (JOrder o : jOrders) {
                            ModifyOrderAudit audit = new ModifyOrderAudit(o, controllerId, modifyOrders, nameToPath);
                            jocAuditLog.logAuditMessage(audit);
                            jocAuditLog.storeAuditLogEntry(audit, connection);
                        }
//                        jOrders.forEach(o -> {
//                            ModifyOrderAudit audit = new ModifyOrderAudit(o, controllerId, modifyOrders, nameToPath);
//                            jocAuditLog.logAuditMessage(audit);
//                            jocAuditLog.storeAuditLogEntry(audit, connection);
//                        });
                    } finally {
                        Globals.disconnect(connection);
                    }
                } catch (Exception e) {
                    return Either.left(e);
                }
            }
            return Either.right(null);
        });
    }

    public static CompletableFuture<Either<Exception, Void>> createAuditLogFromJFreshOrders(JocAuditLog jocAuditLog, Collection<JFreshOrder> jOrders,
            String controllerId, AuditParams auditParams) {
        return CompletableFuture.supplyAsync(() -> {
            if (jOrders != null) {
                try {
                    final SOSHibernateSession connection = Globals.createSosHibernateStatelessConnection("storeAuditLogEntry");
                    try {
                        DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(connection);
                        final Map<String, String> nameToPath = dbLayer.getNamePathMapping(controllerId, jOrders.stream().map(o -> o.asScala()
                                .workflowPath().string()).collect(Collectors.toSet()), DeployType.WORKFLOW.intValue());
                        for (JFreshOrder o : jOrders) {
                            ModifyOrderAudit audit = new ModifyOrderAudit(o, controllerId, auditParams, nameToPath);
                            jocAuditLog.logAuditMessage(audit);
                            jocAuditLog.storeAuditLogEntry(audit, connection);
                        }
//                        jOrders.forEach(o -> {
//                            ModifyOrderAudit audit = new ModifyOrderAudit(o, controllerId, auditParams, nameToPath);
//                            jocAuditLog.logAuditMessage(audit);
//                            jocAuditLog.storeAuditLogEntry(audit, connection);
//                        });
                    } finally {
                        Globals.disconnect(connection);
                    }
                } catch (Exception e) {
                    return Either.left(e);
                }
            }
            return Either.right(null);
        });
    }

    public static CompletableFuture<Either<Exception, Void>> createAuditLogFromJFreshOrders(JocAuditLog jocAuditLog, AddOrders addOrders) {
        return CompletableFuture.supplyAsync(() -> {
            if (addOrders != null) {
                try {
                    final SOSHibernateSession connection = Globals.createSosHibernateStatelessConnection("storeAuditLogEntry");
                    String controllerId = addOrders.getControllerId();
                    AuditParams auditParams = addOrders.getAuditLog();
                    try {
                        addOrders.getOrders().stream().filter(o -> o.getOrderName().contains("#T")).forEach(o -> {
                            AddOrderAudit audit = new AddOrderAudit(o, controllerId, auditParams);
                            jocAuditLog.logAuditMessage(audit);
                            jocAuditLog.storeAuditLogEntry(audit, connection);
                        });
                    } finally {
                        Globals.disconnect(connection);
                    }
                } catch (Exception e) {
                    return Either.left(e);
                }
            }
            return Either.right(null);
        });
    }
    
}
