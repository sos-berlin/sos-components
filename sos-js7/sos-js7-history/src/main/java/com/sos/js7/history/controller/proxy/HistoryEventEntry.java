package com.sos.js7.history.controller.proxy;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.js7.history.controller.exception.FatEventProblemException;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.agent.AgentPath;
import js7.data.agent.AgentRefStateEvent.AgentCouplingFailed;
import js7.data.agent.AgentRefStateEvent.AgentReady;
import js7.data.controller.ControllerEvent.ControllerReady;
import js7.data.event.Event;
import js7.data.event.KeyedEvent;
import js7.data.event.Stamped;
import js7.data.lock.Lock;
import js7.data.lock.LockPath;
import js7.data.order.OrderEvent;
import js7.data.order.OrderEvent.OrderLockAcquired;
import js7.data.order.OrderEvent.OrderLockQueued;
import js7.data.order.OrderEvent.OrderLockReleased;
import js7.data.order.OrderId;
import js7.data.order.Outcome;
import js7.data.order.Outcome.Completed;
import js7.data.order.Outcome.Disrupted;
import js7.data.order.Outcome.Failed;
import js7.data.value.Value;
import js7.data.workflow.instructions.executable.WorkflowJob.Name;
import js7.data_for_java.agent.JAgentRef;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.lock.JLock;
import js7.data_for_java.lock.JLockState;
import js7.data_for_java.order.JOrder;
import js7.data_for_java.order.JOrder.Forked;
import js7.data_for_java.order.JOrderEvent;
import js7.data_for_java.order.JOrderEvent.JOrderForked;
import js7.data_for_java.workflow.JWorkflow;
import js7.data_for_java.workflow.JWorkflowId;
import js7.data_for_java.workflow.position.JPosition;
import js7.data_for_java.workflow.position.JWorkflowPosition;
import js7.proxy.javaapi.data.controller.JEventAndControllerState;
import scala.Option;
import scala.collection.JavaConverters;
import scala.jdk.javaapi.OptionConverters;

public class HistoryEventEntry {

    public static enum OutcomeType {
        succeeded, failed, disrupted, broken
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryEventEntry.class);

    private static final String NAMED_NAME_RETURN_CODE = "returnCode";

    private final JEventAndControllerState<Event> eventAndState;
    private final KeyedEvent<Event> keyedEvent;
    private final Event event;
    private final Long eventId;
    private final Date eventDate;
    private final HistoryEventType eventType;

