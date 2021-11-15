package com.sos.js7.history.controller;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.classes.proxy.ProxyUser;
import com.sos.joc.cluster.AJocClusterService;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration;
import com.sos.joc.cluster.notifier.DefaultNotifier;
import com.sos.joc.cluster.notifier.INotifier;
import com.sos.joc.cluster.notifier.Mailer;
import com.sos.js7.history.controller.configuration.HistoryConfiguration;
import com.sos.js7.history.controller.model.HistoryModel;
import com.sos.js7.history.controller.proxy.EventFluxStopper;
import com.sos.js7.history.controller.proxy.HistoryEventEntry;
import com.sos.js7.history.controller.proxy.HistoryEventEntry.HistoryAgentCouplingFailed;
import com.sos.js7.history.controller.proxy.HistoryEventEntry.HistoryAgentReady;
import com.sos.js7.history.controller.proxy.HistoryEventEntry.HistoryAgentShutDown;
import com.sos.js7.history.controller.proxy.HistoryEventEntry.HistoryClusterCoupled;
import com.sos.js7.history.controller.proxy.HistoryEventEntry.HistoryControllerReady;
import com.sos.js7.history.controller.proxy.HistoryEventEntry.HistoryOrder;
import com.sos.js7.history.controller.proxy.HistoryEventEntry.HistoryOrder.OrderLock;
import com.sos.js7.history.controller.proxy.HistoryEventEntry.HistoryOrder.OutcomeInfo;
import com.sos.js7.history.controller.proxy.HistoryEventEntry.HistoryOrder.WorkflowInfo;
import com.sos.js7.history.controller.proxy.HistoryEventEntry.HistoryOrder.WorkflowInfo.Position;
import com.sos.js7.history.controller.proxy.HistoryEventEntry.OutcomeType;
import com.sos.js7.history.controller.proxy.HistoryEventType;
import com.sos.js7.history.controller.proxy.fatevent.AFatEvent;
import com.sos.js7.history.controller.proxy.fatevent.FatEventAgentCouplingFailed;
import com.sos.js7.history.controller.proxy.fatevent.FatEventAgentReady;
import com.sos.js7.history.controller.proxy.fatevent.FatEventAgentShutDown;
import com.sos.js7.history.controller.proxy.fatevent.FatEventClusterCoupled;
import com.sos.js7.history.controller.proxy.fatevent.FatEventControllerReady;
import com.sos.js7.history.controller.proxy.fatevent.FatEventControllerShutDown;
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderBroken;
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderCancelled;
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderFailed;
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderFinished;
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderForked;
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderJoined;
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderLockAcquired;
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderLockQueued;
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderLockReleased;
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderResumeMarked;
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderResumed;
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderStarted;
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderStepProcessed;
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderStepStarted;
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderStepStdWritten;
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderStepStdWritten.StdType;
import com.sos.js7.history.helper.HistoryUtil;
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderSuspendMarked;
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderSuspended;
import com.sos.js7.history.controller.proxy.fatevent.FatEventWithProblem;
import com.sos.js7.history.controller.proxy.fatevent.FatForkedChild;
import com.sos.js7.history.controller.proxy.fatevent.FatOutcome;

import js7.base.problem.ProblemCode;
import js7.base.problem.ProblemException;
import js7.data.event.Event;
import js7.data.order.OrderEvent.OrderBroken;
import js7.data.order.OrderEvent.OrderLockAcquired;
import js7.data.order.OrderEvent.OrderLockQueued;
import js7.data.order.OrderEvent.OrderLockReleased;
import js7.data.order.OrderEvent.OrderStderrWritten;
import js7.data.order.OrderEvent.OrderStdoutWritten;
import js7.data.order.OrderId;
import js7.data_for_java.order.JOrder.Forked;
import js7.data_for_java.order.JOrderEvent.JOrderFailed;
import js7.data_for_java.order.JOrderEvent.JOrderForked;
import js7.data_for_java.order.JOrderEvent.JOrderJoined;
import js7.data_for_java.order.JOrderEvent.JOrderProcessed;
import js7.data_for_java.problem.JProblem;
import js7.proxy.data.event.ProxyEvent;
import js7.proxy.javaapi.JControllerApi;
import js7.proxy.javaapi.data.controller.JEventAndControllerState;
import js7.proxy.javaapi.eventbus.JStandardEventBus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;

