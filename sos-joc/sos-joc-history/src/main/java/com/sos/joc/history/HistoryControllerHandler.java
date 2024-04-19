package com.sos.joc.history;

import java.nio.file.Path;
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

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSString;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.classes.proxy.ProxyUser;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.JocHistoryConfiguration;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration;
import com.sos.joc.cluster.service.JocClusterServiceLogger;
import com.sos.joc.history.controller.exception.HistoryFatalException;
import com.sos.joc.history.controller.exception.HistoryProcessingDatabaseConnectException;
import com.sos.joc.history.controller.exception.HistoryProcessingException;
import com.sos.joc.history.controller.model.HistoryModel;
import com.sos.joc.history.controller.proxy.EventFluxStopper;
import com.sos.joc.history.controller.proxy.HistoryEventEntry;
import com.sos.joc.history.controller.proxy.HistoryEventEntry.AgentInfo;
import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryAgentCouplingFailed;
import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryAgentReady;
import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryAgentShutDown;
import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryAgentSubagentDedicated;
import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryClusterCoupled;
import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryControllerReady;
import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryOrder;
import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryOrder.OrderLock;
import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryOrder.WorkflowInfo;
import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryOrder.WorkflowInfo.Position;
import com.sos.joc.history.controller.proxy.HistoryEventEntry.OutcomeType;
import com.sos.joc.history.controller.proxy.HistoryEventType;
import com.sos.joc.history.controller.proxy.fatevent.AFatEvent;
import com.sos.joc.history.controller.proxy.fatevent.FatEventAgentCouplingFailed;
import com.sos.joc.history.controller.proxy.fatevent.FatEventAgentReady;
import com.sos.joc.history.controller.proxy.fatevent.FatEventAgentShutDown;
import com.sos.joc.history.controller.proxy.fatevent.FatEventAgentSubagentDedicated;
import com.sos.joc.history.controller.proxy.fatevent.FatEventClusterCoupled;
import com.sos.joc.history.controller.proxy.fatevent.FatEventControllerReady;
import com.sos.joc.history.controller.proxy.fatevent.FatEventControllerShutDown;
import com.sos.joc.history.controller.proxy.fatevent.FatEventEmpty;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderAttached;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderBroken;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderCancelled;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderCaught;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderCyclingPrepared;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderFailed;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderFinished;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderForked;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderJoined;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderLocksAcquired;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderLocksQueued;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderLocksReleased;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderMoved;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderNoticePosted;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderNoticesConsumed;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderNoticesConsumptionStarted;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderNoticesExpected;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderNoticesRead;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderOutcomeAdded;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderPromptAnswered;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderPrompted;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderResumed;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderResumptionMarked;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderRetrying;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderStarted;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderStepProcessed;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderStepStarted;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderStepStdWritten;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderStepStdWritten.StdType;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderStopped;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderSuspended;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderSuspensionMarked;
import com.sos.joc.history.controller.proxy.fatevent.FatEventWithProblem;
import com.sos.joc.history.controller.proxy.fatevent.FatForkedChild;
import com.sos.joc.history.helper.HistoryUtil;