    public HistoryEventEntry(JEventAndControllerState<Event> es) {
        eventAndState = es;

        Stamped<KeyedEvent<Event>> stampedEvent = eventAndState.stampedEvent();

        keyedEvent = stampedEvent.value();
        event = keyedEvent.event();
        eventId = stampedEvent.eventId();
        eventDate = Date.from(stampedEvent.timestamp().toInstant());

        eventType = HistoryEventType.fromValue(event.getClass().getSimpleName());
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

    public HistoryControllerReady getControllerReady() {
        return new HistoryControllerReady();
    }

    public HistoryAgentCouplingFailed getAgentCouplingFailed() throws FatEventProblemException {
        return new HistoryAgentCouplingFailed();
    }

    public HistoryAgentReady getAgentReady() throws FatEventProblemException {
        return new HistoryAgentReady();
    }

    public HistoryOrder getOrder() throws FatEventProblemException {
        return new HistoryOrder();
    }

    public HistoryOrder getCheckedOrder() throws FatEventProblemException {
        return new HistoryOrder(eventAndState.state());
    }

    public HistoryOrder getCheckedOrderFromPreviousState() throws FatEventProblemException {
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

        private HistoryOrder() throws FatEventProblemException {
            this(null);
        }

        private HistoryOrder(JControllerState controllerState) throws FatEventProblemException {
            orderId = (OrderId) keyedEvent.key();
            if (controllerState != null) {
                state = controllerState;
                Either<Problem, JOrder> po = state.idToCheckedOrder(orderId);
                order = getFromEither(po);
            }
        }

        public String getOrderId() {
            return orderId.string();
        }

        public Date getScheduledFor() {
            if (order != null) {
                try {
                    Optional<Instant> ot = order.scheduledFor();
                    return ot.isPresent() ? Date.from(ot.get()) : null;
                } catch (Throwable e) {
                    LOGGER.warn(String.format("[%s][getScheduledFor]%s", getOrderId(), e.toString()), e);
                }
            }
            return null;
        }

        public Boolean isStarted() {
            if (order != null) {
                try {
                    return order.asScala().isStarted();
                } catch (Throwable e) {
                    LOGGER.warn(String.format("[%s][isStarted]%s", getOrderId(), e.toString()), e);
                }
            }
            return null;
        }

        public Map<String, Value> getArguments() {
            return order == null ? null : order.arguments();
        }

        public Forked getForked() throws FatEventProblemException {
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
                    OrderId oid = c.orderId();
                    forkedChilds.add(new ForkedChild(oid.string(), c.branchId().string()));
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

        public OutcomeInfo getOutcomeInfo(OutcomeType type, Problem problem) throws Exception {
            if (outcomeInfo == null) {
                outcomeInfo = new OutcomeInfo(type, problem);
            }
            return outcomeInfo;
        }

        public OutcomeInfo getOutcomeInfo(Option<Outcome.NotSucceeded> problem) throws Exception {
            if (problem == null) {
                return getOutcomeInfo(OutcomeType.failed, null);
            }
            Optional<Outcome.NotSucceeded> op = OptionConverters.toJava(problem);
            if (!op.isPresent()) {
                return getOutcomeInfo(OutcomeType.failed, null);
            }
            if (outcomeInfo == null) {
                outcomeInfo = new OutcomeInfo(OutcomeType.failed, op.get());
            }
            return outcomeInfo;
        }

        public OrderLock getOrderLock(OrderLockAcquired event) throws FatEventProblemException {
            return getOrderLock(event.lockPath(), event.count(), false);
        }

        public OrderLock getOrderLock(OrderLockQueued event) throws FatEventProblemException {
            return getOrderLock(event.lockPath(), event.count(), true);
        }

        public OrderLock getOrderLock(OrderLockReleased event) throws FatEventProblemException {
            // count not available
            return getOrderLock(event.lockPath(), null, false);
        }

        private OrderLock getOrderLock(LockPath lockPath, Option<Object> count, boolean checkState) throws FatEventProblemException {
            Lock l = null;
            Collection<OrderId> orderIds = null;
            List<OrderId> queuedOrderIds = null;
            if (checkState) {
                JLockState jl = getFromEither(state.pathToLockState(lockPath));
                l = jl.lock();
                orderIds = jl.orderIds();
                queuedOrderIds = jl.queuedOrderIds();
            } else {
                JLock jl = getFromEither(state.pathToLock(lockPath));
                l = jl.asScala();
            }
            return new OrderLock(l.path().string(), l.limit(), count == null ? null : OptionConverters.toJava(count), orderIds, queuedOrderIds);
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
            private String errorCode;
            private String errorMessage;

            private OutcomeInfo(Outcome outcome) {
                if (outcome instanceof Completed) {
                    Completed c = (Completed) outcome;

                    if (c.namedValues() != null) {
                        namedValues = JavaConverters.asJava(c.namedValues());
                        try {
                            Value rt = namedValues.get(NAMED_NAME_RETURN_CODE);
                            if (rt != null) {
                                returnCode = Integer.parseInt(rt.toString());
                            }
                        } catch (Throwable e) {
                            LOGGER.error(String.format("[can't extract returnCode][%s]", SOSString.toString(namedValues), e.toString()), e);
                        }
                    }

                    isSucceeded = c.isSucceeded();
                    isFailed = c.isFailed();
                    if (isFailed) {
                        type = OutcomeType.failed;
                        if (outcome instanceof Failed) {
                            Optional<String> em = OptionConverters.toJava(((Failed) outcome).errorMessage());
                            if (em.isPresent()) {
                                errorMessage = em.get();
                            }
                        } else {
                            LOGGER.warn(String.format("[not handled failed type]%s", SOSString.toString(outcome)));
                        }
                    } else {
                        if (returnCode != null) {
                            returnCode = 0;
                        }
                        type = OutcomeType.succeeded;
                    }
                } else if (outcome instanceof Disrupted) {
                    handleDisrupted(outcome, (Disrupted) outcome);
                }
            }

            private OutcomeInfo(OutcomeType type, Problem problem) {
                this.type = type;
                setError(problem);
                switch (type) {
                case failed:
                case broken:
                    returnCode = null;
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
                }
            }

            private void handleDisrupted(Outcome outcome, Disrupted problem) {
                returnCode = null;
                isSucceeded = problem.isSucceeded();
                isFailed = problem.isFailed();
                type = OutcomeType.disrupted;
                if (isFailed) {
                    try {
                        setError(problem.reason().problem());
                    } catch (Throwable e) {
                        LOGGER.warn(e.toString(), e);
                    }

                    if (SOSString.isEmpty(errorMessage) && outcome != null && outcome instanceof Failed) {
                        Optional<String> em = OptionConverters.toJava(((Failed) outcome).errorMessage());
                        if (em.isPresent()) {
                            errorMessage = em.get();
                        }
                    }
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
        }

        public class StepInfo {

            private final WorkflowInfo workflowInfo;
            private final JOrder order;
            private String agentId;
            private String jobName;

            public StepInfo(WorkflowInfo workflowInfo, JOrder order) {
                this.workflowInfo = workflowInfo;
                this.order = order;
            }

            public String getAgentId() throws Exception {
                if (agentId == null) {
                    if (order == null) {
                        throw new Exception(String.format("[%s][%s]missing JOrder", eventId, orderId));
                    }

                    Either<Problem, AgentPath> pa = order.attached();
                    AgentPath arp = getFromEither(pa);
                    agentId = arp.string();
                }
                return agentId;
            }

            public String getJobName() throws Exception {
                if (jobName == null) {
                    if (state == null) {
                        throw new Exception(String.format("[%s][%s]missing JControllerState", eventId, orderId));
                    }
                    if (workflowInfo == null) {
                        throw new Exception(String.format("[%s][%s]missing WorkflowInfo", eventId, orderId));
                    }

                    Either<Problem, JWorkflow> pw = state.repo().idToWorkflow(workflowInfo.getWorkflowId());
                    JWorkflow workflow = getFromEither(pw);

                    Either<Problem, Name> pn = workflow.checkedJobName(workflowInfo.getPosition().getUnderlying());
                    jobName = getFromEither(pn).toString();
                }
                return jobName;
            }
        }

        public class WorkflowInfo {

            private final JWorkflowId workflowId;
            private final String path;
            private final String versionId;
            private final Position position;

            public WorkflowInfo(JOrder order) {
                JWorkflowPosition wp = order.workflowPosition();

                workflowId = wp.workflowId();
                path = workflowId.path().string();
                versionId = workflowId.versionId().string();
                position = new Position(wp.position());
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

    public class HistoryControllerReady {

        private final String timezone;

        public HistoryControllerReady() {
            timezone = ((ControllerReady) event).timezone();
        }

        public String getTimezone() {
            return timezone;
        }

    }

    public class HistoryAgentCouplingFailed {

        private final String id;
        private String message;

        public HistoryAgentCouplingFailed() throws FatEventProblemException {
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

    public class HistoryAgentReady {

        private final String id;
        private final String timezone;
        private String uri;

        public HistoryAgentReady() throws FatEventProblemException {
            timezone = ((AgentReady) event).timezone();

            AgentPath arp = (AgentPath) keyedEvent.key();
            id = arp.string();

            Either<Problem, JAgentRef> pa = eventAndState.state().pathToAgentRef(arp);
            JAgentRef ar = getFromEither(pa);
            if (ar != null) {
                uri = ar.uri().toString();
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

    private <T> T getFromEither(Either<Problem, T> either) throws FatEventProblemException {
        if (either.isLeft()) {
            throw new FatEventProblemException(either.getLeft());
        }
        return either.get();
    }
}
