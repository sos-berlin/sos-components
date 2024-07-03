package com.sos.joc.classes.order;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import java.util.stream.Stream;

import org.apache.pekko.util.OptionConverters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.auth.classes.SOSAuthFolderPermissions;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.controller.model.order.ExpectedNotice;
import com.sos.controller.model.order.OrderCycleState;
import com.sos.controller.model.order.OrderItem;
import com.sos.controller.model.order.OrderModeType;
import com.sos.controller.model.order.OrderRetryState;
import com.sos.controller.model.workflow.HistoricOutcome;
import com.sos.controller.model.workflow.WorkflowId;
import com.sos.inventory.model.common.Variables;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.inventory.model.workflow.ListParameter;
import com.sos.inventory.model.workflow.ListParameterType;
import com.sos.inventory.model.workflow.ListParameters;
import com.sos.inventory.model.workflow.Parameter;
import com.sos.inventory.model.workflow.Requirements;
import com.sos.joc.Globals;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.audit.AuditLogDetail;
import com.sos.joc.classes.audit.JocAuditLog;
import com.sos.joc.classes.common.StringSizeSanitizer;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.JsonConverter;
import com.sos.joc.classes.inventory.Validator;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.settings.ClusterSettings;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.cluster.configuration.globals.common.ConfigurationEntry;
import com.sos.joc.db.dailyplan.DBItemDailyPlanOrder;
import com.sos.joc.db.dailyplan.DBItemDailyPlanWithHistory;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.deploy.items.DeployedContent;
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
import com.sos.joc.model.order.BlockPosition;
import com.sos.joc.model.order.ModifyOrders;
import com.sos.joc.model.order.Obstacle;
import com.sos.joc.model.order.ObstacleType;
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
import js7.data.item.VersionedItemId;
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
import js7.data.workflow.position.Label;
import js7.data.workflow.position.Position;
import js7.data.workflow.position.PositionOrLabel;
import js7.data.workflow.position.WorkflowPosition;
import js7.data_for_java.command.JCancellationMode;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JFreshOrder;
import js7.data_for_java.order.JOrder;
import js7.data_for_java.order.JOrderObstacle;
import js7.data_for_java.order.JOrderPredicates;
import js7.data_for_java.workflow.JWorkflow;
import js7.data_for_java.workflow.JWorkflowId;
import js7.data_for_java.workflow.position.JBranchPath;
import js7.data_for_java.workflow.position.JPosition;
import js7.data_for_java.workflow.position.JPositionOrLabel;
import js7.proxy.javaapi.JControllerApi;
import js7.proxy.javaapi.JControllerProxy;
import reactor.core.publisher.Flux;
import scala.Function1;
import scala.Option;
import scala.collection.JavaConverters;

