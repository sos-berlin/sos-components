package com.sos.js7.history.controller;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.classes.proxy.ProxyUser;
import com.sos.joc.model.cluster.common.ClusterServices;
import com.sos.js7.event.controller.EventMeta;
import com.sos.js7.event.controller.configuration.Configuration;
import com.sos.js7.event.controller.configuration.controller.ControllerConfiguration;
import com.sos.js7.event.notifier.DefaultNotifier;
import com.sos.js7.event.notifier.INotifier;
import com.sos.js7.event.notifier.Mailer;
import com.sos.js7.history.controller.configuration.HistoryConfiguration;
import com.sos.js7.history.controller.model.HistoryModel;
import com.sos.js7.history.controller.proxy.EventFluxStopper;
import com.sos.js7.history.controller.proxy.HistoryEventEntry;
import com.sos.js7.history.controller.proxy.HistoryEventEntry.HistoryAgentReady;
import com.sos.js7.history.controller.proxy.HistoryEventEntry.HistoryControllerReady;
import com.sos.js7.history.controller.proxy.HistoryEventEntry.HistoryOrder;
import com.sos.js7.history.controller.proxy.HistoryEventEntry.HistoryOrder.OutcomeInfo;
import com.sos.js7.history.controller.proxy.HistoryEventEntry.OutcomeType;
import com.sos.js7.history.controller.proxy.HistoryEventType;
import com.sos.js7.history.controller.proxy.fatevent.AFatEvent;
import com.sos.js7.history.controller.proxy.fatevent.FatEventAgentReady;
import com.sos.js7.history.controller.proxy.fatevent.FatEventControllerReady;
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderAdded;
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderBroken;
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderCancelled;
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderFailed;
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderFinished;
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderForked;
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderJoined;
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderResumeMarked;
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderResumed;
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderStepProcessed;
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderStepStarted;
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderStepStdWritten;
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderStepStdWritten.StdType;
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderSuspendMarked;
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderSuspended;
import com.sos.js7.history.controller.proxy.fatevent.FatEventWithProblem;
import com.sos.js7.history.controller.proxy.fatevent.FatForkedChild;
import com.sos.js7.history.controller.proxy.fatevent.FatOutcome;

import js7.base.problem.ProblemCode;
import js7.base.problem.ProblemException;
import js7.data.event.Event;
import js7.data.order.OrderEvent.OrderBroken;
import js7.data.order.OrderEvent.OrderStderrWritten;
import js7.data.order.OrderEvent.OrderStdoutWritten;
import js7.data.order.OrderId;
import js7.proxy.data.ProxyEvent;
import js7.proxy.javaapi.JControllerApi;
import js7.proxy.javaapi.data.controller.JEventAndControllerState;
import js7.proxy.javaapi.data.order.JOrder.Forked;
import js7.proxy.javaapi.data.order.JOrderEvent.JOrderAdded;
import js7.proxy.javaapi.data.order.JOrderEvent.JOrderFailed;
import js7.proxy.javaapi.data.order.JOrderEvent.JOrderForked;
import js7.proxy.javaapi.data.order.JOrderEvent.JOrderJoined;
import js7.proxy.javaapi.data.order.JOrderEvent.JOrderProcessed;
import js7.proxy.javaapi.data.problem.JProblem;
import js7.proxy.javaapi.eventbus.JStandardEventBus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;

