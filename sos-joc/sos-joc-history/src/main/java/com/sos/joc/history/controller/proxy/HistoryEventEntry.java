package com.sos.joc.history.controller.proxy;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.agent.AgentVersionUpdatedEvent;
import com.sos.joc.event.bean.agent.SubagentVersionUpdatedEvent;
import com.sos.joc.history.controller.exception.proxy.HistoryControllerProxyEventException;
import com.sos.joc.history.controller.proxy.fatevent.FatExpectNotice;
import com.sos.joc.history.controller.proxy.fatevent.FatExpectNotices;
import com.sos.joc.history.controller.proxy.fatevent.FatOutcome;
import com.sos.joc.history.controller.proxy.fatevent.FatPostNotice;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.base.time.Timestamp;
import js7.base.web.Uri;
import js7.data.agent.AgentPath;
import js7.data.agent.AgentRefStateEvent.AgentCouplingFailed;
import js7.data.agent.AgentRefStateEvent.AgentReady;
import js7.data.board.Notice;
import js7.data.cluster.ClusterEvent.ClusterCoupled;
import js7.data.controller.ControllerEvent.ControllerReady;
import js7.data.event.Event;
import js7.data.event.KeyedEvent;
import js7.data.event.Stamped;
import js7.data.lock.Lock;
import js7.data.lock.LockPath;
import js7.data.node.NodeId;
import js7.data.order.Order;
import js7.data.order.Order.State;
import js7.data.order.OrderEvent;
import js7.data.order.OrderEvent.LockDemand;
import js7.data.order.OrderEvent.OrderFinished;
import js7.data.order.OrderEvent.OrderLocksAcquired;
import js7.data.order.OrderEvent.OrderLocksQueued;
import js7.data.order.OrderEvent.OrderLocksReleased;
import js7.data.order.OrderEvent.OrderNoticePosted;
import js7.data.order.OrderEvent.OrderNoticesConsumptionStarted;
import js7.data.order.OrderEvent.OrderOutcomeAdded;
import js7.data.order.OrderId;
import js7.data.order.Outcome;
import js7.data.order.Outcome.Completed;
import js7.data.order.Outcome.Disrupted;
import js7.data.order.Outcome.Failed;
import js7.data.order.Outcome.Killed;
import js7.data.order.Outcome.TimedOut;
import js7.data.subagent.SubagentId;
import js7.data.subagent.SubagentItemStateEvent.SubagentDedicated;
import js7.data.value.Value;
import js7.data.workflow.Instruction;
import js7.data.workflow.instructions.ExpectNotices;
import js7.data.workflow.instructions.Fail;
import js7.data.workflow.instructions.Finish;
import js7.data.workflow.instructions.executable.WorkflowJob.Name;
import js7.data_for_java.agent.JAgentRef;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.lock.JLock;
import js7.data_for_java.lock.JLockState;
import js7.data_for_java.order.JOrder;
import js7.data_for_java.order.JOrder.Forked;
import js7.data_for_java.order.JOrderEvent;
import js7.data_for_java.order.JOrderEvent.JExpectedNotice;
import js7.data_for_java.order.JOrderEvent.JOrderFailed;
import js7.data_for_java.order.JOrderEvent.JOrderForked;
import js7.data_for_java.order.JOrderObstacle;
import js7.data_for_java.order.JOrderObstacle.WaitingForAdmission;
import js7.data_for_java.subagent.JSubagentItem;
import js7.data_for_java.workflow.JWorkflow;
import js7.data_for_java.workflow.JWorkflowId;
import js7.data_for_java.workflow.position.JPosition;
import js7.data_for_java.workflow.position.JWorkflowPosition;
import js7.proxy.javaapi.data.controller.JEventAndControllerState;
import scala.Option;
import scala.collection.JavaConverters;
import scala.concurrent.duration.FiniteDuration;
import scala.jdk.javaapi.OptionConverters;

public class HistoryEventEntry {

    public static enum OutcomeType {
        succeeded, failed, disrupted, broken, killed, timedout
    }

    public static enum OutcomeTypeFailedReason {
        finish_instruction, fail_instruction
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryEventEntry.class);

    private static final String NAMED_NAME_RETURN_CODE = "returnCode";

    private final JEventAndControllerState<Event> eventAndState;
    private final KeyedEvent<Event> keyedEvent;
    private final Event event;
    private final Long eventId;
    private final Date eventDate;
    private final HistoryEventType eventType;
    private final String controllerId;

    public HistoryEventEntry(String controllerId, JEventAndControllerState<Event> es) {
        this.controllerId = controllerId;

        eventAndState = es;

        Stamped<KeyedEvent<Event>> stampedEvent = eventAndState.stampedEvent();
        keyedEvent = stampedEvent.value();
        event = keyedEvent.event();
        eventId = stampedEvent.eventId();
        eventDate = Date.from(Instant.ofEpochMilli(stampedEvent.timestampMillis()));
        eventType = HistoryEventType.fromValue(event.getClass().getSimpleName());
    }

