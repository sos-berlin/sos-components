package com.sos.js7.history.controller.proxy;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.js7.event.controller.EventMeta;
import com.sos.js7.history.controller.exception.FatEventProblemException;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.controller.data.events.ControllerAgentEvent.AgentReady;
import js7.controller.data.events.ControllerEvent.ControllerReady;
import js7.data.agent.AgentRefPath;
import js7.data.event.Event;
import js7.data.event.KeyedEvent;
import js7.data.event.Stamped;
import js7.data.job.ReturnCode;
import js7.data.order.OrderEvent;
import js7.data.order.OrderId;
import js7.data.order.Outcome;
import js7.data.order.Outcome.Completed;
import js7.data.order.Outcome.Disrupted;
import js7.data.order.Outcome.Failed;
import js7.data.workflow.instructions.executable.WorkflowJob.Name;
import js7.proxy.javaapi.data.agent.JAgentRef;
import js7.proxy.javaapi.data.controller.JControllerState;
import js7.proxy.javaapi.data.controller.JEventAndControllerState;
import js7.proxy.javaapi.data.order.JOrder;
import js7.proxy.javaapi.data.order.JOrder.Forked;
import js7.proxy.javaapi.data.order.JOrderEvent;
import js7.proxy.javaapi.data.order.JOrderEvent.JOrderForked;
import js7.proxy.javaapi.data.workflow.JWorkflow;
import js7.proxy.javaapi.data.workflow.JWorkflowId;
import js7.proxy.javaapi.data.workflow.position.JPosition;
import js7.proxy.javaapi.data.workflow.position.JWorkflowPosition;
import scala.collection.JavaConverters;
import scala.jdk.javaapi.OptionConverters;

public class HistoryEventEntry {

    public static enum OutcomeType {
        succeeded, failed, disrupted
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryEventEntry.class);

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

    public Date getEventIdAsDate() {
        return eventId == null ? null : Date.from(EventMeta.eventId2Instant(eventId));
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

        public Map<String, String> getArguments() {
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

            private int returnCode;
            private boolean isSuccessReturnCode;
            private boolean isSucceeded;
            private boolean isFailed;
            private Map<String, String> keyValues;
            private OutcomeType type;
            private String errorMessage;

            private OutcomeInfo(Outcome outcome) {
                if (outcome instanceof Completed) {
                    Completed c = (Completed) outcome;
                    ReturnCode rc = c.returnCode();

                    returnCode = rc.number();
                    isSuccessReturnCode = rc.isSuccess();

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
                        type = OutcomeType.succeeded;
                    }
                    keyValues = JavaConverters.asJava(c.keyValues());
                } else if (outcome instanceof Disrupted) {
                    Disrupted c = (Disrupted) outcome;
                    isSucceeded = c.isSucceeded();
                    isFailed = c.isFailed();
                    type = OutcomeType.disrupted;
                    if (isFailed) {
                        try {
                            Problem p = c.reason().problem();
                            if (p != null) {
                                if (p.codeOrNull() == null) {
                                    errorMessage = p.message();
                                } else {
                                    errorMessage = String.format("[%s]%s", p.codeOrNull(), p.messageWithCause());
                                }
                            }
                        } catch (Throwable e) {
                            LOGGER.warn(e.toString(), e);
                        }

                        if (SOSString.isEmpty(errorMessage) && outcome instanceof Failed) {
                            Optional<String> em = OptionConverters.toJava(((Failed) outcome).errorMessage());
                            if (em.isPresent()) {
                                errorMessage = em.get();
                            }
                        }
                    }
                }
            }

            public int getReturnCode() {
                return returnCode;
            }

            public boolean isSuccessReturnCode() {
                return isSuccessReturnCode;
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

            public Map<String, String> getKeyValues() {
                return keyValues;
            }

            public OutcomeType getType() {
                return type;
            }
        }

        public class StepInfo {

            private final WorkflowInfo workflowInfo;
            private final JOrder order;
            private String agentPath;
            private String jobName;

            public StepInfo(WorkflowInfo workflowInfo, JOrder order) {
                this.workflowInfo = workflowInfo;
                this.order = order;
            }

            public String getAgentPath() throws Exception {
                if (agentPath == null) {
                    if (order == null) {
                        throw new Exception(String.format("[%s][%s]missing JOrder", eventId, orderId));
                    }

                    Either<Problem, AgentRefPath> pa = order.attached();
                    AgentRefPath arp = getFromEither(pa);
                    agentPath = arp.string();
                }
                return agentPath;
            }

            public String getJobName() throws Exception {
                if (jobName == null) {
                    if (state == null) {
                        throw new Exception(String.format("[%s][%s]missing JControllerState", eventId, orderId));
                    }
                    if (workflowInfo == null) {
                        throw new Exception(String.format("[%s][%s]missing WorkflowInfo", eventId, orderId));
                    }

                    Either<Problem, JWorkflow> pw = state.idToWorkflow(workflowInfo.getWorkflowId());
                    JWorkflow workflow = getFromEither(pw);

                    Either<Problem, Name> pn = workflow.checkedJobName(workflowInfo.getPosition().getUnderlying());
                    Name name = getFromEither(pn);
                    jobName = name.toString();
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

            public class Position {

                private static final String DELIMITER = "/";
                private final JPosition underlying;
                private final List<Object> entries;
                private String value;

                public Position(JPosition p) {
                    underlying = p;
                    entries = underlying.toList();
                    // value = underlying.toString();
                }

                public JPosition getUnderlying() {
                    return underlying;
                }

                public String asString() {
                    if (value == null) {
                        value = entries.stream().map(o -> o.toString()).collect(Collectors.joining(DELIMITER));
                    }
                    return value;
                }

                public List<Object> asList() {
                    return entries;
                }

                public Integer getRetry() {
                    Optional<Object> r = entries.stream().filter(f -> f.toString().startsWith("try+")).findFirst();
                    if (r.isPresent()) {
                        return Integer.parseInt(r.get().toString().substring(3));// TODO check
                    }
                    return 0;
                }

                public Integer getLastPosition() {
                    return (Integer) entries.get(entries.size() - 1);
                }

                public String getParentPositionAsString() {// 0->0, 1/fork_1/0 -> 1/fork_1
                    return getParentPositionAsString(entries);
                }

                public String getParentPositionAsString(List<Object> pos) {// 0->0, 1/fork_1/0 -> 1/fork_1
                    if (pos == null || pos.size() < 1) {
                        return null;
                    }
                    // if (pos.size() == 1) {
                    // return pos.get(0).toString();
                    // }

                    return pos.stream().limit(pos.size() - 1).map(o -> o.toString()).collect(Collectors.joining(DELIMITER));
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

    public class HistoryAgentReady {

        private final String timezone;
        private final String path;
        private String uri;

        public HistoryAgentReady() throws FatEventProblemException {
            timezone = ((AgentReady) event).timezone();

            AgentRefPath arp = (AgentRefPath) keyedEvent.key();
            path = arp.string();

            Either<Problem, JAgentRef> pa = eventAndState.state().pathToAgentRef(arp);
            JAgentRef ar = getFromEither(pa);
            if (ar != null) {
                uri = ar.uri().toString();
            }
        }

        public String getTimezone() {
            return timezone;
        }

        public String getPath() {
            return path;
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