public class HistoryControllerHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryControllerHandler.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private static final String TORN_PROBLEM_CODE = "SnapshotForUnknownEventId";

    private final SOSHibernateFactory factory;
    private final Configuration config;
    private final HistoryConfiguration historyConfig;
    private final ControllerConfiguration controllerConfig;
    private final INotifier notifier;

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

    public HistoryControllerHandler(SOSHibernateFactory factory, Configuration config, ControllerConfiguration controllerConfig, Mailer notifier) {
        this.factory = factory;
        this.config = config;
        this.historyConfig = (HistoryConfiguration) config.getApp();
        this.controllerConfig = controllerConfig;
        this.notifier = notifier == null ? new DefaultNotifier() : notifier;
        setIdentifier(controllerConfig.getCurrent().getType());
    }

    public void start() {
        MDC.put("clusterService", ClusterServices.history.name());
        closed.set(false);

        String method = "run";
        try {
            model = new HistoryModel(factory, historyConfig, controllerConfig);
            setIdentifier(controllerConfig.getCurrent().getType());
            executeGetEventId();
            start(new AtomicLong(model.getStoredEventId()));
        } catch (Throwable e) {
            LOGGER.error(String.format("[%s][%s]%s", identifier, method, e.toString()), e);
            notifier.notifyOnError(method, e);
            wait(config.getHandler().getWaitIntervalOnError());
        }
    }

    private void start(AtomicLong eventId) throws Exception {
        String method = getMethodName("start");
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%seventId=%s", method, eventId));
        }
        initReleaseEvents(model.getHistoryConfiguration());
        api = ControllerApi.of(controllerConfig.getCurrent().getId(), ProxyUser.HISTORY);
        while (!closed.get()) {
            try {
                if (tornAfterEventId != null) {
                    eventId.set(tornAfterEventId.get());
                    tornAfterEventId = null;
                }
                eventId = process(eventId);
            } catch (Throwable ex) {
                if (closed.get()) {
                    LOGGER.info(String.format("%s[closed][exception ignored]%s", method, ex.toString()));
                } else {
                    if (isTornException(ex)) {
                        LOGGER.error(String.format("%s[TORN]%s", method, ex.toString()));
                        tornAfterEventId = new AtomicLong(getTornEventId());
                    } else {
                        LOGGER.error(String.format("%s[exception]%s", method, ex.toString()), ex);
                    }
                    // notifier.notifyOnError(method, ex); //TODO avoid flooding
                }
                wait(config.getHandler().getWaitIntervalOnError());
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
                    wait(config.getHandler().getWaitIntervalOnError());
                }
            }
        }
        return null;
    }

    private AtomicLong process(AtomicLong eventId) throws Exception {

        try (JStandardEventBus<ProxyEvent> eventBus = new JStandardEventBus<>(ProxyEvent.class)) {
            Flux<JEventAndControllerState<Event>> flux = api.eventFlux(eventBus, OptionalLong.of(eventId.get()));
            flux = flux.filter(e -> HistoryEventType.fromValue(e.stampedEvent().value().event().getClass().getSimpleName()) != null);

            // flux = flux.doOnNext(this::fluxDoOnNext);
            flux = flux.doOnError(this::fluxDoOnError);
            flux = flux.doOnComplete(this::fluxDoOnComplete);
            flux = flux.doOnCancel(this::fluxDoOnCancel);
            flux = flux.doFinally(this::fluxDoFinally);

            flux.takeUntilOther(stopper.stopped()).map(this::map2fat).bufferTimeout(historyConfig.getBufferTimeoutMaxSize(), Duration.ofSeconds(
                    historyConfig.getBufferTimeoutMaxTime())).toIterable().forEach(list -> {
                        boolean run = true;
                        while (run) {
                            if (closed.get()) {
                                run = false;
                            } else {
                                try {
                                    eventId.set(model.process(list));
                                    releaseEvents(eventId.get());
                                    run = false;
                                } catch (Throwable e) {
                                    LOGGER.error(e.toString(), e);
                                    wait(config.getHandler().getWaitIntervalOnError());
                                }
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
            switch (entry.getEventType()) {
            case ControllerReady:
                HistoryControllerReady cr = entry.getControllerReady();

                event = new FatEventControllerReady(entry.getEventId(), entry.getEventDate());
                event.set(controllerConfig.getCurrent().getId(), cr.getTimezone());
                break;
            case AgentReady:
                HistoryAgentReady ar = entry.getAgentReady();

                event = new FatEventAgentReady(entry.getEventId(), entry.getEventDate());
                event.set(ar.getPath(), ar.getUri(), ar.getTimezone());
                break;

            case OrderAdded:
                order = entry.getCheckedOrder();

                JOrderAdded orderAdded = (JOrderAdded) entry.getJOrderEvent();
                Date planned = orderAdded.scheduledFor().isPresent() ? Date.from(orderAdded.scheduledFor().get()) : null;

                event = new FatEventOrderAdded(entry.getEventId(), entry.getEventDate());
                event.set(order.getOrderId(), order.getWorkflowInfo().getPath(), order.getWorkflowInfo().getVersionId(), order.getWorkflowInfo()
                        .getPosition().asList(), EventMeta.map2Json(order.getArguments()), planned);
                break;

            case OrderForked:
                order = entry.getCheckedOrder();
                JOrderForked jof = (JOrderForked) entry.getJOrderEvent();

                childs = new ArrayList<FatForkedChild>();
                jof.children().forEach(c -> {
                    childs.add(new FatForkedChild(c.orderId().string(), c.branchId().string()));
                });
                event = new FatEventOrderForked(entry.getEventId(), entry.getEventDate());
                event.set(order.getOrderId(), order.getWorkflowInfo().getPath(), order.getWorkflowInfo().getVersionId(), order.getWorkflowInfo()
                        .getPosition().asList(), EventMeta.map2Json(order.getArguments()), childs);
                break;

            case OrderJoined:
                order = entry.getCheckedOrderFromPreviousState();
                childs = new ArrayList<FatForkedChild>();
                Forked f = order.getForked();
                for (OrderId id : f.childOrderIds()) {
                    childs.add(new FatForkedChild(id.string(), null));
                }

                JOrderJoined joj = (JOrderJoined) entry.getJOrderEvent();
                oi = order.getOutcomeInfo(joj.outcome());
                outcome = null;
                if (oi != null) {
                    outcome = new FatOutcome(oi.getType(), oi.getReturnCode(), oi.isSuccessReturnCode(), oi.isSucceeded(), oi.isFailed(), oi
                            .getKeyValues(), oi.getErrorCode(), oi.getErrorMessage());
                }

                event = new FatEventOrderJoined(entry.getEventId(), entry.getEventDate());
                event.set(order.getOrderId(), order.getWorkflowInfo().getPath(), order.getWorkflowInfo().getVersionId(), order.getWorkflowInfo()
                        .getPosition().asList(), EventMeta.map2Json(order.getArguments()), childs, outcome);
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
                        .getPosition().asList(), EventMeta.map2Json(order.getArguments()), order.getStepInfo().getAgentPath(), order.getStepInfo()
                                .getJobName());
                break;

            case OrderStepProcessed:
                order = entry.getOrder();

                JOrderProcessed op = (JOrderProcessed) entry.getJOrderEvent();
                oi = order.getOutcomeInfo(op.outcome());
                outcome = null;
                if (oi != null) {
                    outcome = new FatOutcome(oi.getType(), oi.getReturnCode(), oi.isSuccessReturnCode(), oi.isSucceeded(), oi.isFailed(), oi
                            .getKeyValues(), oi.getErrorCode(), oi.getErrorMessage());
                }
                event = new FatEventOrderStepProcessed(entry.getEventId(), entry.getEventDate());
                event.set(order.getOrderId(), outcome);
                break;
            case OrderFailed:
                order = entry.getOrder();

                JOrderFailed of = (JOrderFailed) entry.getJOrderEvent();
                oi = order.getOutcomeInfo(of.outcome());
                outcome = null;
                if (oi != null) {
                    outcome = new FatOutcome(oi.getType(), oi.getReturnCode(), oi.isSuccessReturnCode(), oi.isSucceeded(), oi.isFailed(), oi
                            .getKeyValues(), oi.getErrorCode(), oi.getErrorMessage());
                }
                event = new FatEventOrderFailed(entry.getEventId(), entry.getEventDate());
                event.set(order.getOrderId(), outcome);

                break;

            case OrderBroken:
                order = entry.getOrder();
                OrderBroken ob = (OrderBroken) entry.getEvent();

                oi = order.getOutcomeInfo(OutcomeType.broken, ob.problem());
                outcome = new FatOutcome(oi.getType(), oi.getReturnCode(), oi.isSuccessReturnCode(), oi.isSucceeded(), oi.isFailed(), oi
                        .getKeyValues(), oi.getErrorCode(), oi.getErrorMessage());

                event = new FatEventOrderBroken(entry.getEventId(), entry.getEventDate());
                event.set(order.getOrderId(), outcome);

                break;
            case OrderSuspended:
                order = entry.getOrder();

                event = new FatEventOrderSuspended(entry.getEventId(), entry.getEventDate());
                event.set(order.getOrderId());

                break;
            case OrderSuspendMarked:
                order = entry.getOrder();

                event = new FatEventOrderSuspendMarked(entry.getEventId(), entry.getEventDate());
                event.set(order.getOrderId());

                break;
            case OrderResumed:
                order = entry.getOrder();

                event = new FatEventOrderResumed(entry.getEventId(), entry.getEventDate());
                event.set(order.getOrderId());

                break;
            case OrderResumeMarked:
                order = entry.getOrder();

                event = new FatEventOrderResumeMarked(entry.getEventId(), entry.getEventDate());
                event.set(order.getOrderId());

                break;
            case OrderFinished:
                order = entry.getOrder();

                event = new FatEventOrderFinished(entry.getEventId(), entry.getEventDate());
                event.set(order.getOrderId());

                break;

            case OrderCancelled:
                order = entry.getOrder();

                event = new FatEventOrderCancelled(entry.getEventId(), entry.getEventDate());
                event.set(order.getOrderId());

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
        releaseEvents(model.getStoredEventId());
    }

    private void fluxDoOnCancel() {
        LOGGER.info("[fluxDoOnCancel]");
    }

    private Throwable fluxDoOnError(Throwable t) {
        LOGGER.info("[fluxDoOnError]" + t.toString());
        return t;
    }

    private void fluxDoOnComplete() {
        LOGGER.info("[fluxDoOnComplete]");
    }

    private void fluxDoFinally(SignalType type) {
        LOGGER.info("[fluxDoFinally] - " + type);
    }

    private boolean isTornException(Throwable t) {
        try {
            if (t instanceof ProblemException) {
                Optional<ProblemCode> code = JProblem.apply(((ProblemException) t).problem()).maybeCode();
                if (code.isPresent()) {
                    if (TORN_PROBLEM_CODE.equalsIgnoreCase(code.get().string())) {
                        return true;
                    }
                }
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
        MDC.put("clusterService", ClusterServices.history.name());
        doClose();
        if (model != null) {
            model.close();
        }
    }

    public void doClose() {
        stopper.stop();
        closed.set(true);

        synchronized (lockObject) {
            lockObject.notifyAll();
        }
    }

    private void setIdentifier(String type) {
        String identifier = controllerConfig.getCurrent().getId();
        if (controllerConfig.getBackup() != null) {
            identifier = "cluster][" + identifier;
            if (!SOSString.isEmpty(type)) {
                identifier = identifier + "][" + type;
            }
        }
        this.identifier = identifier;
        if (model != null) {
            model.setIdentifier(identifier);
        }
    }

    public String getIdentifier() {
        return identifier;
    }

    private void executeGetEventId() {
        String method = "executeGetEventId";
        int count = 0;
        boolean run = true;
        while (run) {
            count++;
            try {
                model.setStoredEventId(model.getEventId());
                run = false;
                LOGGER.info(String.format("[%s][%s]%s", identifier, method, model.getStoredEventId()));
            } catch (Throwable e) {
                LOGGER.error(String.format("[%s][%s][%s]%s", identifier, method, count, e.toString()), e);
                notifier.notifyOnError(String.format("[%s][%s]", method, count), e);
                wait(config.getHandler().getWaitIntervalOnError());
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
                    js7.proxy.javaapi.data.common.VavrUtils.await(api.releaseEvents(eventId));
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