    public static Date getDate(Timestamp t) {
        if (t == null) {
            return null;
        }
        return Date.from(Instant.ofEpochMilli(t.toEpochMilli()));
    }

    public static Date getDate(Option<Timestamp> ot) {
        if (ot == null) {
            return null;
        }
        Optional<Timestamp> t = OptionConverters.toJava(ot);
        if (t.isPresent()) {
            return getDate(t.get());
        }
        return null;
    }

    public static String getStringValue(Value val) {
        if (val == null) {
            return null;
        }
        return val.toJava().toString();
    }

    public Long getEventId() {
        return eventId;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public JOrderEvent getJOrderEvent() {
        return JOrderEvent.apply((OrderEvent) event);
    }

    public KeyedEvent<Event> getKeyedEvent() {
        return keyedEvent;
    }

    public Event getEvent() {
        return event;
    }

    public HistoryEventType getEventType() {
        return eventType;
    }

    public HistoryClusterCoupled getClusterCoupled() {
        return new HistoryClusterCoupled();
    }

    public HistoryControllerReady getControllerReady() {
        return new HistoryControllerReady();
    }

    public HistoryAgentCouplingFailed getAgentCouplingFailed() {
        return new HistoryAgentCouplingFailed();
    }

    public HistoryAgentShutDown getAgentShutDown() {
        return new HistoryAgentShutDown();
    }

    public HistoryAgentReady getAgentReady() throws HistoryControllerProxyEventException {
        return new HistoryAgentReady();
    }

    public HistoryAgentSubagentDedicated getAgentSubagentDedicated() {
        return new HistoryAgentSubagentDedicated();
    }

    public HistoryOrder getOrder() throws HistoryControllerProxyEventException {
        return new HistoryOrder();
    }

    public HistoryOrder getCheckedOrder() throws HistoryControllerProxyEventException {
        return new HistoryOrder(eventAndState.state());
    }

    public HistoryOrder getCheckedOrderFromPreviousState() throws HistoryControllerProxyEventException {
        return new HistoryOrder(eventAndState.previousState());
    }

    public class HistoryOrder {

        private final OrderId orderId;
        private JControllerState state;

        private JOrder order;
        private WorkflowInfo workflowInfo;
        private StepInfo stepInfo;
        private OutcomeInfo outcomeInfo;
        private List<ForkedChild> forkedChilds;

        private HistoryOrder() throws HistoryControllerProxyEventException {
            this(null);
        }

        private HistoryOrder(JControllerState controllerState) throws HistoryControllerProxyEventException {
            orderId = (OrderId) keyedEvent.key();
            if (controllerState != null) {
                state = controllerState;
                // Either<Problem, JOrder> po = state.idToCheckedOrder(orderId);
                // order = getFromEither(po);
                order = state.idToOrder().get(orderId);
                if (order == null) {
                    throw new HistoryControllerProxyEventException(controllerId, String.format("Unknown OrderId in JControllerState:%s", orderId
                            .string()));
                }
            }
        }

        public JOrder getJOrder() {
            return order;
        }

        public String getOrderId() {
            return orderId.string();
        }

        public State getOrderState() {
            if (order == null) {
                return null;
            }
            try {
                return order.asScala().state();
            } catch (Throwable e) {
                LOGGER.warn(String.format("[%s][getOrderState]%s", getOrderId(), e.toString()), e);
                return null;
            }
        }

        public Set<JOrderObstacle> getOrderObstacles() {
            try {
                JControllerState s = eventAndState.state();
                return getFromEither(s.orderToObstacles(orderId, s.instant()));
            } catch (Throwable e) {
                LOGGER.warn(String.format("[%s][getOrderObstacles]%s", getOrderId(), e.toString()), e);
                return null;
            }
        }

        public List<Date> getWaitingForAdmission() {
            try {
                Set<JOrderObstacle> o = getOrderObstacles();
                if (o != null && o.size() > 0) {
                    return o.parallelStream().filter(e -> (e instanceof JOrderObstacle.WaitingForAdmission)).map(e -> {
                        return HistoryEventEntry.getDate(((WaitingForAdmission) e).asScala().until());
                    }).filter(Objects::nonNull).collect(Collectors.toList());
                }
            } catch (Throwable e) {
                LOGGER.warn(String.format("[%s][getWaitingForAdmission]%s", getOrderId(), e.toString()), e);
            }
            return null;
        }

        public OrderStartedInfo getOrderStartedInfo() {
            return new OrderStartedInfo();
        }

        public boolean isStarted() {
            if (order != null) {
                try {
                    return order.asScala().isStarted();
                } catch (Throwable e) {
                    LOGGER.warn(String.format("[%s][isStarted]%s", getOrderId(), e.toString()), e);
                }
            }
            return false;
        }

        public boolean wasStarted() {
            if (!isStarted()) {
                try {
                    return getCheckedOrderFromPreviousState().isStarted();
                } catch (HistoryControllerProxyEventException e) {
                    LOGGER.warn(String.format("[%s][wasStarted]%s", getOrderId(), e.toString()), e);
                }
            }
            return true;
        }

        public boolean isMarked() {
            if (order != null) {
                try {
                    return OptionConverters.toJava(order.asScala().mark()).isPresent();
                    // maybe without OptionConverters return order.asScala().mark().isDefined();
                    // isMarked has not longer public access: return order.asScala().isMarked();
                } catch (Throwable e) {
                    LOGGER.warn(String.format("[%s][isMarked]%s", getOrderId(), e.toString()), e);
                }
            }
            return false;
        }

        public boolean isSuspended() {
            if (order != null) {
                try {
                    return order.asScala().isSuspended();
                } catch (Throwable e) {
                    LOGGER.warn(String.format("[%s][isSuspended]%s", getOrderId(), e.toString()), e);
                }
            }
            return false;
        }

        public Map<String, Value> getArguments() {
            return order == null ? null : order.arguments();
        }

        public Forked getForked() throws HistoryControllerProxyEventException {
            if (order == null) {
                return null;
            }
            Either<Problem, Forked> pf = order.checkedState(JOrder.forked());
            return getFromEither(pf);
        }

        public List<ForkedChild> getForkedChilds() {
            if (forkedChilds == null) {
                forkedChilds = new ArrayList<HistoryEventEntry.HistoryOrder.ForkedChild>();
                JOrderForked jof = (JOrderForked) getJOrderEvent();
                jof.children().forEach(c -> {
                    if (c.branchId().isPresent()) {
                        OrderId oid = c.orderId();
                        forkedChilds.add(new ForkedChild(oid.string(), c.branchId().get().string()));
                    }
                });
            }
            return forkedChilds;
        }

        public WorkflowInfo getWorkflowInfo() throws Exception {
            if (workflowInfo == null) {
                if (order == null) {
                    throw new Exception(String.format("[%s][%s]missing JOrder", eventId, orderId));
                }
                workflowInfo = new WorkflowInfo(order);
            }
            return workflowInfo;
        }

        public StepInfo getStepInfo() throws Exception {
            if (stepInfo == null) {
                stepInfo = new StepInfo(getWorkflowInfo(), order);
            }
            return stepInfo;
        }

        public OutcomeInfo getOutcomeInfo(Outcome outcome) throws Exception {
            if (outcomeInfo == null) {
                outcomeInfo = new OutcomeInfo(outcome);
            }
            return outcomeInfo;
        }

        private OutcomeInfo newOutcomeInfo(OutcomeType type) {
            return new OutcomeInfo(type, (Problem) null);
        }

        public OutcomeInfo getOutcomeInfo(OutcomeType type, Option<Problem> problem) throws Exception {
            if (outcomeInfo == null) {
                Optional<Problem> op = OptionConverters.toJava(problem);
                if (!op.isPresent()) {
                    outcomeInfo = newOutcomeInfo(type);
                    return outcomeInfo;
                }
                outcomeInfo = new OutcomeInfo(type, op.get());
            }
            return outcomeInfo;
        }

        public OutcomeInfo getOutcomeInfo(Option<Outcome.NotSucceeded> problem) throws Exception {
            if (outcomeInfo == null) {
                if (problem == null) {
                    outcomeInfo = newOutcomeInfo(OutcomeType.failed);
                    return outcomeInfo;
                }
                Optional<Outcome.NotSucceeded> op = OptionConverters.toJava(problem);
                if (!op.isPresent()) {
                    outcomeInfo = newOutcomeInfo(OutcomeType.failed);
                    return outcomeInfo;
                }

                outcomeInfo = new OutcomeInfo(OutcomeType.failed, op.get());
            }
            return outcomeInfo;
        }

        public OutcomeInfo getOutcomeInfoOutcomeAdded() throws Exception {
            if (outcomeInfo == null) {
                outcomeInfo = getOutcomeInfo(((OrderOutcomeAdded) event).outcome());
                if (outcomeInfo != null) {
                    Instruction i = getCurrentPositionInstruction();
                    if (i != null && i instanceof Fail) {
                        outcomeInfo.errorReason = OutcomeTypeFailedReason.fail_instruction;
                    }
                }
            }
            return outcomeInfo;
        }

        public OutcomeInfo getOutcomeInfoFailed() throws Exception {
            if (outcomeInfo == null) {
                JOrderEvent ev = getJOrderEvent();
                if (ev instanceof JOrderFailed) {
                    outcomeInfo = getOutcomeInfo(((JOrderFailed) getJOrderEvent()).outcome());
                }
                if (outcomeInfo != null) {
                    Instruction i = getCurrentPositionInstruction();
                    if (i != null && i instanceof Fail) {
                        outcomeInfo.errorReason = OutcomeTypeFailedReason.fail_instruction;
                    }
                }
            }
            return outcomeInfo;
        }

        public OutcomeInfo getOutcomeInfoFinished() throws Exception {
            if (outcomeInfo == null) {
                Option<Completed> completed = ((OrderFinished) event).outcome();
                if (completed == null) {
                    outcomeInfo = newOutcomeInfo(OutcomeType.succeeded);
                    return outcomeInfo;
                }
                Optional<Completed> op = OptionConverters.toJava(completed);
                if (!op.isPresent()) {
                    outcomeInfo = newOutcomeInfo(OutcomeType.succeeded);
                    return outcomeInfo;
                }

                outcomeInfo = new OutcomeInfo(op.get());
                if (outcomeInfo.isFailed) {
                    Instruction i = getCurrentPositionInstruction();
                    if (i != null && i instanceof Finish) {
                        outcomeInfo.errorReason = OutcomeTypeFailedReason.finish_instruction;
                    }
                }
            }
            return outcomeInfo;
        }

        public Instruction getCurrentPositionInstruction() throws Exception {
            WorkflowInfo wi = getWorkflowInfo();
            if (wi != null) {
                JWorkflow w = wi.getWorkflow();
                if (w != null) {
                    return w.asScala().instruction(wi.getPosition().getUnderlying().asScala());
                }
            }
            return null;
        }

        // if expected notice(s) exists
        public FatExpectNotices readNotices() throws Exception {
            Instruction i = getCurrentPositionInstruction();
            if (i != null && i instanceof ExpectNotices) {
                return new FatExpectNotices((ExpectNotices) i);
            }
            return null;
        }

        // if expected notice(s) not exist
        public List<FatExpectNotice> getExpectNotices() throws Exception {
            // OrderNoticesExpected ev = (OrderNoticesExpected) event;
            // List<JExpectedNotice> l = JavaConverters.asJava(ev.expected()).stream().map(e -> new JExpectedNotice(e)).collect(Collectors.toList());
            List<JExpectedNotice> l = state.orderToStillExpectedNotices(order.id());

            List<FatExpectNotice> r = new ArrayList<>();
            if (l != null && l.size() > 0) {
                for (JExpectedNotice en : l) {
                    r.add(new FatExpectNotice(en.noticeId().string(), en.boardPath().string()));
                }
            }
            return r;
        }

        public List<FatExpectNotice> getConsumingNotices() throws Exception {
            OrderNoticesConsumptionStarted ev = (OrderNoticesConsumptionStarted) event;
            List<JExpectedNotice> l = JavaConverters.asJava(ev.consumptions()).stream().map(e -> new JExpectedNotice(e)).collect(Collectors.toList());
            List<FatExpectNotice> r = new ArrayList<>();
            if (l != null && l.size() > 0) {
                for (JExpectedNotice en : l) {
                    r.add(new FatExpectNotice(en.noticeId().string(), en.boardPath().string()));
                }
            }
            return r;
        }

        public FatPostNotice getPostNotice() {
            if (event instanceof OrderNoticePosted) {
                Notice n = ((OrderNoticePosted) event).notice();
                if (n != null) {
                    return new FatPostNotice(n);
                }
            }
            return null;
        }

        public List<OrderLock> getOrderLocks(OrderLocksAcquired event) throws HistoryControllerProxyEventException {
            // why FatEventProblemException is not a runtime exception
            // return JavaConverters.asJava(event.demands()).stream().map(ld -> getOrderLock(ld.lockPath(), ld.count(), false)).collect(Collectors.toList());
            List<OrderLock> result = new ArrayList<>();
            for (LockDemand ld : JavaConverters.asJava(event.demands())) {
                result.add(getOrderLock(ld.lockPath(), ld.count(), false));
            }
            return result;
        }

        public List<OrderLock> getOrderLocks(OrderLocksQueued event) throws HistoryControllerProxyEventException {
            // why FatEventProblemException is not a runtime exception
            // return JavaConverters.asJava(event.demands()).stream().map(ld -> getOrderLock(ld.lockPath(), ld.count(), true)).collect(Collectors.toList());
            List<OrderLock> result = new ArrayList<>();
            for (LockDemand ld : JavaConverters.asJava(event.demands())) {
                result.add(getOrderLock(ld.lockPath(), ld.count(), true));
            }
            return result;
        }

        public List<OrderLock> getOrderLocks(OrderLocksReleased event) throws HistoryControllerProxyEventException {
            // count not available
            // why FatEventProblemException is not a runtime exception
            // return JavaConverters.asJava(event.lockPaths()).stream().map(lp -> getOrderLock(lp, null, false)).collect(Collectors.toList());
            List<OrderLock> result = new ArrayList<>();
            for (LockPath lp : JavaConverters.asJava(event.lockPaths())) {
                result.add(getOrderLock(lp, null, false));
            }
            return result;
        }

        private OrderLock getOrderLock(LockPath lockPath, Option<Object> count, boolean checkState) throws HistoryControllerProxyEventException {
            Lock l = null;
            Collection<OrderId> orderIds = null;
            List<OrderId> queuedOrderIds = null;
            if (checkState) {
                JLockState jl = getFromMap(state.pathToLockState().get(lockPath), lockPath.string());
                l = jl.lock();
                orderIds = jl.orderIds();
                queuedOrderIds = jl.queuedOrderIds();
            } else {
                JLock jl = getFromMap(state.pathToLock().get(lockPath), lockPath.string());
                l = jl.asScala();
            }
            return new OrderLock(l.path().string(), l.limit(), count == null ? null : OptionConverters.toJava(count), orderIds, queuedOrderIds);
        }

        public class OrderStartedInfo {

            private Date scheduledFor;
            private boolean maybePreviousStatesLogged;

            private OrderStartedInfo() {
                try {
                    JOrder jo = getCheckedOrderFromPreviousState().getJOrder();
                    this.scheduledFor = getScheduledFor(jo);
                    this.maybePreviousStatesLogged = true;// maybePreviousStatesLogged(jo);
                } catch (Throwable e) {
                    LOGGER.warn(String.format("[%s][OrderStarted]%s", getOrderId(), e.toString()), e);
                }
            }

            private Date getScheduledFor(JOrder jo) {
                if (jo != null) {
                    try {
                        Optional<Instant> ot = jo.scheduledFor();
                        return ot.isPresent() ? Date.from(ot.get()) : null;
                    } catch (Throwable e) {
                        LOGGER.warn(String.format("[%s][getScheduledFor]%s", getOrderId(), e.toString()), e);
                    }
                }
                return null;
            }

            @SuppressWarnings("unused")
            private boolean maybePreviousStatesLogged(JOrder jo) {
                if (jo != null) {
                    try {
                        // Moved/Skipped before OrderStarted
                        // TODO check previous event instead of position, because can be skipped by 0/try+0:1 etc
                        String position = getWorkflowInfo().getPosition().asString();
                        if (position != null && !position.equals("0")) {// problem: true for 0/try:0:0 etc ... or an order starts with a position > 0...
                            return true;
                        }
                        // Suspended/Stopped and Resumed before OrderStarted
                        Order<State> os = jo.asScala();
                        return os.isResumed();
                    } catch (Throwable e) {
                        LOGGER.warn(String.format("[%s][maybePreviousStateLogged]%s", getOrderId(), e.toString()), e);
                    }
                }
                return false;
            }

            public Date getScheduledFor() {
                return scheduledFor;
            }

            public boolean maybePreviousStatesLogged() {
                return maybePreviousStatesLogged;
            }

        }

        public class OrderLock {

            private final String lockId;
            private final int limit;
            private final Integer count;
            private OrderLockState state;

            private OrderLock(String lockId, int limit, Optional<Object> count, Collection<OrderId> orderIds, List<OrderId> queuedOrderIds) {
                this.lockId = lockId;
                this.limit = limit;
                this.count = count != null && count.isPresent() ? (Integer) count.get() : null;
                this.state = null;
                if (orderIds != null || queuedOrderIds != null) {
                    this.state = new OrderLockState(orderIds, queuedOrderIds);
                }
            }

            public class OrderLockState {

                private final List<String> orderIds;
                private final List<String> queuedOrderIds;

                private OrderLockState(Collection<OrderId> orderIds, List<OrderId> queuedOrderIds) {
                    this.orderIds = orderIds == null ? null : orderIds.stream().map(o -> o.string()).collect(Collectors.toList());
                    this.queuedOrderIds = queuedOrderIds == null ? null : queuedOrderIds.stream().map(o -> o.string()).collect(Collectors.toList());
                }

                public List<String> getOrderIds() {
                    return orderIds;
                }

                public List<String> getQueuedOrderIds() {
                    return queuedOrderIds;
                }

            }

            public String getLockId() {
                return lockId;
            }

            public int getLimit() {
                return limit;
            }

            public Integer getCount() {
                return count;
            }

            public OrderLockState getState() {
                return state;
            }
        }

        public class ForkedChild {

            private final String key;
            private final String branchId;

            private ForkedChild(String key, String branchId) {
                this.key = key;
                this.branchId = branchId;
            }

            public String getKey() {
                return key;
            }

            public String getBranchId() {
                return branchId;
            }
        }

        public class OutcomeInfo {

            private Integer returnCode;
            private boolean isSucceeded;
            private boolean isFailed;
            private Map<String, Value> namedValues;
            private OutcomeType type;
            private OutcomeTypeFailedReason errorReason;
            private String errorCode;
            private String errorMessage;

            private OutcomeInfo(Outcome outcome) {
                if (outcome instanceof Completed) {
                    Completed c = (Completed) outcome;
                    isSucceeded = c.isSucceeded();
                    isFailed = !isSucceeded; // c.isFailed();
                    handleNamedValues(c);
                    if (isFailed) {
                        type = OutcomeType.failed;
                        if (outcome instanceof Failed) {
                            setError((Failed) outcome);
                        } else {
                            LOGGER.warn(String.format("[not handled failed type]%s", SOSString.toString(outcome)));
                        }
                    } else {
                        if (returnCode == null) {
                            returnCode = 0;
                        }
                        type = OutcomeType.succeeded;
                    }
                } else if (outcome instanceof Disrupted) {
                    handleDisrupted(outcome, (Disrupted) outcome);
                } else if (outcome instanceof Killed) {
                    handleKilled(outcome, (Killed) outcome);
                } else if (outcome instanceof TimedOut) {
                    handleTimedOut(outcome, (TimedOut) outcome);
                }
            }

            private OutcomeInfo(OutcomeType type, Problem problem) {
                this.type = type;
                setError(problem);
                switch (type) {
                case succeeded:
                    if (!isSucceeded && !isFailed) {
                        isSucceeded = true;
                    }
                    break;
                case failed:
                case broken:
                    returnCode = null;// TODO ?
                    isSucceeded = false;
                    isFailed = true;
                    break;
                default:
                    break;
                }
            }

            private OutcomeInfo(OutcomeType type, Outcome.NotSucceeded problem) {
                this.type = type;
                if (problem instanceof Disrupted) {
                    handleDisrupted(null, (Disrupted) problem);
                } else if (problem instanceof Failed) {
                    handleFailed((Failed) problem);
                }
            }

            private void handleNamedValues(Completed c) {
                if (c.namedValues() != null) {
                    namedValues = JavaConverters.asJava(c.namedValues());
                    try {
                        Value vrt = namedValues.get(NAMED_NAME_RETURN_CODE);
                        if (vrt != null) {
                            String rt = vrt.toString();
                            if (!SOSString.isEmpty(rt) && !rt.equals("\"\"")) {
                                returnCode = Integer.parseInt(rt.toString());
                            }
                        }
                    } catch (Throwable e) {
                        String pos = "";
                        try {
                            pos = "[" + getWorkflowInfo().getPosition().asString() + "]";
                        } catch (Throwable ex) {
                        }
                        LOGGER.warn(String.format("[%s][%s]%s[isSucceeded=%s][can't extract returnCode][%s]", getOrderId(), eventType, pos,
                                isSucceeded, SOSString.toString(namedValues), e.toString()), e);
                    }
                }
            }

            private void handleFailed(Failed failed) {
                isSucceeded = failed.isSucceeded();
                isFailed = !isSucceeded;
                type = OutcomeType.failed;
                handleNamedValues(failed);
                setError(failed);
            }

            private void handleDisrupted(Outcome outcome, Disrupted problem) {
                returnCode = null; // TODO ?
                isSucceeded = problem.isSucceeded();
                isFailed = !isSucceeded; // problem.isFailed();
                type = OutcomeType.disrupted;
                if (isFailed) {
                    try {
                        setError(problem.reason().problem());
                    } catch (Throwable e) {
                        LOGGER.warn(e.toString(), e);
                    }

                    if (SOSString.isEmpty(errorMessage) && outcome != null && outcome instanceof Failed) {
                        setError((Failed) outcome);
                    }
                }
            }

            private void handleKilled(Outcome outcome, Killed problem) {
                returnCode = null;
                isSucceeded = problem.isSucceeded();
                isFailed = !isSucceeded; // problem.isFailed();
                type = OutcomeType.killed;

                if (isFailed) {
                    Completed o = problem.outcome();
                    if (o != null && o instanceof Failed) {
                        handleFailed((Failed) o);
                        type = OutcomeType.killed;
                    }
                }
            }

            private void handleTimedOut(Outcome outcome, TimedOut problem) {
                returnCode = null;
                isSucceeded = problem.isSucceeded();
                isFailed = !isSucceeded; // problem.isFailed();
                type = OutcomeType.timedout;

                if (isFailed) {
                    Completed o = problem.outcome();
                    if (o != null && o instanceof Failed) {
                        handleFailed((Failed) o);
                        type = OutcomeType.timedout;
                    }
                }
            }

            private void setError(Failed failed) {
                Optional<String> em = OptionConverters.toJava(failed.errorMessage());
                if (em.isPresent()) {
                    errorMessage = em.get();
                }
            }

            private void setError(Problem problem) {
                if (problem != null) {
                    errorMessage = problem.message();
                    if (problem.codeOrNull() != null) {
                        errorCode = problem.codeOrNull().toString();
                    }
                }
            }

            public Integer getReturnCode() {
                return returnCode;
            }

            public boolean isSucceeded() {
                return isSucceeded;
            }

            public boolean isFailed() {
                return isFailed;
            }

            public String getErrorMessage() {
                return errorMessage;
            }

            public String getErrorCode() {
                return errorCode;
            }

            public Map<String, Value> getNamedValues() {
                return namedValues;
            }

            public OutcomeType getType() {
                return type;
            }

            public OutcomeTypeFailedReason getErrorReason() {
                return errorReason;
            }

            public FatOutcome toFatOutcome() {
                return new FatOutcome(this);
            }
        }

        public class StepInfo {

            private final WorkflowInfo workflowInfo;
            private final JOrder order;
            private AgentInfo agentInfo;
            private String jobName;
            private String jobLabel;

            public StepInfo(WorkflowInfo workflowInfo, JOrder order) {
                this.workflowInfo = workflowInfo;
                this.order = order;
            }

            public AgentInfo getAgentInfo() throws Exception {
                if (agentInfo == null) {
                    if (order == null) {
                        throw new Exception(String.format("[%s][%s]missing JOrder", eventId, orderId));
                    }
                    agentInfo = new AgentInfo(state, order);
                }
                return agentInfo;
            }

            public String getJobName() throws Exception {
                if (jobName == null) {
                    Either<Problem, Name> pn = workflowInfo.getWorkflow().checkedJobName(workflowInfo.getPosition().getUnderlying());
                    jobName = getFromEither(pn).toString();
                }
                return jobName;
            }

            public String getJobLabel() throws Exception {
                if (jobLabel == null) {
                    JWorkflow workflow = workflowInfo.getWorkflow();
                    try {
                        jobLabel = workflow.asScala().labeledInstruction(workflowInfo.getPosition().getUnderlying().asScala()).toOption().get()
                                .maybeLabel().get().string();
                    } catch (Throwable e) {
                        LOGGER.warn(e.toString(), e);
                    }
                }
                return jobLabel;
            }
        }

        public class WorkflowInfo {

            private final JWorkflowId workflowId;
            private final String path;
            private final String versionId;
            private final Position position;

            private JWorkflow workflow;

            public WorkflowInfo(JOrder order) {
                JWorkflowPosition wp = order.workflowPosition();

                workflowId = wp.workflowId();
                path = workflowId.path().string();
                versionId = workflowId.versionId().string();
                position = new Position(wp.position());

            }

            public JWorkflow getWorkflow() throws Exception {
                if (workflow == null) {
                    if (state == null) {
                        throw new Exception(String.format("[%s][%s]missing JControllerState", eventId, orderId));
                    }
                    if (workflowId == null) {
                        throw new Exception(String.format("[%s][%s]missing workflowId", eventId, orderId));
                    }
                    workflow = getFromEither(state.repo().idToCheckedWorkflow(workflowId));
                }
                return workflow;
            }

            public JWorkflowId getWorkflowId() {
                return workflowId;
            }

            public String getPath() {
                return path;
            }

            public String getVersionId() {
                return versionId;
            }

            public Position getPosition() {
                return position;
            }

            public Position createNewPosition(List<Object> positions) {
                Either<Problem, JPosition> p = JPosition.fromList(positions);
                try {
                    return new Position(getFromEither(p));
                } catch (Throwable e) {
                    return null;
                }
            }

            public class Position {

                private final JPosition underlying;

                public Position(JPosition p) {
                    underlying = p;
                }

                public JPosition getUnderlying() {
                    return underlying;
                }

                public String asString() {
                    return underlying.toString();
                }
            }
        }

    }

    public class HistoryClusterCoupled {

        private String activeId;
        private boolean isPrimary = true;// TODO remove after test ...

        public HistoryClusterCoupled() {
            ClusterCoupled ev = (ClusterCoupled) event;
            try {
                NodeId nid = ev.activeId();
                activeId = nid.string();
                isPrimary = !activeId.equalsIgnoreCase("backup");
            } catch (Throwable e) {

            }
        }

        public String getActiveId() {
            return activeId;
        }

        public boolean isPrimary() {
            return isPrimary;
        }
    }

    public class HistoryControllerReady {

        private final String timezone;
        private final Duration totalRunningTime;

        public HistoryControllerReady() {
            ControllerReady ev = (ControllerReady) event;
            timezone = ev.timezone().string();
            totalRunningTime = getDuration(ev.totalRunningTime());
        }

        public String getTimezone() {
            return timezone;
        }

        public Duration getTotalRunningTime() {
            return totalRunningTime;
        }

        public Long getTotalRunningTimeAsMillis() {
            return Long.valueOf(totalRunningTime == null ? 0 : totalRunningTime.toMillis());
        }
    }

    public class HistoryAgentCouplingFailed {

        private final String id;
        private String message;

        public HistoryAgentCouplingFailed() {
            AgentPath arp = (AgentPath) keyedEvent.key();
            id = arp.string();

            Problem p = ((AgentCouplingFailed) event).problem();
            if (p != null) {
                message = p.message();
            }
        }

        public String getId() {
            return id;
        }

        public String getMessage() {
            return message;
        }
    }

    public class HistoryAgentShutDown {

        private final String id;

        public HistoryAgentShutDown() {
            AgentPath arp = (AgentPath) keyedEvent.key();
            id = arp.string();
        }

        public String getId() {
            return id;
        }
    }

    /** only post event - not used by the history */
    public class HistoryAgentSubagentDedicated {

        public HistoryAgentSubagentDedicated() {
        }

        public void postEvent() {
            try {
                SubagentDedicated ev = (SubagentDedicated) event;

                SubagentId subAgentId = (SubagentId) keyedEvent.key();
                JSubagentItem subAgentItem = eventAndState.state().idToSubagentItem().get(subAgentId);
                if (subAgentItem != null) {
                    OptionConverters.toJava(ev.platformInfo()).ifPresent(p -> EventBus.getInstance().post(new SubagentVersionUpdatedEvent(subAgentItem
                            .agentPath().string(), subAgentId.string(), p.js7Version().string(), p.java().version())));
                }
            } catch (Throwable e) {
                LOGGER.error(String.format("[postEvent][HistoryAgentSubagentDedicated]%s", e.toString()), e);
            }
        }
    }

    public class HistoryAgentReady {

        private final String id;
        private final String timezone;
        private String uri;
        // private Map<String, String> subAgents;

        public HistoryAgentReady() throws HistoryControllerProxyEventException {
            AgentReady ev = (AgentReady) event;
            timezone = ev.timezone();

            AgentPath arp = (AgentPath) keyedEvent.key();
            id = arp.string();

            // subAgents = new HashMap<>();
            JAgentRef ar = getFromMap(eventAndState.state().pathToAgentRef().get(arp), id);
            if (ar != null) {
                // if (ar.uri().isPresent()) {// single agent
                // uri = ar.uri().get().string();
                // } else {// agent cluster
                //Optional<SubagentId> director = ar.director();// ar.directors();
                if (!ar.directors().isEmpty()) {
                    Map<SubagentId, JSubagentItem> map = eventAndState.state().idToSubagentItem();
//                    SubagentId directorId = ar.directors().get(0);
//                    JSubagentItem sar = map.get(directorId);
//                    if (sar != null) {
//                        uri = sar.uri().string();
//                    }
                    // if (map != null) {
                    // map.entrySet().stream().forEach(e -> {
                    // // subAgents.put(e.getKey().string(), e.getValue().uri().string());
                    // if (e.getKey().equals(directorId)) {
                    // uri = e.getValue().uri().string();
                    // }
                    // });
                    // }
                    
                    ar.directors().stream().map(sa -> map.get(sa)).filter(Objects::nonNull).findFirst().map(JSubagentItem::uri).map(Uri::string)
                        .ifPresent(s -> uri = s);
                }
                // }
            }
            if (uri == null) {
                uri = "unknown";
            }
        }

        public void postEvent() {
            try {
                AgentReady ev = (AgentReady) event;
                OptionConverters.toJava(ev.platformInfo()).ifPresent(p -> EventBus.getInstance().post(new AgentVersionUpdatedEvent(id, p.js7Version()
                        .string(), p.java().version())));
            } catch (Throwable e) {
                LOGGER.error(String.format("[postEvent][AgentVersionUpdatedEvent][agent id=%s]%s", id, e.toString()), e);
            }
        }

        public String getTimezone() {
            return timezone;
        }

        public String getId() {
            return id;
        }

        public String getUri() {
            return uri;
        }
    }

    public class AgentInfo {

        private String agentId;
        private String agentUri;
        private String subagentId;

        public AgentInfo(JControllerState state, JOrder order) {
            if (order != null) {
                try {
                    Either<Problem, AgentPath> pap = order.attached();
                    AgentPath ap = getFromEither(pap);
                    agentId = ap.string();
                } catch (Throwable e) {
                    LOGGER.error(String.format("[order id=%s][evaluate agentId]%s", order.id(), e.toString()), e);
                }
                try {
                    Optional<SubagentId> sid = OptionConverters.toJava(((Order.Processing) order.asScala().state()).subagentId());
                    if (sid.isPresent()) {
                        subagentId = sid.get().string();
                        if (agentId != null && subagentId.equals(agentId + "-1")) { // delete Joacim's unexpected default for implicit directors
                            subagentId = subagentId.replaceFirst("-1$", "");
                        }
                        JSubagentItem si = state.idToSubagentItem().get(sid.get());
                        if (si != null) {
                            agentUri = si.uri().string();
                        }
                    }
                } catch (Throwable e) {
                    LOGGER.error(String.format("[order id=%s][evaluate subAgentId]%s", order.id(), e.toString()), e);
                }

                if (subagentId == null) {
                    subagentId = agentId;
                }
            }
        }

        public String getAgentId() {
            return agentId;
        }

        public String getAgentUri() {
            return agentUri;
        }

        public String getSubagentId() {
            return subagentId;
        }
    }

    private <T> T getFromEither(Either<Problem, T> either) throws HistoryControllerProxyEventException {
        if (either.isLeft()) {
            throw new HistoryControllerProxyEventException(controllerId, either.getLeft());
        }
        return either.get();
    }

    private <T> T getFromMap(T o, String name) throws HistoryControllerProxyEventException {
        if (o == null) {
            throw new HistoryControllerProxyEventException(controllerId, Problem.of("Object '" + name + "' doesn't exist."));
        }
        return o;
    }

    private Duration getDuration(FiniteDuration fd) {
        return fd == null ? null : Duration.ofNanos(fd.toNanos());
    }
}