import js7.base.problem.ProblemCode;
import js7.base.problem.ProblemException;
import js7.data.event.Event;
import js7.data.order.OrderEvent.OrderBroken;
import js7.data.order.OrderEvent.OrderLocksAcquired;
import js7.data.order.OrderEvent.OrderLocksQueued;
import js7.data.order.OrderEvent.OrderLocksReleased;
import js7.data.order.OrderEvent.OrderMoved;
import js7.data.order.OrderEvent.OrderNoticesConsumed;
import js7.data.order.OrderEvent.OrderPrompted;
import js7.data.order.OrderEvent.OrderRetrying;
import js7.data.order.OrderEvent.OrderStderrWritten;
import js7.data.order.OrderEvent.OrderStdoutWritten;
import js7.data.order.OrderId;
import js7.data.workflow.Instruction;
import js7.data_for_java.order.JOrder.Forked;
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
    private static final String TORN_PROBLEM_CODE_REGEXP = "UnknownEventId|SnapshotForUnknownEventId";

    private final SOSHibernateFactory factory;
    private final ControllerConfiguration controllerConfig;
    private final String controllerId;
    private final String serviceIdentifier;

    private JControllerApi api;
    private EventFluxStopper stopper = null;
    private final Object lockObject = new Object();
    private JocHistoryConfiguration config;
    private HistoryModel model;

    private AtomicBoolean closed = new AtomicBoolean(false);
    private String identifier;
    private long releaseEventsInterval;// seconds
    private long lastReleaseEvents;// seconds
    private Long lastReleaseEventId;
    private long lastClearCache;// seconds

    private AtomicLong tornAfterEventId;
    private AtomicLong lastActivityStart = new AtomicLong();
    private AtomicLong lastActivityEnd = new AtomicLong();

    public HistoryControllerHandler(SOSHibernateFactory factory, JocHistoryConfiguration config, ControllerConfiguration controllerConfig,
            String serviceIdentifier) {
        this.factory = factory;
        this.config = config;
        this.controllerConfig = controllerConfig;
        this.controllerId = controllerConfig.getCurrent().getId();
        this.serviceIdentifier = serviceIdentifier;
        setIdentifier(controllerConfig.getCurrent().getType());
    }

    // Another thread
    public void updateHistoryConfiguration(JocHistoryConfiguration config) {
        this.config = config;
        if (this.model != null) {
            this.model.updateHistoryConfiguration(config);
        }
    }

    public void start(StartupMode mode, ThreadGroup tg) {
        closed.set(false);

        String method = "start";
        try {
            model = new HistoryModel(factory, config, controllerConfig);
            setIdentifier(controllerConfig.getCurrent().getType());
            lastActivityStart.set(new Date().getTime());
            executeGetEventId();
            lastActivityEnd.set(new Date().getTime());
            if (model.getStoredEventId() != null) {
                start(mode, tg, new AtomicLong(model.getStoredEventId()));
            }
        } catch (Throwable e) {
            LOGGER.error(String.format("[%s][%s]%s", identifier, method, e.toString()), e);
            waitFor(config.getWaitIntervalOnError());
        }
    }

    private void start(StartupMode mode, ThreadGroup tg, AtomicLong eventId) throws Exception {
        String method = getMethodName("start");
        LOGGER.info(String.format("%seventId=%s", method, eventId));

        initIntervals(model.getHistoryConfiguration());
        model.getLogExtHandler().start(tg);
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
                errorCounter++;
                boolean waitForDatabaseConnection = false;

                if (closed.get()) {
                    LOGGER.info(String.format("%s[closed][exception ignored]%s", method, ex.toString()));
                } else {
                    if (isProblemException(ex)) {
                        if (isTornException((ProblemException) ex)) {
                            LOGGER.info(String.format("%s[TORN]%s", method, ex.toString()));
                            tornAfterEventId = new AtomicLong(getTornEventId());
                        } else {
                            LOGGER.error(String.format("%s[errorCounter=%s]%s", method, errorCounter, ex.toString()), ex);
                        }
                    } else if (isReactorException(ex)) {
                        LOGGER.info(String.format("%s[errorCounter=%s]%s", method, errorCounter, ex.toString()), ex);
                    } else if (isHistoryProcessingException(ex)) {
                        if (ex instanceof HistoryProcessingDatabaseConnectException) {
                            waitForDatabaseConnection = true;
                            errorCounter = 0;
                            LOGGER.error(String.format("%s%s", method, ex.toString()), ex);
                        } else {
                            if (config.getMaxStopProcessingOnErrors() > 0 && errorCounter >= config.getMaxStopProcessingOnErrors()) {
                                HistoryFatalException hfe = new HistoryFatalException(controllerId, config.getMaxStopProcessingOnErrors(), ex);
                                LOGGER.error(String.format("%s[errorCounter=%s]%s", method, errorCounter, hfe.toString()), hfe);
                                close(mode);
                            } else {
                                LOGGER.error(String.format("%s[errorCounter=%s]%s", method, errorCounter, ex.toString()), ex);
                            }
                        }
                    } else {
                        LOGGER.error(String.format("%s[errorCounter=%s]%s", method, errorCounter, ex.toString()), ex);
                    }
                    // notifier.notifyOnError(method, ex); //TODO avoid flooding
                }

                stopFlux();

                if (waitForDatabaseConnection) {
                    waitForDatabaseConnection();
                } else {
                    long interval = config.getWaitIntervalOnProcessingError();
                    if (errorCounter > 10) {
                        interval = interval * 2;
                    }
                    waitFor(interval);
                }
            }
        }

        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s[end]%s", method, eventId));
        }
    }

    private void waitForDatabaseConnection() {
        String method = getMethodName("waitForDatabaseConnection");
        boolean run = true;
        long counter = 0;

        while (run) {
            if (closed.get()) {
                run = false;
            } else {
                counter++;
                try {
                    long interval = 0;
                    if (counter <= 10) {
                        interval = config.getWaitIntervalOnError();// 5s
                    } else {
                        interval = config.getWaitIntervalOnProcessingError();// 30s
                        if (counter >= 20) {
                            interval = interval * 2;
                        }
                    }
                    LOGGER.info(String.format("%s[%s]wait %ss...", method, counter, interval));
                    waitFor(interval);

                    if (this.model == null) {
                        throw new Exception("HistoryModel is null");
                    } else {
                        this.model.checkConnection(method);
                    }
                    counter = 0;
                    run = false;
                } catch (Throwable e) {
                    LOGGER.error(String.format("%s[%s]%s", method, counter, e.toString()), e);
                }
            }
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
                    LOGGER.info(String.format("%s[end]%s", method, e.toString()), e);
                    waitFor(config.getWaitIntervalOnError());
                }
            }
        }
        return null;
    }

    private synchronized AtomicLong process(AtomicLong eventId) throws Exception {

        stopper = new EventFluxStopper();

        try (JStandardEventBus<ProxyEvent> eventBus = new JStandardEventBus<>(ProxyEvent.class)) {
            Flux<JEventAndControllerState<Event>> flux = api.eventFlux(eventBus, OptionalLong.of(eventId.get()));
            flux = flux.filter(e -> HistoryEventType.fromValue(e.stampedEvent().value().event().getClass().getSimpleName()) != null);

            // flux = flux.doOnNext(this::fluxDoOnNext);
            flux = flux.doOnError(this::fluxDoOnError);
            flux = flux.doOnComplete(this::fluxDoOnComplete);
            flux = flux.doOnCancel(this::fluxDoOnCancel);
            flux = flux.doFinally(this::fluxDoFinally);
            // TODO test flux.publishOn
            // flux = flux.publishOn(Schedulers.fromExecutor(ForkJoinPool.commonPool()));

            flux.takeUntilOther(stopper.stopped()).map(this::map2fat).filter(e -> e.getEventId() != null).bufferTimeout(config
                    .getBufferTimeoutMaxSize(), Duration.ofSeconds(config.getBufferTimeoutMaxTime())).toIterable().forEach(list -> {
                        boolean run = true;
                        int errorCounter = 0;
                        long errorStartMs = 0;

                        clearCache();

                        while (run) {
                            if (closed.get()) {
                                run = false;
                            } else {
                                try {
                                    lastActivityStart.set(new Date().getTime());
                                    eventId.set(model.process(list));
                                    // list.clear();
                                    releaseEvents(eventId.get());
                                    lastActivityEnd.set(new Date().getTime());
                                    errorCounter = 0;
                                } catch (Throwable e) {
                                    if (SOSHibernate.isConnectException(e)) {
                                        throw new HistoryProcessingDatabaseConnectException(controllerId, e);
                                    }

                                    errorCounter++;
                                    if (errorStartMs == 0) {
                                        errorStartMs = new Date().getTime();
                                    } else {
                                        Long current = SOSDate.getSeconds(new Date());
                                        if ((current - errorStartMs / 1_000) >= config.getWaitIntervalStopProcessingOnErrors()) {
                                            int totalErrors = errorCounter;
                                            errorCounter = 0;
                                            throw new HistoryProcessingException(controllerId, e, config.getWaitIntervalStopProcessingOnErrors(),
                                                    new Date(errorStartMs), totalErrors);
                                        }
                                    }
                                    LOGGER.error("[processing][errorCounter=" + errorCounter + "]" + e.toString(), e);
                                    waitFor(config.getWaitIntervalOnProcessingError());
                                } finally {
                                    if (errorCounter == 0) {
                                        errorStartMs = 0;
                                        run = false;
                                    }
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
        HistoryOrder order = null;
        try {
            entry = new HistoryEventEntry(model.getControllerConfiguration().getCurrent().getId(), eventAndState);
            List<FatForkedChild> childs;
            List<OrderLock> ol;
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
                ar.postEvent();

                event = new FatEventAgentReady(entry.getEventId(), entry.getEventDate());
                event.set(ar.getId(), ar.getUri(), ar.getTimezone());
                break;

            case AgentSubagentDedicated:
                HistoryAgentSubagentDedicated ad = entry.getAgentSubagentDedicated();
                ad.postEvent();

                event = new FatEventAgentSubagentDedicated(entry.getEventId(), entry.getEventDate());
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

            case OrderMoved:
                try {
                    OrderMoved om = (OrderMoved) entry.getEvent();
                    if (om.reason().isEmpty()) {
                        order = entry.getCheckedOrder();
                        List<Date> wfa = order.getWaitingForAdmission();
                        if (wfa == null || wfa.size() == 0) {
                            event = new FatEventEmpty();
                        } else {
                            event = new FatEventOrderMoved(entry.getEventId(), entry.getEventDate(), order.getOrderId(), order.getWorkflowInfo()
                                    .getPosition(), om, null, wfa, order.isStarted());
                        }
                    } else {
                        order = entry.getCheckedOrderFromPreviousState();
                        event = new FatEventOrderMoved(entry.getEventId(), entry.getEventDate(), order.getOrderId(), order.getWorkflowInfo()
                                .getPosition(), om, order.getCurrentPositionInstruction(), null, order.isStarted());
                    }
                } catch (Throwable e) {
                    try {
                        order = entry.getCheckedOrder();
                        LOGGER.info(String.format("[OrderMoved][%s]%s", order.getOrderId(), e.toString()), e);
                    } catch (Throwable ee) {
                        LOGGER.info(String.format("[OrderMoved]%s", e.toString()), e);
                    }
                    event = new FatEventEmpty();
                }
                break;

            case OrderAttached:
                order = entry.getCheckedOrder();
                List<Date> wfa = order.getWaitingForAdmission();
                if (wfa == null || wfa.size() == 0) {
                    event = new FatEventEmpty();
                } else {
                    event = new FatEventOrderAttached(entry.getEventId(), entry.getEventDate(), order.getOrderId(), order.getWorkflowInfo()
                            .getPosition(), wfa, order.isStarted());
                }
                break;

            case OrderCyclingPrepared:
                order = entry.getCheckedOrder();
                event = new FatEventOrderCyclingPrepared(entry.getEventId(), entry.getEventDate(), order);
                break;

            case OrderStarted:
                order = entry.getCheckedOrder();

                event = new FatEventOrderStarted(entry.getEventId(), entry.getEventDate(), order.getOrderStartedInfo());
                event.set(order.getOrderId(), order.getWorkflowInfo().getPath(), order.getWorkflowInfo().getVersionId(), order.getWorkflowInfo()
                        .getPosition(), order.getArguments());
                break;

            case OrderForked:
                order = entry.getCheckedOrder();
                JOrderForked jof = (JOrderForked) entry.getJOrderEvent();

                WorkflowInfo wi = order.getWorkflowInfo();
                Position parentPosition = wi.getPosition();
                List<Object> parentPositionAsList = parentPosition.getUnderlying().toList();
                childs = new ArrayList<FatForkedChild>();
                jof.children().forEach(c -> {
                    String branchIdOrName = null;
                    if (c.branchId().isPresent()) {
                        branchIdOrName = c.branchId().get().string();
                    } else {
                        branchIdOrName = HistoryUtil.getForkChildNameFromOrderId(c.orderId().string());
                    }
                    // copy parent position
                    List<Object> childPosition = new ArrayList<>(parentPositionAsList);
                    childPosition.add("fork+" + branchIdOrName);
                    childPosition.add(0);
                    childs.add(new FatForkedChild(c.orderId().string(), branchIdOrName, wi.createNewPosition(childPosition)));
                });
                event = new FatEventOrderForked(entry.getEventId(), entry.getEventDate());
                event.set(order.getOrderId(), wi.getPath(), wi.getVersionId(), parentPosition, null, childs);
                break;

            case OrderJoined:
                order = entry.getCheckedOrderFromPreviousState();
                childs = new ArrayList<FatForkedChild>();
                Forked f = order.getForked();
                for (OrderId id : f.childOrderIds()) {
                    childs.add(new FatForkedChild(id.string(), null, null));
                }
                JOrderJoined joj = (JOrderJoined) entry.getJOrderEvent();

                event = new FatEventOrderJoined(entry.getEventId(), entry.getEventDate());
                event.set(order.getOrderId(), order.getWorkflowInfo().getPath(), order.getWorkflowInfo().getVersionId(), order.getWorkflowInfo()
                        .getPosition(), null, childs, order.getOutcomeInfo(joj.outcome()));
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
                AgentInfo ai = order.getStepInfo().getAgentInfo();

                event = new FatEventOrderStepStarted(entry.getEventId(), entry.getEventDate());
                event.set(order.getOrderId(), order.getWorkflowInfo().getPath(), order.getWorkflowInfo().getVersionId(), order.getWorkflowInfo()
                        .getPosition(), null, ai.getAgentId(), ai.getAgentUri(), ai.getSubagentId(), order.getStepInfo().getJobName(), order
                                .getStepInfo().getJobLabel());
                break;

            case OrderStepProcessed:
                order = entry.getCheckedOrder();
                JOrderProcessed op = (JOrderProcessed) entry.getJOrderEvent();

                event = new FatEventOrderStepProcessed(entry.getEventId(), entry.getEventDate());
                event.set(order.getOrderId(), order.getOutcomeInfo(op.outcome()), order.getWorkflowInfo().getPosition());
                break;

            case OrderOutcomeAdded:
                order = entry.getCheckedOrder();

                event = new FatEventOrderOutcomeAdded(entry.getEventId(), entry.getEventDate());
                event.set(order.getOrderId(), order.getOutcomeInfoOutcomeAdded(), order.getWorkflowInfo().getPosition());
                break;

            case OrderFailed:
                order = entry.getCheckedOrder();

                event = new FatEventOrderFailed(entry.getEventId(), entry.getEventDate());
                event.set(order.getOrderId(), order.getOutcomeInfoFailed(), order.getWorkflowInfo().getPosition());
                break;

            case OrderStopped:
                order = entry.getCheckedOrder();

                event = new FatEventOrderStopped(entry.getEventId(), entry.getEventDate());
                event.set(order.getOrderId(), order.getOutcomeInfoFailed(), order.getWorkflowInfo().getPosition());
                break;

            case OrderBroken:
                order = entry.getCheckedOrder();
                OrderBroken ob = (OrderBroken) entry.getEvent();

                event = new FatEventOrderBroken(entry.getEventId(), entry.getEventDate());
                event.set(order.getOrderId(), order.getOutcomeInfo(OutcomeType.broken, ob.problem()), order.getWorkflowInfo().getPosition());
                break;

            case OrderSuspended:
                order = entry.getCheckedOrderFromPreviousState();

                Instruction cin = null;
                if (!order.isMarked()) {
                    cin = order.getCurrentPositionInstruction();
                }

                event = new FatEventOrderSuspended(entry.getEventId(), entry.getEventDate(), cin, order.isStarted());
                event.set(order.getOrderId(), null, order.getWorkflowInfo().getPosition());
                break;

            case OrderSuspensionMarked:
                order = entry.getCheckedOrder();

                event = new FatEventOrderSuspensionMarked(entry.getEventId(), entry.getEventDate(), order.isStarted());
                event.set(order.getOrderId(), null, order.getWorkflowInfo().getPosition());
                break;

            case OrderResumed:
                order = entry.getCheckedOrder();

                event = new FatEventOrderResumed(entry.getEventId(), entry.getEventDate(), order.getCurrentPositionInstruction(), order.wasStarted());
                event.set(order.getOrderId(), null, order.getWorkflowInfo().getPosition());
                break;

            case OrderResumptionMarked:
                order = entry.getCheckedOrder();

                event = new FatEventOrderResumptionMarked(entry.getEventId(), entry.getEventDate(), order.wasStarted());
                event.set(order.getOrderId(), null, order.getWorkflowInfo().getPosition());
                break;

            case OrderFinished:
                order = entry.getCheckedOrder();

                event = new FatEventOrderFinished(entry.getEventId(), entry.getEventDate());
                event.set(order.getOrderId(), order.getOutcomeInfoFinished(), order.getWorkflowInfo().getPosition());
                break;

            case OrderCancelled:
                order = entry.getCheckedOrder();

                HistoryOrder orderFromPrevState = null;
                boolean isStarted = order.isStarted();
                if (!isStarted) {
                    orderFromPrevState = entry.getCheckedOrderFromPreviousState();
                    isStarted = orderFromPrevState.isStarted();
                }

                if (isStarted) {
                    event = new FatEventOrderCancelled(entry.getEventId(), entry.getEventDate());
                    event.set(order.getOrderId(), null, order.getWorkflowInfo().getPosition());
                } else {
                    deleteNotStartedOrderLog(orderFromPrevState);
                    event = new FatEventEmpty();
                }
                break;

            case OrderLocksAcquired:
                order = entry.getCheckedOrder();

                ol = order.getOrderLocks((OrderLocksAcquired) entry.getEvent());
                event = new FatEventOrderLocksAcquired(entry.getEventId(), entry.getEventDate(), order.getOrderId(), order.getWorkflowInfo()
                        .getPosition(), ol);
                break;

            case OrderLocksQueued:
                order = entry.getCheckedOrder();

                ol = order.getOrderLocks((OrderLocksQueued) entry.getEvent());
                event = new FatEventOrderLocksQueued(entry.getEventId(), entry.getEventDate(), order.getOrderId(), order.getWorkflowInfo()
                        .getPosition(), ol);
                break;

            case OrderLocksReleased:
                order = entry.getCheckedOrder();

                ol = order.getOrderLocks((OrderLocksReleased) entry.getEvent());
                event = new FatEventOrderLocksReleased(entry.getEventId(), entry.getEventDate(), order.getOrderId(), order.getWorkflowInfo()
                        .getPosition(), ol);
                break;

            case OrderNoticesConsumed:
                order = entry.getCheckedOrder();
                event = new FatEventOrderNoticesConsumed(entry.getEventId(), entry.getEventDate(), order.getOrderId(), order.getWorkflowInfo()
                        .getPosition(), ((OrderNoticesConsumed) entry.getEvent()).failed());
                break;

            case OrderNoticesConsumptionStarted:
                order = entry.getCheckedOrder();
                event = new FatEventOrderNoticesConsumptionStarted(entry.getEventId(), entry.getEventDate(), order.getOrderId(), order
                        .getWorkflowInfo().getPosition(), order.getConsumingNotices());
                break;

            // if expected notice(s) exists
            case OrderNoticesRead:
                order = entry.getCheckedOrder();
                event = new FatEventOrderNoticesRead(entry.getEventId(), entry.getEventDate(), order.getOrderId(), order.getWorkflowInfo()
                        .getPosition(), order.readNotices());
                break;

            // if expected notice(s) not exist
            case OrderNoticesExpected:
                order = entry.getCheckedOrder();
                event = new FatEventOrderNoticesExpected(entry.getEventId(), entry.getEventDate(), order.getOrderId(), order.getWorkflowInfo()
                        .getPosition(), order.getExpectNotices());
                break;

            case OrderNoticePosted:
                order = entry.getCheckedOrder();
                event = new FatEventOrderNoticePosted(entry.getEventId(), entry.getEventDate(), order.getOrderId(), order.getWorkflowInfo()
                        .getPosition(), order.getPostNotice());
                break;

            case OrderCaught:
                order = entry.getCheckedOrder();
                event = new FatEventOrderCaught(entry.getEventId(), entry.getEventDate(), order.getOrderId(), order.getWorkflowInfo().getPosition(),
                        order.getCurrentPositionInstruction());
                break;

            case OrderRetrying:
                order = entry.getCheckedOrder();
                OrderRetrying or = (OrderRetrying) entry.getEvent();
                event = new FatEventOrderRetrying(entry.getEventId(), entry.getEventDate(), order.getOrderId(), order.getWorkflowInfo().getPosition(),
                        HistoryEventEntry.getDate(or.delayedUntil()));
                break;

            case OrderPrompted:
                order = entry.getCheckedOrder();
                OrderPrompted opp = (OrderPrompted) entry.getEvent();
                event = new FatEventOrderPrompted(entry.getEventId(), entry.getEventDate(), order.getOrderId(), order.getWorkflowInfo().getPosition(),
                        HistoryEventEntry.getStringValue(opp.question()));
                break;

            case OrderPromptAnswered:
                order = entry.getCheckedOrder();
                event = new FatEventOrderPromptAnswered(entry.getEventId(), entry.getEventDate(), order.getOrderId(), order.getWorkflowInfo()
                        .getPosition());
                break;

            default:
                event = new FatEventWithProblem(entry, null, new Exception("unknown type=" + entry.getEventType()));
                break;
            }

        } catch (Throwable e) {
            // Flux.error(e);
            String orderId = order == null ? null : order.getOrderId();
            if (entry == null) {
                event = new FatEventWithProblem(entry, orderId, e);
            } else {
                event = new FatEventWithProblem(entry, orderId, e, entry.getEventId(), entry.getEventDate());
            }
        }
        return event;
    }

    private void deleteNotStartedOrderLog(HistoryOrder order) {
        // TODO why check !order.isSuspended() ???
        // if (order == null || !order.isSuspended()) {
        if (order == null || order.getOrderId() == null) {
            return;
        }
        try {
            Path file = HistoryUtil.getOrderLog(config.getLogDirTmpOrders(), order.getOrderId());
            if (file != null) {
                SOSPath.deleteIfExists(file);
            }
        } catch (Throwable e) {
        }
    }

    @SuppressWarnings("unused")
    private void fluxDoOnNext(JEventAndControllerState<Event> state) {
        // releaseEvents(model.getStoredEventId());
        JocClusterServiceLogger.setLogger(serviceIdentifier);
        LOGGER.info(String.format("[%s][fluxDoOnNext]%s", controllerId, SOSString.toString(state)));
    }

    private void fluxDoOnCancel() {
        JocClusterServiceLogger.setLogger(serviceIdentifier);
        LOGGER.debug(String.format("[%s][fluxDoOnCancel]", controllerId));
    }

    private Throwable fluxDoOnError(Throwable t) {
        JocClusterServiceLogger.setLogger(serviceIdentifier);
        LOGGER.info(String.format("[%s][fluxDoOnError]%s", controllerId, t.toString()));
        return t;
    }

    private void fluxDoOnComplete() {
        JocClusterServiceLogger.setLogger(serviceIdentifier);
        LOGGER.info(String.format("[%s][fluxDoOnComplete]", controllerId));
    }

    private void fluxDoFinally(SignalType type) {
        JocClusterServiceLogger.setLogger(serviceIdentifier);
        LOGGER.info(String.format("[%s][fluxDoFinally]SignalType=%s", controllerId, type));
    }

    private boolean isProblemException(Throwable t) {
        return t != null && t instanceof ProblemException;
    }

    private boolean isTornException(ProblemException ex) {
        try {
            Optional<ProblemCode> code = JProblem.apply(ex.problem()).maybeCode();
            if (code.isPresent()) {
                if (code.get().string().matches(TORN_PROBLEM_CODE_REGEXP)) {
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

    private boolean isHistoryProcessingException(Throwable t) {
        return t instanceof HistoryProcessingException;
    }

    private void waitFor(long seconds) {
        if (!closed.get() && seconds > 0) {
            String method = getMethodName("waitFor");
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s%ss ...", method, seconds));
            }
            try {
                synchronized (lockObject) {
                    lockObject.wait(seconds * 1_000);
                }
            } catch (InterruptedException e) {
                if (closed.get()) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("%ssleep interrupted due to handler close", method));
                    }
                } else {
                    LOGGER.info(String.format("%s%s", method, e.toString()), e);
                }
            }
        }
    }

    private String getMethodName(String name) {
        String prefix = identifier == null ? "" : String.format("[%s]", identifier);
        return String.format("%s[%s]", prefix, name);
    }

    public void close(StartupMode mode) {
        doClose();
        if (model != null) {
            model.close(mode);
        }
    }

    public void doClose() {
        closed.set(true);
        stopFlux();
        synchronized (lockObject) {
            lockObject.notifyAll();
        }
    }

    private void stopFlux() {
        try {
            if (stopper != null) {
                stopper.stop();
            }
        } catch (Throwable e) {
            LOGGER.warn("[stopFlux]" + e.toString(), e);
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
                waitFor(config.getWaitIntervalOnError());
            }
        }
    }

    private void initIntervals(JocHistoryConfiguration hc) {
        releaseEventsInterval = hc.getReleaseEventsInterval();
        lastReleaseEventId = 0L;
        lastReleaseEvents = SOSDate.getSeconds(new Date());

        lastClearCache = lastReleaseEvents;
    }

    private void releaseEvents(Long eventId) {
        if (eventId != null && eventId > 0 && lastReleaseEvents > 0 && !eventId.equals(lastReleaseEventId)) {
            Long current = SOSDate.getSeconds(new Date());
            if ((current - lastReleaseEvents) >= releaseEventsInterval) {
                String method = "releaseEvents";
                try {
                    LOGGER.info(String.format("[%s][%s]%s", getIdentifier(), method, eventId));
                    // js7.data_for_java.vavr.VavrUtils.await(api.releaseEvents(eventId));
                    api.releaseEvents(eventId);
                    lastReleaseEventId = eventId;
                } catch (Throwable t) {
                    LOGGER.error(String.format("[%s][%s][%s]%s", getIdentifier(), method, eventId, t.toString()));
                } finally {
                    lastReleaseEvents = current;
                }
            }
        }
    }

    private void clearCache() {
        if (model != null) {
            if (lastClearCache > 0) {
                Long current = SOSDate.getSeconds(new Date());
                if ((current - lastClearCache) >= model.getHistoryConfiguration().getCacheAge()) {
                    model.getCacheHandler().clear(current, model.getHistoryConfiguration().getCacheAge());
                }
            }
        }
    }

}