public class OrdersHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrdersHelper.class);
    private static AtomicInteger no = new AtomicInteger(0);
    public static final int mainOrderIdLength = 25;
    public static final String mainOrderIdControllerPattern = "replaceAll(\"$js7EpochMilli\", '^.*([0-9]{9})$', '$1')";
    public static final Pattern orderIdPattern = Pattern.compile("#(\\d{4}-\\d{2}-\\d{2})#\\D(\\d)(\\d{8})\\d{2}-.*");
    public static final String cyclicOrderIdRegex = "#\\d{4}-\\d{2}-\\d{2}#C[0-9]+-.*";
    private static final DateTimeFormatter datetimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());

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
                    put(Order.Ready$.class, OrderStateText.WAITING);
                    put(Order.Broken.class, OrderStateText.FAILED);
                    put(Order.Failed$.class, OrderStateText.FAILED);
                    put(Order.FailedInFork$.class, OrderStateText.FAILED);
                    put(Order.FailedWhileFresh$.class, OrderStateText.FAILED);
                    put(Order.Stopped$.class, OrderStateText.FAILED);
                    put(Order.StoppedWhileFresh$.class, OrderStateText.FAILED);
                    put(Order.Processed$.class, OrderStateText.INPROGRESS);
                    put(Order.Processing.class, OrderStateText.RUNNING);
                    put(Order.Finished$.class, OrderStateText.FINISHED);
                    put(Order.ProcessingKilled$.class, OrderStateText.FAILED);
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
            put("Ready", OrderStateText.WAITING);
            put("Broken", OrderStateText.FAILED);
            put("Failed", OrderStateText.FAILED);
            put("FailedInFork", OrderStateText.FAILED);
            put("FailedWhileFresh", OrderStateText.FAILED);
            put("Stopped", OrderStateText.FAILED);
            put("StoppedWhileFresh", OrderStateText.FAILED);
            put("Processed", OrderStateText.INPROGRESS);
            put("Processing", OrderStateText.RUNNING);
            put("Suspended", OrderStateText.SUSPENDED);
            put("Finished", OrderStateText.FINISHED);
            put("Cancelled", OrderStateText.CANCELLED);
            put("ProcessingKilled", OrderStateText.FAILED);
            put("Blocked", OrderStateText.BLOCKED);
            put("Prompting", OrderStateText.PROMPTING);
        }
    });

    public static final Map<String, OrderWaitingReason> waitingReasons = Collections.unmodifiableMap(new HashMap<String, OrderWaitingReason>() {

        private static final long serialVersionUID = 1L;

        {
            put("DelayedAfterError", OrderWaitingReason.DELAYED_AFTER_ERROR);
            put("Forked", OrderWaitingReason.FORKED);
            put("ExpectingNotice", OrderWaitingReason.EXPECTING_NOTICES);
            put("ExpectingNotices", OrderWaitingReason.EXPECTING_NOTICES); // TODO introduce plural in OrderWaitingReason??
            put("WaitingForLock", OrderWaitingReason.WAITING_FOR_LOCK);
            put("BetweenCycles", OrderWaitingReason.BETWEEN_CYCLES);
            put("WaitingForAdmission", OrderWaitingReason.WAITING_FOR_ADMISSION);
            put("JobParallelismLimitReached", OrderWaitingReason.JOB_PROCESS_LIMIT_REACHED);
            put("JobProcessLimitReached", OrderWaitingReason.JOB_PROCESS_LIMIT_REACHED);
            put("AgentProcessLimitReached", OrderWaitingReason.AGENT_PROCESS_LIMIT_REACHED);
            put("WorkflowIsSuspended", OrderWaitingReason.WORKFLOW_IS_SUSPENDED);
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
    
    public static boolean isSuspendible(JOrder order) {
        Order<Order.State> o = order.asScala();
        //return o.isSuspendible();
        if (o.isSuspended() || isTerminated(o) || isFailed(o)) {
            return false;
        }
        return true;
    }
    
    public static boolean isResumable(JOrder order) {
        Order<Order.State> o = order.asScala();
        //return o.isResumable();
        return o.isSuspended() || isFailed(o);// || isSuspending(o.mark());
    }
    
    private static boolean isTerminated(Order<Order.State> o) {
        return ((o.state() instanceof Order.Finished$) || (o.state() instanceof Order.Cancelled$));
    }
    
    private static boolean isFailed(Order<Order.State> o) {
        return OrderStateText.FAILED.equals(getGroupedState(o.state().getClass()));
    }
    
//    private static boolean isSuspending(Option<js7.data.order.OrderMark> opt) {
//        if (opt.nonEmpty()) {
//            return (opt.get() instanceof Suspending);
//        }
//        return false;
//    }
    
    public static boolean isNotFailed(JOrder order) {
        return !OrderStateText.FAILED.equals(getGroupedState(order.asScala().state().getClass()));
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

    public static OrderState getState(String state, Boolean isSuspended, ObstacleType obstacle) {
        OrderState oState = new OrderState();
        OrderStateText groupedState = getGroupedState(state);
        if (isSuspended == Boolean.TRUE && !(OrderStateText.CANCELLED.equals(groupedState) || OrderStateText.FINISHED.equals(groupedState))) {
            groupedState = OrderStateText.SUSPENDED;
        } else {
            oState.set_reason(obstacle != null ? waitingReasons.get(obstacle.value()) : waitingReasons.get(state));
        }
        oState.set_text(groupedState);
        oState.setSeverity(severityByGroupedStates.get(groupedState));
        return oState;
    }
    
    public static OrderState getState(String state, Boolean isSuspended) {
        return getState(state, isSuspended, null);
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
            Long surveyDateMillis, ZoneId zoneId) throws IOException, JocFolderPermissionsException {
        OrderV o = new OrderV();
        WorkflowId wId = oItem.getWorkflowPosition().getWorkflowId();
        if (finalParameters != null) {
            o.setArguments(removeFinalParameters(oItem.getArguments(), finalParameters.get(jOrder.workflowId())));
        }
        o.setAttachedState(oItem.getAttachedState());
        o.setOrderId(oItem.getId());
        o.setHasChildOrders(null);
        boolean isChildOrder = oItem.getId().contains("|");

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
        
        o.setEndPositions(oItem.getStopPositions());
        o.setCycleState(oItem.getState().getCycleState());
        o.setExpectedNotices(getStillExpectedNotices(jOrder.id(), oItem, controllerState));
        int positionsSize = o.getPosition().size();
        if ("DelayedAfterError".equals(oItem.getState().getTYPE())) {
            OrderRetryState rs = new OrderRetryState();
            rs.setNext(oItem.getState().getUntil());
            if (positionsSize > 2) {
                try {
                    String lastPosition = (String) origPos.toList().get(positionsSize - 2);
                    if (lastPosition.startsWith("try+")) {
                        rs.setAttempt(Integer.valueOf(lastPosition.substring(4)) + 1);
                    }
                } catch (Exception e) {
                    //
                }
            }
            o.setRetryState(rs);
            if (outcomes != null && outcomes.size() > 1) { // ignore last outcome from catch instruction; always succeeded
                o.setLastOutcome(outcomes.get(outcomes.size() - 2).getOutcome());
            }
        }
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
        } else if (isChildOrder) { // is (not running) child order
            orderIsInImplicitEnd(jOrder, controllerState).ifPresent(b -> o.setPositionIsImplicitEnd(b ? true : null));
        }
        Long scheduledFor = getScheduledForMillis(jOrder, zoneId);
//        if ("Fresh".equals(oItem.getState().getTYPE()) || "Ready".equals(oItem.getState().getTYPE())) {
//            Optional<Obstacle> obstacleOpt = getObstacle(OrderId.of(oItem.getId()), controllerState);
//            if (obstacleOpt.isPresent()) {
//                o.setState(getState("Ready", oItem.getIsSuspended(), obstacleOpt.get().getType()));
//            }
//        } else 
        if (scheduledFor != null && surveyDateMillis != null && scheduledFor < surveyDateMillis && "Fresh".equals(oItem.getState().getTYPE())) {
            if (blockedButWaitingForAdmissionOrderIds != null && blockedButWaitingForAdmissionOrderIds.contains(jOrder.id())) {
                //o.setState(getState("Ready", oItem.getIsSuspended(), ObstacleType.WaitingForAdmission));
                o.setState(getState("Processed", oItem.getIsSuspended(), ObstacleType.WaitingForAdmission));
            } else {
                Optional<Obstacle> obstacleOpt = getObstacle(OrderId.of(oItem.getId()), controllerState);
                if (obstacleOpt.isPresent()) {
                    o.setState(getState("Blocked", oItem.getIsSuspended(), obstacleOpt.get().getType()));
                } else {
                    o.setState(getState("Blocked", oItem.getIsSuspended()));
                }
            }
        } else if (scheduledFor != null && JobSchedulerDate.NEVER_MILLIS.equals(scheduledFor) && "Fresh".equals(oItem.getState().getTYPE())) {
            o.setState(getState("Pending", oItem.getIsSuspended()));
        } else if ("Ready".equals(oItem.getState().getTYPE())) {
            Optional<Obstacle> obstacleOpt = getObstacle(OrderId.of(oItem.getId()), controllerState);
            if (obstacleOpt.isPresent()) {
                o.setState(getState("Ready", oItem.getIsSuspended(), obstacleOpt.get().getType()));
            } else {
                o.setState(getState("Ready", oItem.getIsSuspended()));
            }
        } else {
            o.setState(getState(oItem.getState().getTYPE(), oItem.getIsSuspended()));
        }
        if ("Prompting".equals(oItem.getState().getTYPE())) {
            o.setQuestion(((Order.Prompting) jOrder.asScala().state()).question().convertToString());
        }
        // completed order
        if (OrderStateText.FINISHED.equals(o.getState().get_text()) || OrderStateText.CANCELLED.equals(o.getState().get_text())) {
            o.setCanLeave(!o.getOrderId().matches(".*#F[0-9]+-.*") && !isChildOrder); // true if not file order and not child order
            if (isChildOrder && OrderStateText.CANCELLED.equals(o.getState().get_text())) {
                // force canceled child order to position of branch's implicit end
                try {
                    pos = moveChildOrderPosToImplicitEnd(pos, jOrder, controllerState);
                    o.setPosition(pos.toList());
                    o.setPositionString(pos.toString());
                    o.setPositionIsImplicitEnd(true);
                } catch (Exception e) {
                    //
                }
            }
        }
        o.setMarked(getMark(jOrder.asScala().mark()));
        // o.setIsCancelable(jOrder.asScala().isCancelable() ? true : null);
        o.setIsSuspendible(isSuspendible(jOrder) ? true : null);
        o.setIsResumable(isResumable(jOrder) ? true : null);
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
        // only label of a job instruction is available
        orderPositionToLabel(jOrder, controllerState).ifPresent(l -> o.setLabel(l));
        return o;
    }
    
    private static JPosition moveChildOrderPosToImplicitEnd(JPosition pos, JOrder o, JControllerState controllerState) {
        JPosition p = pos;
        if (controllerState != null && o != null && o.id().string().contains("|")) { // is childOrder
            VersionedItemId<WorkflowPath> wId = o.asScala().workflowPosition().workflowId();
            if (!orderIsInImplicitEnd(o.asScala().workflowPosition(), controllerState)) {
                List<Object> lpos = pos.toList();
                int indexOfParentFork = lpos.size();
                boolean parentForkFound = false;
                for (int j = lpos.size() - 2; j > 0; j -= 2) {
                    if (lpos.get(j).toString().startsWith("fork")) {
                        indexOfParentFork = j;
                        parentForkFound = true;
                        break;
                    }
                }
                if (parentForkFound) {
                    List<Object> sublpos = new ArrayList<>(lpos.subList(0, indexOfParentFork + 2));
                    Integer lastpos = (Integer) lpos.get(indexOfParentFork + 1);
                    int limit = lastpos + 1000; // avoiding endless loop
                    do {
                        lastpos++;
                        sublpos.set(sublpos.size() - 1, lastpos);
                        p = JPosition.fromList(sublpos).get();
                    } while (lastpos < limit && !orderIsInImplicitEnd(WorkflowPosition.apply(wId, p.asScala()), controllerState));
                }
            }
        }
        return p;
    }
    
    private static Optional<String> orderPositionToLabel(JOrder order, JControllerState controllerState) {
        if (controllerState == null || order == null) {
            return Optional.empty();
        }
        return OptionConverters.toJava(controllerState.asScala().workflowPositionToLabel(order.asScala().workflowPosition()).map(
                OptionConverters::toJava).toOption()).orElse(Optional.empty()).map(Label::string);
    }
    
    private static List<ExpectedNotice> getStillExpectedNotices(OrderId orderId, OrderItem oItem, JControllerState controllerState) {
        if ("ExpectingNotices".equals(oItem.getState().getTYPE())) {
            if (controllerState != null) {
                return controllerState.orderToStillExpectedNotices(orderId).stream().map(n -> new ExpectedNotice(n.boardPath().string(), n.noticeId()
                        .string())).collect(Collectors.toList());
            } else {
                return oItem.getState().getExpected();
            }
        }
        return null;
    }

    public static OrderV mapJOrderToOrderV(JOrder jOrder, JControllerState controllerState, Boolean compact, Set<Folder> listOfFolders,
            Set<OrderId> blockedButWaitingForAdmissionOrderIds, Map<JWorkflowId, Collection<String>> finalParameters, Long surveyDateMillis,
            ZoneId zoneId) throws JsonParseException, JsonMappingException, IOException, JocFolderPermissionsException {
        // TODO mapping without ObjectMapper
        OrderItem oItem = Globals.objectMapper.readValue(jOrder.toJson(), OrderItem.class);
        return mapJOrderToOrderV(jOrder, oItem, controllerState, compact, listOfFolders, blockedButWaitingForAdmissionOrderIds, finalParameters,
                surveyDateMillis, zoneId);
    }

    public static OrderV mapJOrderToOrderV(JOrder jOrder, Boolean compact, Map<JWorkflowId, Collection<String>> finalParameters,
            Long surveyDateMillis, ZoneId zoneId) throws JsonParseException, JsonMappingException, IOException, JocFolderPermissionsException {
        // TODO mapping without ObjectMapper
        OrderItem oItem = Globals.objectMapper.readValue(jOrder.toJson(), OrderItem.class);
        return mapJOrderToOrderV(jOrder, oItem, null, compact, null, null, finalParameters, surveyDateMillis, zoneId);
    }

    public static OrderPreparation getOrderPreparation(JOrder jOrder, JControllerState currentState) throws JsonParseException, JsonMappingException,
            IOException {
        Either<Problem, JWorkflow> eW = currentState.repo().idToCheckedWorkflow(jOrder.workflowId());
        ProblemHelper.throwProblemIfExist(eW);
        return Globals.objectMapper.readValue(eW.get().toJson(), Workflow.class).getOrderPreparation();
    }
    
    public static Requirements getRequirements(JWorkflowId jWorkflowId, String controllerId, DeployedConfigurationDBLayer dbLayer)
            throws JsonMappingException, JsonProcessingException {
        return getRequirements(jWorkflowId, controllerId, dbLayer, false);
    }

    public static Requirements getRequirements(JWorkflowId jWorkflowId, String controllerId, DeployedConfigurationDBLayer dbLayer, boolean withFinals)
            throws JsonMappingException, JsonProcessingException {
        return getRequirements(jWorkflowId.path().string(), jWorkflowId.versionId().string(), controllerId, dbLayer, withFinals);
    }
    
    public static Requirements getRequirements(JOrder jOrder, String controllerId, DeployedConfigurationDBLayer dbLayer) throws JsonMappingException,
            JsonProcessingException {
        return getRequirements(jOrder.workflowId(), controllerId, dbLayer, false);
    }
    
    public static Requirements getRequirements(JOrder jOrder, String controllerId, DeployedConfigurationDBLayer dbLayer, boolean withFinals)
            throws JsonMappingException, JsonProcessingException {
        return getRequirements(jOrder.workflowId(), controllerId, dbLayer, withFinals);
    }
    
    private static Requirements getRequirements(String workflowName, String controllerId) throws JsonParseException, JsonMappingException, IOException {
        SOSHibernateSession connection = null;
        try {
            connection = Globals.createSosHibernateStatelessConnection("getOrderPreparation");
            return getRequirements(workflowName, null, controllerId, new DeployedConfigurationDBLayer(connection), true);
        } finally {
            Globals.disconnect(connection);
        }
    }
    
    private static Requirements getRequirements(String workflowName, String versionId, String controllerId, DeployedConfigurationDBLayer dbLayer,
            boolean withFinals) throws JsonMappingException, JsonProcessingException {
        DeployedContent lastContent = dbLayer.getDeployedInventory(controllerId, DeployType.WORKFLOW.intValue(), workflowName, versionId);
        Requirements orderPreparation = Globals.objectMapper.readValue(lastContent.getContent(), com.sos.inventory.model.workflow.Workflow.class)
                .getOrderPreparation();
        if (!withFinals && orderPreparation != null && orderPreparation.getParameters() != null && orderPreparation.getParameters()
                .getAdditionalProperties() != null) {
            Set<String> finalParameters = orderPreparation.getParameters().getAdditionalProperties().entrySet().stream().filter(e -> e.getValue()
                    .getFinal() != null).map(Map.Entry::getKey).collect(Collectors.toSet());
            finalParameters.forEach(k -> orderPreparation.getParameters().removeAdditionalProperty(k));
        }
        return orderPreparation;
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
    
    public static Variables checkArgumentsWithAllowedDollarInValues(Variables arguments, Requirements orderRequirements)
            throws JocMissingRequiredParameterException, JocConfigurationException {
        return checkArguments(arguments, orderRequirements, ClusterSettings.getAllowEmptyArguments(Globals.getConfigurationGlobalsJoc()), true);
    }

    public static Variables checkArguments(Variables arguments, Requirements orderRequirements) throws JocMissingRequiredParameterException,
            JocConfigurationException {
        return checkArguments(arguments, orderRequirements, ClusterSettings.getAllowEmptyArguments(Globals.getConfigurationGlobalsJoc()), false);
    }

    public static Variables checkArguments(Variables arguments, Requirements orderRequirements, boolean allowEmptyValues)
            throws JocMissingRequiredParameterException, JocConfigurationException {
        return checkArguments(arguments, orderRequirements, allowEmptyValues, false);
    }

    @SuppressWarnings("unchecked")
    public static Variables checkArguments(Variables arguments, Requirements orderRequirements, boolean allowEmptyValues, boolean allowDollarInValue)
            throws JocMissingRequiredParameterException, JocConfigurationException {
        final Map<String, Parameter> params = (orderRequirements != null && orderRequirements.getParameters() != null) ? orderRequirements
                .getParameters().getAdditionalProperties() : Collections.emptyMap();
        // clone arguments
        Variables vars = new Variables();
        if (arguments != null) {
            vars.setAdditionalProperties(arguments.getAdditionalProperties());
        }
        Map<String, Object> args = vars.getAdditionalProperties();

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
                    vars.removeAdditionalProperty(param.getKey());
                }
                continue;
            }
            if (param.getValue().getDefault() == null && args.get(param.getKey()) ==  null) { // required
                throw new JocMissingRequiredParameterException("Variable '" + param.getKey() + "' is missing but required");
            }
            if (args.containsKey(param.getKey())) {
                Object curArg = args.get(param.getKey());
                if (curArg == null) {
                    vars.removeAdditionalProperty(param.getKey());
                    continue;
                }
                
                switch (param.getValue().getType()) {
                case String:
                    if ((curArg instanceof String) == false) {
                        invalid = true;
                    } else {
                        String strArg = (String) curArg;
                        if (!allowEmptyValues && strArg.isEmpty() && param.getValue().getDefault() == null) {
                            throw new JocMissingRequiredParameterException("Variable '" + param.getKey() + "' is empty but required");
                        }
                        
                        try {
                            StringSizeSanitizer.test("variable '" + param.getKey() + "'", strArg);
                        } catch (IllegalArgumentException e1) {
                            throw new JocConfigurationException(e1.getMessage());
                        }
                    }
                    break;
                case Boolean:
                    if ((curArg instanceof Boolean) == false) {
                        if (curArg instanceof String) {
                            String strArg = (String) curArg;
                            if ("true".equals(strArg)) {
                                vars.setAdditionalProperty(param.getKey(), Boolean.TRUE);
                            } else if ("false".equals(strArg)) {
                                vars.setAdditionalProperty(param.getKey(), Boolean.FALSE);
                            } else if (allowDollarInValue && strArg.contains("$")) { 
                                // only relevant for addOrder instruction
                                Validator.validateExpression("Variable '" + param.getKey() + "': ", strArg);
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
                        String strArg = (String) curArg;
                        if (allowDollarInValue && strArg.contains("$")) {
                            // only relevant for addOrder instruction
                            Validator.validateExpression("Variable '" + param.getKey() + "': ", strArg);
                        } else {
                            try {
                                BigDecimal number = new BigDecimal(strArg);
                                vars.setAdditionalProperty(param.getKey(), number);
                            } catch (NumberFormatException e) {
                                invalid = true;
                            }
                        }
                    }
                    break;
                case List:
                    if ((curArg instanceof List) == false) {
                        invalid = true;
                    }
                    if (!invalid) {
                        checkListArguments((List<Map<String, Object>>) curArg, param.getValue().getListParameters(), param.getKey(), allowEmptyValues, allowDollarInValue);
                    }
                    break;
                }
                if (invalid) {
                    throw new JocConfigurationException(String.format("Variable '%s': Wrong data type %s (%s is expected).", param.getKey(), curArg
                            .getClass().getSimpleName().replaceFirst("Array", ""), param.getValue().getType().value()));
                }
            }
        }
        return vars;
    }

    private static List<Map<String, Object>> checkListArguments(List<Map<String, Object>> listVariables, ListParameters listParameters,
            String listKey, boolean allowEmptyValues, boolean allowDollarInValue) throws JocMissingRequiredParameterException, JocConfigurationException {
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
//                if (!listVariable.containsKey(p.getKey()) && p.getValue().getDefault() == null) { // required
//                    throw new JocMissingRequiredParameterException("Variable '" + p.getKey() + "' of list variable '" + listKey
//                            + "' is missing but required");
//                }
                
                Object curListArg = listVariable.get(p.getKey());
                
                if (curListArg == null) { // TODO later only if it is nullable
                    if (p.getValue().getDefault() == null) { // required? TODO later only if it is nullable
                        throw new JocMissingRequiredParameterException("Variable '" + p.getKey() + "' of list variable '" + listKey
                                + "' is missing but required");
                    } else {
                        listVariable.put(p.getKey(), p.getValue().getDefault());
                    }
                    continue;
                }
                
                if (p.getValue().getType().equals(ListParameterType.String) && !allowEmptyValues) {
                    if ((curListArg instanceof String) && ((String) curListArg).isEmpty()) {
                        if (p.getValue().getDefault() == null) { // required? TODO later only if it is nullable
                            throw new JocMissingRequiredParameterException("Variable '" + p.getKey() + "' of list variable '" + listKey
                                    + "' is missing but required");
                        } else {
                            listVariable.put(p.getKey(), p.getValue().getDefault());
                        }
                        continue;
                    }
                }
                
//                if ((curListArg instanceof String) && ((String) curListArg).isEmpty()) { // TODO later only if it is nullable
//                    continue;
//                }
                
                
                switch (p.getValue().getType()) {
                case String:
                    if ((curListArg instanceof String) == false) {
                        invalid = true;
                    } else {
                        String strListArg = (String) curListArg;
                        if (!allowEmptyValues && strListArg.isEmpty()) {
                            throw new JocMissingRequiredParameterException("Variable '" + p.getKey() + "' of list variable '" + listKey
                                    + "' is empty but required");
                        }
                        
                        try {
                            StringSizeSanitizer.test("variable '" + p.getKey() + "' of list variable '" + listKey, strListArg);
                        } catch (IllegalArgumentException e1) {
                            throw new JocConfigurationException(e1.getMessage());
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
                            } else if (allowDollarInValue && strArg.contains("$")) { 
                                // only relevant for addOrder instruction
                                Validator.validateExpression("Variable '" + p.getKey() + "' of list variable '" + listKey + "': ", strArg);
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
                        String strArg = (String) curListArg;
                        if (allowDollarInValue && strArg.contains("$")) {
                            // only relevant for addOrder instruction
                            Validator.validateExpression("Variable '" + p.getKey() + "' of list variable '" + listKey + "': ", strArg);
                        } else {
                            try {
                                BigDecimal number = new BigDecimal(strArg);
                                listVariable.put(p.getKey(), number);
                            } catch (NumberFormatException e) {
                                invalid = true;
                            }
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
    
    public static Optional<Set<String>> getNotFreshOrders(Collection<String> orderIds, JControllerState currentState) {
        Function1<Order<Order.State>, Object> notFreshAndExistsFilter = JOrderPredicates.and(o -> orderIds.contains(o.id().string()), JOrderPredicates
                .not(JOrderPredicates.byOrderState(Order.Fresh$.class)));
        Set<String> notFreshOrderIds = currentState.ordersBy(notFreshAndExistsFilter).map(JOrder::id).map(OrderId::string).collect(Collectors
                .toSet());
        if (!notFreshOrderIds.isEmpty()) {
            return Optional.of(notFreshOrderIds);
        }
        return Optional.empty();
    }
    
    public static Stream<String> getWorkflowNamesOfFreshOrders(Collection<String> orderIds, JControllerState currentState) {
        Function1<Order<Order.State>, Object> freshAndExistsFilter = JOrderPredicates.and(o -> orderIds.contains(o.id().string()),
                JOrderPredicates.byOrderState(Order.Fresh$.class));
        return currentState.ordersBy(freshAndExistsFilter).map(JOrder::workflowId).map(JWorkflowId::path).map(
                WorkflowPath::string).distinct();
    }
    
//    public static Either<List<Err419>, OrderIdMap> cancelAndAddFreshOrder(Collection<String> temporaryOrderIds,
//            DailyPlanModifyOrder dailyplanModifyOrder, String accessToken, JocError jocError, Long auditlogId, ZoneId zoneId,
//            SOSAuthFolderPermissions folderPermissions) throws ControllerConnectionResetException, ControllerConnectionRefusedException,
//            DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException,
//            ExecutionException {
//
//        Either<List<Err419>, OrderIdMap> result = Either.right(new OrderIdMap());
//        if (temporaryOrderIds.isEmpty()) {
//            return result;
//        }
//        JControllerProxy proxy = Proxy.of(dailyplanModifyOrder.getControllerId());
//        JControllerState currentState = proxy.currentState();
//        return cancelAndAddFreshOrder(temporaryOrderIds, dailyplanModifyOrder, accessToken, jocError, auditlogId, proxy, currentState,
//                zoneId, folderPermissions);
//    }
    
    public static Either<List<Err419>, OrderIdMap> cancelAndAddFreshOrder(Collection<String> temporaryOrderIds,
        DailyPlanModifyOrder dailyplanModifyOrder, String accessToken, JocError jocError, Long auditlogId, JControllerProxy proxy,
        JControllerState currentState, ZoneId zoneId, Map<String, List<Object>> labelMap, Set<BlockPosition> availableBlockPositions, 
        SOSAuthFolderPermissions folderPermissions) throws ControllerConnectionResetException, ControllerConnectionRefusedException, 
        JocConfigurationException, ExecutionException {

        Either<List<Err419>, OrderIdMap> result = Either.right(new OrderIdMap());
        if (temporaryOrderIds.isEmpty()) {
            return result;
        }
        final String controllerId = dailyplanModifyOrder.getControllerId();
        Instant now = Instant.now();
        List<AuditLogDetail> auditLogDetails = new ArrayList<>();
        
        // JOC-1453 consider labels
        final List<Object> startPosition = getPosition(dailyplanModifyOrder.getStartPosition(), labelMap);
        final List<List<Object>> endPositions = getPositions(dailyplanModifyOrder.getEndPositions(), labelMap);
        final boolean forceJobAdmission = dailyplanModifyOrder.getForceJobAdmission() == Boolean.TRUE;
        final boolean allowEmptyArguments = ClusterSettings.getAllowEmptyArguments(Globals.getConfigurationGlobalsJoc());
        final boolean variablesAreModified = dailyplanModifyOrder.getVariables() != null && !dailyplanModifyOrder.getVariables()
                .getAdditionalProperties().isEmpty();
        final boolean variablesAreRemoved = dailyplanModifyOrder.getRemoveVariables() != null;
        final Optional<Long> secondsFromCurDate = JobSchedulerDate.getSecondsOfRelativeCurDate(dailyplanModifyOrder.getScheduledFor());
        Map<String, Requirements> cachedRequirements = new HashMap<>(1);
        
        Function<JOrder, Either<Err419, FreshOrder>> mapper = order -> {
            Either<Err419, FreshOrder> either = null;
            try {
                String workflowName = order.workflowId().path().string();
                Map<String, Value> args = order.arguments();
                Either<Problem, JWorkflow> e = currentState.repo().idToCheckedWorkflow(order.workflowId());
                ProblemHelper.throwProblemIfExist(e);
                String workflowPath = WorkflowPaths.getPath(workflowName);
                if (!folderPermissions.isPermittedForFolder(Paths.get(workflowPath).getParent().toString().replace('\\', '/'))) {
                    throw new JocFolderPermissionsException(workflowPath);
                }
                
                //Workflow workflow = Globals.objectMapper.readValue(e.get().toJson(), Workflow.class);
                if (variablesAreModified || variablesAreRemoved) {
                    if (!cachedRequirements.containsKey(workflowName)) {
                        cachedRequirements.put(workflowName, getRequirements(workflowName, controllerId));
                    }
                    Variables vars = scalaValuedArgumentsToVariables(args);
                    if (variablesAreModified) {
                        vars.setAdditionalProperties(dailyplanModifyOrder.getVariables().getAdditionalProperties());
                    }
                    if (variablesAreRemoved) {
                        dailyplanModifyOrder.getRemoveVariables().forEach(k -> vars.removeAdditionalProperty(k));
                    }
                    args = variablesToScalaValuedArguments(checkArguments(vars, cachedRequirements.get(workflowName), allowEmptyArguments));
                }

                // modify scheduledFor if necessary
                Optional<Instant> scheduledFor = order.scheduledFor();
                if (dailyplanModifyOrder.getScheduledFor() != null) {
                    if (isDateWithoutTime(dailyplanModifyOrder.getScheduledFor())) {
                        if (scheduledFor.isPresent()) {
                            scheduledFor = Optional.of(JobSchedulerDate.convertUTCDate(dailyplanModifyOrder.getScheduledFor(), scheduledFor.get(),
                                    dailyplanModifyOrder.getTimeZone()));
                        } else {
                            scheduledFor = Optional.of(JobSchedulerDate.convertUTCDate(dailyplanModifyOrder.getScheduledFor(), now,
                                    dailyplanModifyOrder.getTimeZone()));
                        }
                    } else if (secondsFromCurDate.isPresent()) {
                        if (scheduledFor.isPresent()) {
                            scheduledFor = Optional.of(scheduledFor.get().plusSeconds(secondsFromCurDate.get()));
                        } else {
                            scheduledFor = Optional.of(now.plusSeconds(secondsFromCurDate.get()));
                        }
                    } else {
                        scheduledFor = JobSchedulerDate.getScheduledForInUTC(dailyplanModifyOrder.getScheduledFor(), dailyplanModifyOrder
                                .getTimeZone());
                    }
                }
                if (!scheduledFor.isPresent()) {
                    scheduledFor = Optional.of(now);
                }
                
                Optional<JPositionOrLabel> startPos = Optional.empty();
                Set<JPositionOrLabel> endPoss = Collections.emptySet();
                JBranchPath jBrachPath = null;
                
                if (dailyplanModifyOrder.getBlockPosition() != null) {
                    
                    BlockPosition blockPosition = getBlockPosition(dailyplanModifyOrder.getBlockPosition(), workflowName, availableBlockPositions);

                    //check start-/endpositions inside block
                    startPos = OrdersHelper.getStartPositionInBlock(startPosition, blockPosition);
                    endPoss = OrdersHelper.getEndPositionInBlock(endPositions, blockPosition);
                    
                    jBrachPath = OrdersHelper.getJBranchPath(blockPosition);
                    
                } else {
                    // modify start/end positions
                    Set<String> reachablePositions = CheckedAddOrdersPositions.getReachablePositions(e.get());

                    startPos = getStartPosition(startPosition, reachablePositions, order.workflowPosition().position());
                    endPoss = getEndPositions(endPositions, reachablePositions, JavaConverters.asJava(order.asScala()
                            .stopPositions()));
                }
                
                FreshOrder o = new FreshOrder(order.id(), order.workflowId().path(), args, scheduledFor, jBrachPath, startPos, endPoss,
                        forceJobAdmission, zoneId);
                auditLogDetails.add(new AuditLogDetail(workflowPath, order.id().string(), controllerId));
                either = Either.right(o);
            } catch (Exception ex) {
                either = Either.left(new BulkError(LOGGER).get(ex, jocError, order.workflowId().path().string() + "/" + order.id().string()));
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
                            // TODO check if really all orders are cancelled. Only then the same orderId can be used fro addOrders(...)
                            
//                            try {
//                                for (int i = 0; i < 10; i++) {
//                                    try {
//                                        TimeUnit.SECONDS.sleep(1L);
//                                        if (i < 9 && !proxy.currentState().orderIds().stream().anyMatch(o -> freshOrders.keySet().contains(o))) {
//                                            // all orders are cancelled
//                                            break;
//                                        }
//                                        if (i == 9) {
//                                            Set<OrderId> oIds = proxy.currentState().orderIds();
//                                            oIds.retainAll(freshOrders.keySet());
//                                            if (oIds.isEmpty()) {
//                                                // all orders are cancelled
//                                                break;
//                                            }
//                                            // addOrders not possible for retained oIds because not cancelled
//                                            // throw Problem
//                                            if (!oIds.isEmpty()) {
//                                                oIds.forEach(o -> freshOrders.remove(o));
//                                                Either<Problem, Void> e = Either.left(Problem.pure("The Orders " + oIds.toString()
//                                                        + " cannot be modified because they could not be deleted before within 10 seconds."));
//                                                ProblemHelper.postProblemEventIfExist(e, accessToken, jocError, controllerId);
//                                            }
//                                        }
//                                    } catch (Exception e) {
//                                        //
//                                    }
//                                }
//                            } catch (Exception e) {
//                                //
//                            }
                            
                            proxy.api().addOrders(Flux.fromIterable(freshOrders.values())).thenAccept(either3 -> {
                                ProblemHelper.postProblemEventIfExist(either3, accessToken, jocError, controllerId);
                                if (either3.isRight()) {
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
    
    private static boolean isDateWithoutTime(String datetime) {
        return datetime == null ? false : datetime.matches("\\d{4}-\\d{2}-\\d{2}");
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

    public static String getUniqueOrderId(ZoneId zoneId) {
        if (zoneId == null) {
            zoneId = getDailyPlanTimeZone();
        }
        return getUniqueOrderId(ZonedDateTime.of(LocalDateTime.now(zoneId), ZoneId.systemDefault()));
        //return Long.valueOf((Instant.now().toEpochMilli() * 100) + (n % 100)).toString().substring(4);
    }
    
    public static String getUniqueOrderId(ZonedDateTime zonedDateTime) {
        int n = no.getAndUpdate(x -> x == Integer.MAX_VALUE ? 0 : x + 1);
        return Long.valueOf((zonedDateTime.toInstant().toEpochMilli() * 100) + (n % 100))
                .toString().substring(4);
        //return Long.valueOf((Instant.now().toEpochMilli() * 100) + (n % 100)).toString().substring(4);
    }

    public static String generateNewFromOldOrderId(String oldOrderId, String newDailyPlanDate, ZoneId zoneId) {
        // #2021-10-12#C4038226057-00012-12-dailyplan_shedule_cyclic
        // #2021-10-25#C1234567890-00012-12-dailyplan_shedule_cyclic
        return generateNewFromOldOrderId(getWithNewDateFromOldOrderId(oldOrderId, newDailyPlanDate), zoneId);
    }
    
    public static String getWithNewDateFromOldOrderId(String oldOrderId, String newDailyPlanDate) {
        return "#" + newDailyPlanDate + oldOrderId.substring(11);
    }

    public static String generateNewFromOldOrderId(String oldOrderId, ZoneId zoneId) {
        return getNewFromOldOrderId(oldOrderId, getUniqueOrderId(zoneId));
    }

    public static String getNewFromOldOrderId(String oldOrderId, String newUniqueOrderIdPart) {
        // #2021-10-12#C4038226057-00012-12-dailyplan_shedule_cyclic
        // replace 4038226057 with the new part
        return oldOrderId.replaceFirst("^(#\\d{4}-\\d{2}-\\d{2}#[A-Z])\\d{10,11}(-.+)$", "$1" + newUniqueOrderIdPart + "$2");
    }

    public static JFreshOrder mapToFreshOrder(AddOrder order, ZoneId zoneId, Optional<JPositionOrLabel> startPos, Set<JPositionOrLabel> endPoss,
            JBranchPath blockPosition, boolean forceJobAdmission) {
        if (zoneId == null) {
            zoneId = getDailyPlanTimeZone();
        }
        ZonedDateTime zonedNow = ZonedDateTime.of(LocalDateTime.now(zoneId), ZoneId.systemDefault());
        String orderId = String.format("#%s#T%s-%s", datetimeFormatter.format(zonedNow), getUniqueOrderId(zonedNow), order.getOrderName());
        Optional<Instant> scheduledFor = JobSchedulerDate.getScheduledForInUTC(order.getScheduledFor(), order.getTimeZone());
        // if (!scheduledFor.isPresent()) {
        // scheduledFor = Optional.of(Instant.now());
        // }
        return mapToFreshOrder(OrderId.of(orderId), WorkflowPath.of(JocInventory.pathToName(order.getWorkflowPath())),
                variablesToScalaValuedArguments(order.getArguments()), scheduledFor, startPos, endPoss, blockPosition, forceJobAdmission);
    }

    private static JFreshOrder mapToFreshOrder(OrderId orderId, WorkflowPath workflowPath, Map<String, Value> args, Optional<Instant> scheduledFor,
            Optional<JPositionOrLabel> startPos, Set<JPositionOrLabel> endPoss, JBranchPath blockPosition, boolean forceJobAdmission) {
        if (blockPosition == null) {
            blockPosition = JBranchPath.empty();
        }
        return JFreshOrder.of(orderId, workflowPath, scheduledFor, args, true, forceJobAdmission, blockPosition, startPos, endPoss);
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
//                if (val == null) {
//                    //MissingValue;
//                    arguments.put(key, (Value) val);
//                } else 
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
                    arguments.put(key, NumberValue.of((BigDecimal) val));
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
            args.forEach((k, v) -> {
                if (v instanceof js7.data.value.ListValue) {
                    List<Object> l1 = new ArrayList<>();
                    ((js7.data.value.ListValue) v).toJava().forEach(item -> {
                        if (item instanceof js7.data.value.ObjectValue) {
                            Map<String, Object> m = new HashMap<>();
                            ((js7.data.value.ObjectValue) item).toJava().forEach((k1, v1) -> m.put(k1, v1.toJava()));
                            l1.add(m);
                        } else {
                            l1.add(item.toJava());
                        }
                    });
                    variables.setAdditionalProperty(k, l1);
                } else {
                    variables.setAdditionalProperty(k, v.toJava());
                }
            });
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
            Collection<DBItemDailyPlanOrder> listOfDailyPlanOrders) throws ControllerConnectionResetException, ControllerConnectionRefusedException,
            DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException,
            ExecutionException {

        Set<OrderId> setOfSubmittedOrderIds = listOfDailyPlanOrders.stream().parallel().filter(DBItemDailyPlanOrder::getSubmitted).map(
                DBItemDailyPlanOrder::getOrderId).map(OrderId::of).collect(Collectors.toSet());

        if (setOfSubmittedOrderIds.isEmpty()) { // if order isn't submitted then it should work without proxy
            return CompletableFuture.supplyAsync(() -> Either.right(null));
        }
        JControllerProxy proxy = Proxy.of(controllerId);
        Set<OrderId> orderIds = proxy.currentState().ordersBy(o -> setOfSubmittedOrderIds.contains(o.id())).parallel().map(JOrder::id).collect(
                (Collectors.toSet()));
        if (orderIds.isEmpty()) {
            return CompletableFuture.supplyAsync(() -> Either.right(null));
        } else {
            return proxy.api().cancelOrders(orderIds, JCancellationMode.freshOnly());
        }
    }

    public static CompletableFuture<Either<Problem, Void>> removeFromJobSchedulerControllerWithHistory(String controllerId,
            List<DBItemDailyPlanWithHistory> listOfPlannedOrders) {
        Set<OrderId> orderIds = listOfPlannedOrders.stream().parallel().map(DBItemDailyPlanWithHistory::getOrderId).map(OrderId::of).collect(
                Collectors.toSet());
        if (orderIds.isEmpty()) {
            return CompletableFuture.supplyAsync(() -> Either.right(null));
        } else {
            return ControllerApi.of(controllerId).cancelOrders(orderIds, JCancellationMode.freshOnly());
        }
    }

    // #2021-10-12#C40382260571-00012-12-dailyplan_shedule_cyclic
    // #2021-10-12#C40382260571-
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
    
    public static Map<OrderId, Obstacle> getWaitingOrderIds(Collection<OrderId> orderIds, JControllerState controllerState) {
        if (!orderIds.isEmpty()) {
            Either<Problem, Map<OrderId, Set<JOrderObstacle>>> obstaclesE = controllerState.ordersToObstacles(orderIds, controllerState
                    .instant());
            if (obstaclesE.isRight()) {
                Function<Map.Entry<OrderId, Set<JOrderObstacle>>, Obstacle> obstacleMapper = e -> mapObstacle(e.getValue().iterator().next());
                Map<OrderId, Set<JOrderObstacle>> obstacles = obstaclesE.get();
                // Attention: It could be that removeIf -> java.lang.UnsupportedOperationException
                return obstacles.entrySet().stream().peek(e -> e.getValue().removeIf(obstacle -> (obstacle instanceof JOrderObstacle.WaitingForOtherTime)
                        || (obstacle instanceof JOrderObstacle.WaitingForTime))).filter(e -> !e.getValue().isEmpty()).collect(Collectors.toMap(
                                Map.Entry::getKey, obstacleMapper));
            }
        }
        return Collections.emptyMap();
    }
    
    public static Optional<Obstacle> getObstacle(OrderId orderId, JControllerState controllerState) {
        Either<Problem, Set<JOrderObstacle>> obstaclesE = controllerState.orderToObstacles(orderId, controllerState.instant());
        if (obstaclesE.isRight()) {
            Set<JOrderObstacle> obstacles = obstaclesE.get();
            for (JOrderObstacle obstacle : obstacles) {
                if (obstacle instanceof JOrderObstacle.WaitingForOtherTime || obstacle instanceof JOrderObstacle.WaitingForCommand$) {
                    continue;
                }
                return Optional.ofNullable(mapObstacle(obstacle));
            }
        }
        return Optional.empty();
    }
    
    public static Obstacle mapObstacle(JOrderObstacle obstacle) {
        Obstacle ob = new Obstacle();
        if (obstacle instanceof JOrderObstacle.WaitingForAdmission) {
            ob.setType(ObstacleType.WaitingForAdmission);
            ob.setUntil(Date.from(((JOrderObstacle.WaitingForAdmission) obstacle).until()));
        } else if (obstacle instanceof JOrderObstacle.JobProcessLimitReached) {
            ob.setType(ObstacleType.JobParallelismLimitReached);
        } else if (obstacle instanceof JOrderObstacle.AgentProcessLimitReached) {
            ob.setType(ObstacleType.AgentProcessLimitReached);
        } else if (obstacle instanceof JOrderObstacle.WorkflowSuspended) {
            ob.setType(ObstacleType.WorkflowIsSuspended);
        } else {
            return null;
        }
        return ob;
    }
    
    public static Set<String> getChildOrders(JControllerState currentState) {
        return currentState.orderIds().stream().map(OrderId::string).filter(s -> s.contains("|")).collect(Collectors.toSet());
    }

    public static Optional<Boolean> orderIsInImplicitEnd(JOrder o, JControllerState controllerState) {
        if (controllerState == null || o == null) {
            return Optional.empty();
        }
        return Optional.of(orderIsInImplicitEnd(o.asScala().workflowPosition(), controllerState));
    }
    
    private static boolean orderIsInImplicitEnd(WorkflowPosition wPos, JControllerState controllerState) {
        return controllerState.asScala().instruction(wPos) instanceof ImplicitEnd;
    }

    public static Long getScheduledForMillis(String orderId, ZoneId zoneId, Long defaultMillis) {
        Instant instant = getScheduledForMillis(orderId, zoneId);
        if (instant == null) {
            return defaultMillis;
        }
        return instant.toEpochMilli();
    }
    
    public static Instant getScheduledForMillis(String orderId, ZoneId zoneId) {
        //reflect DailyPlan timezone
        try {
            Matcher m = orderIdPattern.matcher(orderId);
            if (m.find()) {
                long first5 = Instant.parse(m.group(1) + "T00:00:00Z").toEpochMilli() / 100000000L;
                long first4 = first5 / 10L;
                if (first5 - (first4 * 10) > Long.valueOf(m.group(2))) {
                    first4++;
                }
                Long millis = (first4 * 1000000000L) + Long.valueOf(m.group(2) + m.group(3));
                if (zoneId == null) {
                    zoneId = getDailyPlanTimeZone();
                }
                return ZonedDateTime.of(LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault()), zoneId).toInstant();
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static Long getScheduledForMillis(JOrder order, ZoneId zoneId) {
        return getScheduledForMillis(order, zoneId, null);
    }

    public static Long getScheduledForMillis(JOrder order, ZoneId zoneId, Long defaultMillis) {
        if (order.scheduledFor().isPresent()) {
            return order.scheduledFor().get().toEpochMilli();
        } else {
            return getScheduledForMillis(order.id().string(), zoneId, defaultMillis);
        }
    }

    public static Long getScheduledForMillis(Order<Order.State> order, ZoneId zoneId, Long defaultMillis) {
        if (!order.scheduledFor().isEmpty()) {
            return order.scheduledFor().get().toEpochMilli();
        } else {
            return getScheduledForMillis(order.id().string(), zoneId, defaultMillis);
        }
    }
    
    public static Instant getScheduledForInstant(JOrder order) {
        return getScheduledForInstant(order, null);
    }

    public static Instant getScheduledForInstant(JOrder order, ZoneId zoneId) {
        if (order.scheduledFor().isPresent()) {
            return order.scheduledFor().get();
        } else {
            return getScheduledForMillis(order.id().string(), zoneId);
        }
    }

    public static ToLongFunction<JOrder> getCompareScheduledFor(ZoneId zoneId, long surveyDateMillis) {
        return o -> getScheduledForMillis(o, zoneId, surveyDateMillis);
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
    
    @SuppressWarnings("unchecked")
    public static List<Object> getPosition(Object pos, Map<String, List<Object>> labelMap) {
        if (labelMap == null) {
            labelMap = Collections.emptyMap(); 
        }
        if (pos != null) {
            if (pos instanceof String) {
                List<Object> position = labelMap.get((String) pos);
                if (position == null) {
                    throw new IllegalArgumentException("invalid label '" + (String) pos + "'");
                }
                return position;
            } else {
                return (List<Object>) pos;
            }
        }
        return null;
    }
    
    public static List<List<Object>> getPositions(List<Object> poss, Map<String, List<Object>> labelMap) {
        List<List<Object>> positions = null;
        if (poss != null) {
            positions = poss.stream().filter(Objects::nonNull).map(ep -> getPosition(ep, labelMap)).filter(Objects::nonNull).collect(Collectors
                    .toList());
        }
        return positions;
    }
    
    public static Optional<JPositionOrLabel> getStartPosition(Object pos, Map<String, List<Object>> labelMap, Set<String> reachablePositions) {
        List<Object> startPosition = getPosition(pos, labelMap);
        return getStartPosition(startPosition, reachablePositions, null);
    }
    
    public static Optional<JPositionOrLabel> getStartPosition(List<Object> pos, Set<String> reachablePositions) {
        return getStartPosition(pos, reachablePositions, null);
    }

    public static Optional<JPositionOrLabel> getStartPosition(List<Object> pos, Set<String> reachablePositions, JPosition defaultPosition) {
        Optional<JPositionOrLabel> posOpt = Optional.empty();
        if (defaultPosition != null && !JPosition.apply(Position.First()).equals(defaultPosition)) {
            posOpt = Optional.of(defaultPosition);
        }
        if (pos != null && !pos.isEmpty()) {
            Either<Problem, JPosition> posE = JPosition.fromList(pos);
            ProblemHelper.throwProblemIfExist(posE);
            if (!JPosition.apply(Position.First()).equals(posE.get())) {
                if (reachablePositions == null || reachablePositions.contains(posE.get().toString())) {
                    return Optional.of(posE.get());
                } else {
                    throw new JocBadRequestException("Invalid start position '" + pos.toString() + "'");
                }
            }
        }
        return posOpt;
    }
    
    public static Set<JPositionOrLabel> getEndPosition(List<Object> poss, Map<String, List<Object>> labelMap, Set<String> reachablePositions) {
        List<List<Object>> endPositions = getPositions(poss, labelMap);
        return getEndPositions(endPositions, reachablePositions, Collections.emptySet());
    }
    
    public static Set<JPositionOrLabel> getEndPosition(List<List<Object>> poss, Set<String> reachablePositions) {
        return getEndPositions(poss, reachablePositions, Collections.emptySet());
    }

    public static Set<JPositionOrLabel> getEndPositions(List<List<Object>> poss, Set<String> reachablePositions, Set<PositionOrLabel> defaultPositions) {
        Set<JPositionOrLabel> posOpt = Collections.emptySet();
        if (poss != null && !poss.isEmpty()) {
            posOpt = new HashSet<>();
            for (List<Object> pos : poss) {
                Either<Problem, JPosition> posE = JPosition.fromList(pos);
                ProblemHelper.throwProblemIfExist(posE);
                // endpositions are arbitrary or not?
//                if (reachablePositions.contains(posE.get().toString())) {
                    posOpt.add(posE.get());
//                } else {
//                    throw new JocBadRequestException("Invalid end position '" + pos.toString() + "'");
//                }
            }
        } else {
            if (!defaultPositions.isEmpty()) {
                Set<JPositionOrLabel> defaultPos = defaultPositions.stream().map(JPositionOrLabel::apply).collect(Collectors.toSet());
                posOpt = defaultPos;
            }
        }
        return posOpt;
    }
    
    public static boolean endPositionBeforeStartPosition(JPosition start, JPosition end) {
        List<Object> startPos = start.toList();
        List<Object> endPos = end.toList();
        int compareFirstLevel = ((Integer) endPos.get(0)).compareTo((Integer) startPos.get(0));
        if (compareFirstLevel == -1) {
            return true;
        }
        if (compareFirstLevel == 0 && startPos.size() == endPos.size() && startPos.size() == 3) {
            int compareSecondLevel = ((Integer) endPos.get(2)).compareTo((Integer) startPos.get(2));
            if (compareSecondLevel == -1) {
                return true;
            }
        }
        return false;
    }
    
    public static ZoneId getDailyPlanTimeZone() {
        ConfigurationEntry timeZoneEntry = Globals.getConfigurationGlobalsDailyPlan().getTimeZone();
        String timeZone = timeZoneEntry.getValue();
        if (timeZone == null) {
            timeZone = timeZoneEntry.getDefault();
        }
        try {
            return ZoneId.of(timeZone);
        } catch (Exception e) {
            LOGGER.warn("DailyPlan timezone is invalid. Etc/UTC is used as fallback.", e);
            return ZoneId.of("Etc/UTC");
        }
    }

    public static JBranchPath getJBranchPath(BlockPosition pos) {
        Either<Problem, JBranchPath> eBranchPath = JBranchPath.fromList(pos.getPosition());
        if (eBranchPath.isLeft()) {
            throw new JocBadRequestException("The block position '" + pos.getPosition().toString() + "' has wrong syntax: " + eBranchPath
                    .getLeft().message());
        }
        return eBranchPath.get();
    }
    
    public static BlockPosition getBlockPosition(Object blockPosition, String workflowName, Set<BlockPosition> availableBlockPositions) {
        if (blockPosition == null) {
            return null;
        }
        Optional<BlockPosition> listBlockPosOpt = Optional.empty();
        if (blockPosition instanceof String) {
            String strBlockPos = (String) blockPosition;
            if (!strBlockPos.isEmpty()) {
                listBlockPosOpt = availableBlockPositions.stream().filter(p -> p.getLabel() != null).filter(p -> p.getLabel().equals(strBlockPos))
                        .findAny();
                if (!listBlockPosOpt.isPresent()) {
                    workflowName = workflowName == null ? "" : "'" + workflowName + "' ";
                    throw new JocBadRequestException("Workflow " + workflowName + "doesn't contain the block position '" + strBlockPos + "'");
                }
            } else {
                return null;
            }
        } else if (blockPosition instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<Object> listBlockPos = (List<Object>) blockPosition;
            listBlockPosOpt = availableBlockPositions.stream().filter(p -> p.getPosition().equals(listBlockPos)).findAny();
            if (!listBlockPosOpt.isPresent()) {
                workflowName = workflowName == null ? "" : "'" + workflowName + "' ";
                throw new JocBadRequestException("Workflow " + workflowName + "doesn't contain the block position '" + listBlockPos.toString()
                        + "'");
            }
        }
        return listBlockPosOpt.get();
    }

    public static Optional<JPositionOrLabel> getStartPositionInBlock(Object pos, Map<String, List<Object>> labelMap, BlockPosition blockPosition) {
        Optional<JPositionOrLabel> posOpt = getStartPosition(pos, labelMap, null);
        return getStartPositionInBlock(posOpt, blockPosition);
    }
    
    public static Optional<JPositionOrLabel> getStartPositionInBlock(List<Object> pos, BlockPosition blockPosition) {
        Optional<JPositionOrLabel> posOpt = getStartPosition(pos, null);
        return getStartPositionInBlock(posOpt, blockPosition);
    }
    
    private static Optional<JPositionOrLabel> getStartPositionInBlock(Optional<JPositionOrLabel> posOpt, BlockPosition blockPosition) {
        if (posOpt.isPresent() && blockPosition != null) {
            if (!posOpt.get().toString().startsWith(blockPosition.getPositionString() + ":")) {
                throw new JocBadRequestException("Invalid start position '" + posOpt.get().toString() + "': It has to be inside the block '" + blockPosition
                        .getPosition().toString() + "'.");
            }
            if (blockPosition.getPositions() != null && !blockPosition.getPositions().stream().map(
                    com.sos.joc.model.order.Position::getPositionString).anyMatch(s -> s.equals(posOpt.get().toString()))) {
                throw new JocBadRequestException("Invalid start position '" + posOpt.get().toString() + "': allowed positions are '" + blockPosition
                        .getPositions().stream().map(com.sos.joc.model.order.Position::getPositionString).collect(Collectors.toList()).toString()
                        + "'.");
            }
        }
        return posOpt;
    }
    
    public static Set<JPositionOrLabel> getEndPositionInBlock(List<Object> poss, Map<String, List<Object>> labelMap, BlockPosition blockPosition) {
        Set<JPositionOrLabel> endPoss = getEndPosition(poss, labelMap, null);
        return getEndPositionInBlock(endPoss, blockPosition);
    }
    
    public static Set<JPositionOrLabel> getEndPositionInBlock(List<List<Object>> poss, BlockPosition blockPosition) {
        Set<JPositionOrLabel> endPoss = getEndPosition(poss, null);
        return getEndPositionInBlock(endPoss, blockPosition);
    }
    
    private static Set<JPositionOrLabel> getEndPositionInBlock(Set<JPositionOrLabel> endPoss, BlockPosition blockPosition) {
        if (endPoss != null && blockPosition != null) {
            endPoss.forEach(pos -> {
                if (!pos.toString().startsWith(blockPosition.getPositionString() + ":")) {
                    throw new JocBadRequestException("Invalid end position '" + pos.toString() + "': It has to be inside the block '" + blockPosition
                            .getPosition().toString() + "'.");
                }
            });
        }
        return endPoss;
    }

}