public class HistoryControllerHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryControllerHandler.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private static final String TORN_PROBLEM_CODE = "SnapshotForUnknownEventId";

    private final SOSHibernateFactory factory;
    private final HistoryConfiguration config;
    private final ControllerConfiguration controllerConfig;
    private final INotifier notifier;
    private final String controllerId;
    private final String serviceIdentifier;

    private JControllerApi api;
    private EventFluxStopper stopper = new EventFluxStopper();
    private final Object lockObject = new Object();
    private HistoryModel model;

    private AtomicBoolean closed = new AtomicBoolean(false);
    private String identifier;
    private int releaseEventsInterval;// minutes
    private long lastReleaseEvents;// seconds
    private Long lastReleaseEventId;

    private AtomicLong tornAfterEventId;
    private AtomicLong lastActivityStart = new AtomicLong();
    private AtomicLong lastActivityEnd = new AtomicLong();

    public HistoryControllerHandler(SOSHibernateFactory factory, HistoryConfiguration config, ControllerConfiguration controllerConfig,
            Mailer notifier, String serviceIdentifier) {
        this.factory = factory;
        this.config = config;
        this.controllerConfig = controllerConfig;
        this.notifier = notifier == null ? new DefaultNotifier() : notifier;
        this.controllerId = controllerConfig.getCurrent().getId();
        this.serviceIdentifier = serviceIdentifier;
        setIdentifier(controllerConfig.getCurrent().getType());
    }

    public void start() {
        closed.set(false);

        String method = "start";
        try {
            model = new HistoryModel(factory, config, controllerConfig);
            setIdentifier(controllerConfig.getCurrent().getType());
            lastActivityStart.set(new Date().getTime());
            executeGetEventId();
            lastActivityEnd.set(new Date().getTime());
            if (model.getStoredEventId() != null) {
                start(new AtomicLong(model.getStoredEventId()));
            }
        } catch (Throwable e) {
            LOGGER.error(String.format("[%s][%s]%s", identifier, method, e.toString()), e);
            notifier.notifyOnError(method, e);
            wait(config.getWaitIntervalOnError());
        }
    }

    private void start(AtomicLong eventId) throws Exception {
        String method = getMethodName("start");
        LOGGER.info(String.format("%seventId=%s", method, eventId));

        initReleaseEvents(model.getHistoryConfiguration());
        api = ControllerApi.of(controllerConfig.getCurrent().getId(), ProxyUser.HISTORY);
        long errorCounter = 0;
        while (!closed.get()) {
            try {
                if (tornAfterEventId != null) {
                    eventId.set(tornAfterEventId.get());
                    tornAfterEventId = null;
                }
                eventId = process(eventId);
                errorCounter = 0;
            } catch (Throwable ex) {
                if (closed.get()) {
                    LOGGER.info(String.format("%s[closed][exception ignored]%s", method, ex.toString()));
                } else {
                    if (isProblemException(ex)) {
                        if (isTornException((ProblemException) ex)) {
                            LOGGER.warn(String.format("%s[TORN]%s", method, ex.toString()));
                            tornAfterEventId = new AtomicLong(getTornEventId());
                        }
                    } else if (isReactorException(ex)) {
                        LOGGER.warn(String.format("%s[exception]%s", method, ex.toString()), ex);
                    } else {
                        LOGGER.error(String.format("%s[exception]%s", method, ex.toString()), ex);
                    }
                    // notifier.notifyOnError(method, ex); //TODO avoid flooding
                }
                errorCounter++;
                int interval = config.getWaitIntervalOnError();
                if (errorCounter > 10) {
                    interval = interval * 2;
                }
                wait(interval);

            }
        }

        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s[end]%s", method, eventId));
        }
    }

    private Long getTornEventId() {
        String method = getMethodName("getTornEventId");
        LOGGER.info(String.format("%s...", method));
        boolean run = true;
        while (run) {
            if (closed.get()) {
                run = false;
            } else {
                try {
                    Long id = api.journalInfo().thenApply(o -> o.get().tornEventId()).get();
                    LOGGER.info(String.format("%s[tornEventId]%s", method, id));
                    return id;
                } catch (Throwable e) {
                    LOGGER.error(String.format("%s[end]%s", method, e.toString()), e);
                    wait(config.getWaitIntervalOnError());
                }
            }
        }
        return null;
    }

    private synchronized AtomicLong process(AtomicLong eventId) throws Exception {

        try (JStandardEventBus<ProxyEvent> eventBus = new JStandardEventBus<>(ProxyEvent.class)) {
            Flux<JEventAndControllerState<Event>> flux = api.eventFlux(eventBus, OptionalLong.of(eventId.get()));
            flux = flux.filter(e -> HistoryEventType.fromValue(e.stampedEvent().value().event().getClass().getSimpleName()) != null);

            // flux = flux.doOnNext(this::fluxDoOnNext);
            flux = flux.doOnError(this::fluxDoOnError);
            flux = flux.doOnComplete(this::fluxDoOnComplete);
            flux = flux.doOnCancel(this::fluxDoOnCancel);
            flux = flux.doFinally(this::fluxDoFinally);

            flux.takeUntilOther(stopper.stopped()).map(this::map2fat).bufferTimeout(config.getBufferTimeoutMaxSize(), Duration.ofSeconds(config
                    .getBufferTimeoutMaxTime())).toIterable().forEach(list -> {
                        if (!closed.get()) {
                            try {
                                lastActivityStart.set(new Date().getTime());
                                eventId.set(model.process(list));
                                releaseEvents(eventId.get());
                                lastActivityEnd.set(new Date().getTime());
                            } catch (Throwable e) {
                                LOGGER.error(e.toString(), e);
                                wait(config.getWaitIntervalOnError());
                            }
                        }
                    });
            return eventId;
        }
    }

    private AFatEvent map2fat(JEventAndControllerState<Event> eventAndState) {
        AFatEvent event = null;
        HistoryEventEntry entry = null;
        try {
            entry = new HistoryEventEntry(eventAndState);
            HistoryOrder order;
            OutcomeInfo oi;
            List<FatForkedChild> childs;
            FatOutcome outcome;
            OrderLock ol;
            switch (entry.getEventType()) {
            case ClusterCoupled:
                HistoryClusterCoupled cc = entry.getClusterCoupled();

                event = new FatEventClusterCoupled(entry.getEventId(), entry.getEventDate());
                event.set(controllerConfig.getCurrent().getId(), cc.getActiveId(), cc.isPrimary());
                break;
            case ControllerReady:
                HistoryControllerReady cr = entry.getControllerReady();

                event = new FatEventControllerReady(entry.getEventId(), entry.getEventDate());
                event.set(controllerConfig.getCurrent().getId(), cr.getTimezone(), cr.getTotalRunningTimeAsMillis());
                break;

            case ControllerShutDown:
                event = new FatEventControllerShutDown(entry.getEventId(), entry.getEventDate());
                event.set(controllerConfig.getCurrent().getId());
                break;

            case AgentReady:
                HistoryAgentReady ar = entry.getAgentReady();

                event = new FatEventAgentReady(entry.getEventId(), entry.getEventDate());
                event.set(ar.getId(), ar.getUri(), ar.getTimezone());
                break;

            case AgentCouplingFailed:
                HistoryAgentCouplingFailed acf = entry.getAgentCouplingFailed();

                event = new FatEventAgentCouplingFailed(entry.getEventId(), entry.getEventDate());
                event.set(acf.getId(), acf.getMessage());
                break;

            case AgentShutDown:
                HistoryAgentShutDown acs = entry.getAgentShutDown();

                event = new FatEventAgentShutDown(entry.getEventId(), entry.getEventDate());
                event.set(acs.getId());
                break;

            case OrderStarted:
                order = entry.getCheckedOrder();
                Date scheduledFor = null;
                try {
                    scheduledFor = entry.getCheckedOrderFromPreviousState().getScheduledFor();
                } catch (Throwable e) {
                    LOGGER.warn(String.format("[%s][%s][PreviousState]%s", entry.getEventType().name(), order.getOrderId(), e.toString()), e);
                }
                event = new FatEventOrderStarted(entry.getEventId(), entry.getEventDate());
                event.set(order.getOrderId(), order.getWorkflowInfo().getPath(), order.getWorkflowInfo().getVersionId(), order.getWorkflowInfo()
                        .getPosition(), order.getArguments(), scheduledFor);
                break;
            case OrderForked:
                order = entry.getCheckedOrder();
                JOrderForked jof = (JOrderForked) entry.getJOrderEvent();

                WorkflowInfo wi = order.getWorkflowInfo();
                Position position = wi.getPosition();
                List<?> positions = position.getUnderlying().toList();
                childs = new ArrayList<FatForkedChild>();
                jof.children().forEach(c -> {
                    String branchIdOrName = null;
                    String name4Position = null;
                    if (c.branchId().isPresent()) {
                        branchIdOrName = c.branchId().get().string();
                        name4Position = branchIdOrName;
                    } else {
                        branchIdOrName = HistoryUtil.getForkChildNameFromOrderId(c.orderId().string());
                        name4Position = "fork";
                    }
                    // copy
                    List<Object> childPositions = positions.stream().collect(Collectors.toList());
                    childPositions.add(name4Position);
                    childPositions.add(0);
                    childs.add(new FatForkedChild(c.orderId().string(), branchIdOrName, wi.createNewPosition(childPositions)));
                });
                event = new FatEventOrderForked(entry.getEventId(), entry.getEventDate());
                event.set(order.getOrderId(), wi.getPath(), wi.getVersionId(), position, order.getArguments(), childs);
                break;

            case OrderJoined:
                order = entry.getCheckedOrderFromPreviousState();
                childs = new ArrayList<FatForkedChild>();
                Forked f = order.getForked();
                for (OrderId id : f.childOrderIds()) {
                    childs.add(new FatForkedChild(id.string(), null, null));
                }

                JOrderJoined joj = (JOrderJoined) entry.getJOrderEvent();
                oi = order.getOutcomeInfo(joj.outcome());
                outcome = null;
                if (oi != null) {
                    outcome = new FatOutcome(oi.getType(), oi.getReturnCode(), oi.isSucceeded(), oi.isFailed(), oi.getNamedValues(), oi
                            .getErrorCode(), oi.getErrorMessage());
                }

                event = new FatEventOrderJoined(entry.getEventId(), entry.getEventDate());
                event.set(order.getOrderId(), order.getWorkflowInfo().getPath(), order.getWorkflowInfo().getVersionId(), order.getWorkflowInfo()
                        .getPosition(), order.getArguments(), childs, outcome);
                break;

            case OrderStepStdoutWritten:
                order = entry.getOrder();
                OrderStdoutWritten stdout = (OrderStdoutWritten) entry.getEvent();

                event = new FatEventOrderStepStdWritten(entry.getEventId(), entry.getEventDate());
                event.set(StdType.STDOUT, order.getOrderId(), stdout.chunk());
                break;

            case OrderStepStderrWritten:
                order = entry.getOrder();
                OrderStderrWritten stderr = (OrderStderrWritten) entry.getEvent();

                event = new FatEventOrderStepStdWritten(entry.getEventId(), entry.getEventDate());
                event.set(StdType.STDERROR, order.getOrderId(), stderr.chunk());
                break;

            case OrderStepStarted:
                order = entry.getCheckedOrder();

                event = new FatEventOrderStepStarted(entry.getEventId(), entry.getEventDate());
                event.set(order.getOrderId(), order.getWorkflowInfo().getPath(), order.getWorkflowInfo().getVersionId(), order.getWorkflowInfo()
                        .getPosition(), order.getArguments(), order.getStepInfo().getAgentId(), order.getStepInfo().getJobName(), order.getStepInfo()
                                .getJobLabel());
                break;

            case OrderStepProcessed:
                order = entry.getCheckedOrder();

                JOrderProcessed op = (JOrderProcessed) entry.getJOrderEvent();
                oi = order.getOutcomeInfo(op.outcome());
                outcome = null;
                if (oi != null) {
                    outcome = new FatOutcome(oi.getType(), oi.getReturnCode(), oi.isSucceeded(), oi.isFailed(), oi.getNamedValues(), oi
                            .getErrorCode(), oi.getErrorMessage());
                }
                event = new FatEventOrderStepProcessed(entry.getEventId(), entry.getEventDate());
                event.set(order.getOrderId(), outcome, order.getWorkflowInfo().getPosition());
                break;

            case OrderFailed:
                order = entry.getCheckedOrder();

                JOrderFailed of = (JOrderFailed) entry.getJOrderEvent();
                oi = order.getOutcomeInfo(of.outcome());
                outcome = null;
                if (oi != null) {
                    outcome = new FatOutcome(oi.getType(), oi.getReturnCode(), oi.isSucceeded(), oi.isFailed(), oi.getNamedValues(), oi
                            .getErrorCode(), oi.getErrorMessage());
                }

                event = new FatEventOrderFailed(entry.getEventId(), entry.getEventDate());
                event.set(order.getOrderId(), outcome, order.getWorkflowInfo().getPosition());
                break;

            case OrderBroken:
                order = entry.getCheckedOrder();
                OrderBroken ob = (OrderBroken) entry.getEvent();

                oi = order.getOutcomeInfo(OutcomeType.broken, ob.problem());
                outcome = null;
                if (oi != null) {
                    outcome = new FatOutcome(oi.getType(), oi.getReturnCode(), oi.isSucceeded(), oi.isFailed(), oi.getNamedValues(), oi
                            .getErrorCode(), oi.getErrorMessage());
                }
                event = new FatEventOrderBroken(entry.getEventId(), entry.getEventDate());
                event.set(order.getOrderId(), outcome, order.getWorkflowInfo().getPosition());
                break;

            case OrderSuspended:
                order = entry.getCheckedOrder();

                event = new FatEventOrderSuspended(entry.getEventId(), entry.getEventDate());
                event.set(order.getOrderId(), null, order.getWorkflowInfo().getPosition());
                break;

            case OrderSuspendMarked:
                order = entry.getCheckedOrder();

                event = new FatEventOrderSuspendMarked(entry.getEventId(), entry.getEventDate());
                event.set(order.getOrderId(), null, order.getWorkflowInfo().getPosition());
                break;

            case OrderResumed:
                order = entry.getOrder();

                event = new FatEventOrderResumed(entry.getEventId(), entry.getEventDate());
                event.set(order.getOrderId());
                break;

            case OrderResumeMarked:
                order = entry.getCheckedOrder();

                event = new FatEventOrderResumeMarked(entry.getEventId(), entry.getEventDate());
                event.set(order.getOrderId(), null, order.getWorkflowInfo().getPosition());
                break;

            case OrderFinished:
                order = entry.getCheckedOrder();

                event = new FatEventOrderFinished(entry.getEventId(), entry.getEventDate());
                event.set(order.getOrderId(), null, order.getWorkflowInfo().getPosition());
                break;

            case OrderCancelled:
                order = entry.getCheckedOrder();
                Boolean isStarted = null;
                try {
                    isStarted = entry.getCheckedOrderFromPreviousState().isStarted();
                } catch (Throwable e) {
                    LOGGER.warn(String.format("[%s][%s][PreviousState]%s", entry.getEventType().name(), order.getOrderId(), e.toString()), e);
                }
                event = new FatEventOrderCancelled(entry.getEventId(), entry.getEventDate(), isStarted);
                event.set(order.getOrderId(), null, order.getWorkflowInfo().getPosition());
                break;

            case OrderLockAcquired:
                order = entry.getCheckedOrder();

                ol = order.getOrderLock((OrderLockAcquired) entry.getEvent());
                event = new FatEventOrderLockAcquired(entry.getEventId(), entry.getEventDate(), order.getOrderId(), ol, order.getWorkflowInfo()
                        .getPosition());
                break;

            case OrderLockQueued:
                order = entry.getCheckedOrder();

                ol = order.getOrderLock((OrderLockQueued) entry.getEvent());
                event = new FatEventOrderLockQueued(entry.getEventId(), entry.getEventDate(), order.getOrderId(), ol, order.getWorkflowInfo()
                        .getPosition());
                break;

            case OrderLockReleased:
                order = entry.getCheckedOrder();

                ol = order.getOrderLock((OrderLockReleased) entry.getEvent());
                event = new FatEventOrderLockReleased(entry.getEventId(), entry.getEventDate(), order.getOrderId(), ol, order.getWorkflowInfo()
                        .getPosition());
                break;

            default:
                event = new FatEventWithProblem(entry, new Exception("unknown type=" + entry.getEventType()));
                break;
            }

        } catch (Throwable e) {
            // Flux.error(e);
            if (entry == null) {
                event = new FatEventWithProblem(entry, e);
            } else {
                event = new FatEventWithProblem(entry, e, entry.getEventId(), entry.getEventDate());
            }
        }
        return event;
    }

    @SuppressWarnings("unused")
    private void fluxDoOnNext(JEventAndControllerState<Event> state) {
        // releaseEvents(model.getStoredEventId());
        AJocClusterService.setLogger(serviceIdentifier);
        LOGGER.info(String.format("[%s][fluxDoOnNext]%s", controllerId, SOSString.toString(state)));
    }

    private void fluxDoOnCancel() {
        AJocClusterService.setLogger(serviceIdentifier);
        LOGGER.debug(String.format("[%s][fluxDoOnCancel]", controllerId));
    }

    private Throwable fluxDoOnError(Throwable t) {
        AJocClusterService.setLogger(serviceIdentifier);
        LOGGER.warn(String.format("[%s][fluxDoOnError]%s", controllerId, t.toString()));
        return t;
    }

    private void fluxDoOnComplete() {
        AJocClusterService.setLogger(serviceIdentifier);
        LOGGER.info(String.format("[%s][fluxDoOnComplete]", controllerId));
    }

    private void fluxDoFinally(SignalType type) {
        AJocClusterService.setLogger(serviceIdentifier);
        LOGGER.info(String.format("[%s][fluxDoFinally]SignalType=%s", controllerId, type));
    }

    private boolean isProblemException(Throwable t) {
        return t != null && t instanceof ProblemException;
    }

    private boolean isTornException(ProblemException ex) {
        try {
            Optional<ProblemCode> code = JProblem.apply(ex.problem()).maybeCode();
            if (code.isPresent()) {
                if (TORN_PROBLEM_CODE.equalsIgnoreCase(code.get().string())) {
                    return true;
                }
            }
        } catch (Throwable e) {
        }
        return false;
    }

    private boolean isReactorException(Throwable t) {
        try {
            if (t.toString().contains("reactor.core.Exceptions")) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }

    public void wait(int interval) {
        if (!closed.get() && interval > 0) {
            String method = getMethodName("wait");
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s%ss ...", method, interval));
            }
            try {
                synchronized (lockObject) {
                    lockObject.wait(interval * 1_000);
                }
            } catch (InterruptedException e) {
                if (closed.get()) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("%ssleep interrupted due to handler close", method));
                    }
                } else {
                    LOGGER.warn(String.format("%s%s", method, e.toString()), e);
                }
            }
        }
    }

    private String getMethodName(String name) {
        String prefix = identifier == null ? "" : String.format("[%s]", identifier);
        return String.format("%s[%s]", prefix, name);
    }

    public void close() {
        doClose();
        if (model != null) {
            model.close();
        }
    }

    public void doClose() {
        closed.set(true);
        try {
            stopper.stop();
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        }
        synchronized (lockObject) {
            lockObject.notifyAll();
        }
    }

    private void setIdentifier(String type) {
        String identifier = controllerConfig.getCurrent().getId();
        if (controllerConfig.getSecondary() != null) {
            identifier = "cluster][" + identifier;
            // if (!SOSString.isEmpty(type)) {
            // identifier = identifier + "][" + type;
            // }
        }
        this.identifier = identifier;
        if (model != null) {
            model.setIdentifier(identifier);
        }
    }

    public String getIdentifier() {
        return identifier;
    }

    public AtomicLong getLastActivityStart() {
        return lastActivityStart;
    }

    public AtomicLong getLastActivityEnd() {
        return lastActivityEnd;
    }

    public String getControllerId() {
        return controllerId;
    }

    private void executeGetEventId() {
        String method = "executeGetEventId";
        int count = 0;
        boolean run = true;
        while (run) {
            if (closed.get()) {
                return;
            }
            count++;
            try {
                model.setStoredEventId(model.getEventId());
                run = false;
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("[%s][%s]%s", identifier, method, model.getStoredEventId()));
                }
            } catch (Throwable e) {
                LOGGER.error(String.format("[%s][%s][%s]%s", identifier, method, count, e.toString()), e);
                notifier.notifyOnError(String.format("[%s][%s]", method, count), e);
                wait(config.getWaitIntervalOnError());
            }
        }
    }

    private void initReleaseEvents(HistoryConfiguration hc) {
        releaseEventsInterval = hc.getReleaseEventsInterval();
        lastReleaseEventId = 0L;
        lastReleaseEvents = SOSDate.getSeconds(new Date());
    }

    private void releaseEvents(Long eventId) {
        if (eventId != null && eventId > 0 && lastReleaseEvents > 0 && !eventId.equals(lastReleaseEventId)) {
            Long current = SOSDate.getSeconds(new Date());
            if (((current - lastReleaseEvents) / 60) >= releaseEventsInterval) {
                String method = "releaseEvents";
                try {
                    LOGGER.info(String.format("[%s][%s]%s", getIdentifier(), method, eventId));
                    js7.data_for_java.vavr.VavrUtils.await(api.releaseEvents(eventId));
                    lastReleaseEventId = eventId;
                } catch (Throwable t) {
                    LOGGER.error(String.format("[%s][%s][%s]%s", getIdentifier(), method, eventId, t.toString()));
                } finally {
                    lastReleaseEvents = current;
                }
            }
        }
    }

}
