package com.sos.joc.history;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.OptionalLong;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSString;
import com.sos.joc.classes.proxy.ProxyUser;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.configuration.JocHistoryConfiguration;
import com.sos.joc.cluster.service.JocClusterServiceLogger;
import com.sos.joc.history.controller.proxy.FluxStopper;
import com.sos.joc.history.controller.proxy.HistoryEventEntry;
import com.sos.joc.history.controller.proxy.HistoryEventEntry.AgentInfo;
import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryAgentCoupled;
import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryAgentCouplingFailed;
import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryAgentReady;
import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryAgentShutDown;
import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryClusterCoupled;
import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryControllerReady;
import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryOrder;
import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryOrder.OrderLock;
import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryOrder.WorkflowInfo;
import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryOrder.WorkflowInfo.Position;
import com.sos.joc.history.controller.proxy.HistoryEventEntry.OutcomeType;
import com.sos.joc.history.controller.proxy.HistoryEventType;
import com.sos.joc.history.controller.proxy.common.JProxyTestClass;
import com.sos.joc.history.controller.proxy.fatevent.AFatEvent;
import com.sos.joc.history.controller.proxy.fatevent.FatEventAgentCoupled;
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
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderOrderAdded;
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
import js7.proxy.data.event.ProxyEvent;
import js7.proxy.javaapi.JControllerApi;
import js7.proxy.javaapi.data.controller.JEventAndControllerState;
import js7.proxy.javaapi.eventbus.JStandardEventBus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.core.scheduler.Schedulers;
import reactor.util.Loggers;

public class HistoryControllerHandlerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryControllerHandlerTest.class);
    // Controller and Flux configuration
    private static final String CONTROLLER_URI_PRIMARY = "http://localhost:5444";
    private static final String CONTROLLER_ID = "js7.x";
    private static final Long START_EVENT_ID = 0L;// 1756121197867003L;
    // Flux - Collect incoming values into a List that will be pushed into the returned Flux every timespan OR maxSize items.
    private int BUFFER_TIMEOUT_MAXSIZE = 1_000; // the max collected size
    // If the logger is configured at TRACE level:
    // - the log may contain 1-second tick lines like
    // ...[TRACE][JS7 Proxy-1][js7.common.http.PekkoHttpClient] (PekkoHttpClient.scala:504) - <-<- #3 »⏎« 🩶
    // -- unfortunately, this is not a Flux "heartbeat" to check if the Flux is alive;
    // -- it is only internal Proxy output (from PekkoHttpClient), not Reactor itself
    // -- to identify this - set BUFFER_TIMEOUT_MAXTIME > 1
    private int BUFFER_TIMEOUT_MAXTIME = 1; // the timeout in seconds to use to release a buffered list

    // Test execution
    private static final int MAX_EXECUTION_TIME = 30; // seconds
    private static final int SIMULATE_LONG_EXECUTION_INTERVAL = 0; // seconds

    // creates outputs like: .. - MeterId{name='history-flux.requested', tags=[tag(type=Flux)]} -> [Measurement{statistic='COUNT', value=59.0},
    // Measurement{statistic='TOTAL', value=314.0}, Measurement{statistic='MAX', value=256.0}]; MeterId{name='history-flux.subscribed', tags=[tag(type=Flux)]}
    // -> [Measurement{statistic='COUNT', value=1.0}]; MeterId{name='history-flux.onNext.delay', tags=[tag(type=Flux)]} -> [Measurement{statistic='COUNT',
    // value=58.0}, Measurement{statistic='TOTAL_TIME', value=1691.2287}, Measurement{statistic='MAX', value=1095.5761}]
    // needs additional jars:
    // - reactor
    // -- reactor-core-micrometer-1.1.3.jar
    // --- import reactor.core.observability.micrometer.Micrometer;
    // - micrometer
    // -- micrometer-commons-1.14.10.jar
    // -- micrometer-core-1.14.10.jar
    // --- import io.micrometer.core.instrument.MeterRegistry;
    // --- import io.micrometer.core.instrument.logging.LoggingMeterRegistry;
    // -- micrometer-observation-1.14.10.jar
    private static boolean USE_METRICS = false;

    private JocHistoryConfiguration config = new JocHistoryConfiguration();
    private FluxStopper stopper;
    private AtomicBoolean closed;
    private String serviceIdentifier = "history";

    @Ignore
    @Test
    public void testGetEvents() throws Exception {
        JProxyTestClass proxy = new JProxyTestClass();
        JControllerApi api = null;

        stopper = new FluxStopper();
        closed = new AtomicBoolean();
        try {
            api = proxy.getControllerApi(ProxyUser.HISTORY, CONTROLLER_URI_PRIMARY);
            setStopper();

            // while (!closed.get()) {
            process(api, START_EVENT_ID);
            // }

        } catch (Throwable e) {
            LOGGER.error(String.format("[exception]%s", e.toString()), e);
            try {
                Long id = api.journalInfo().thenApply(o -> o.get().tornEventId()).get();
                LOGGER.info(String.format("[tornEventId]%s", id));
            } catch (Throwable ee) {
                LOGGER.error(String.format("[tornEventId]%s", ee.toString()), ee);
            }
            throw e;
        } finally {
            proxy.close();
        }
    }

    private synchronized Long process(JControllerApi api, Long eventId) throws Exception {
        LOGGER.info("[flux][START]CONTROLLER_URI_PRIMARY=" + CONTROLLER_URI_PRIMARY + "), CONTROLLER_ID=" + CONTROLLER_ID
                + ", BUFFER_TIMEOUT_MAXSIZE=" + BUFFER_TIMEOUT_MAXSIZE + ", BUFFER_TIMEOUT_MAXTIME=" + BUFFER_TIMEOUT_MAXTIME);

        try (JStandardEventBus<ProxyEvent> eventBus = new JStandardEventBus<>(ProxyEvent.class)) {
            // Original Flux
            Flux<JEventAndControllerState<Event>> flux = api.eventFlux(eventBus, OptionalLong.of(eventId));

            MetricsConf metricsConf = getMetricsConf(flux);
            flux = metricsConf.flux;

            if (LOGGER.isTraceEnabled()) {
                // Only actual signals are logged, such as onSubscribe, request(n), onNext(element), onComplete, onError, cancel.
                // - request(1) happens because the history Flux is terminated with .toIterable().forEach
                // - the iterator requests exactly 1 element at a time (step-by-step consumption)
                // Unfortunately, empty buffers (no events emitted) are not logged, so you cannot directly see if the Flux is still "alive" and processing in
                // the background (not blocked).

                // All logs are produced by reactor.util.Loggers.
                // The custom log category (e.g., "history-flux-...") will only be visible in the log output if the Log4j2/SLF4J pattern layout is configured to
                // include %c or %logger (the logger name).

                // flux = flux.log(); <- INFO Level
                // reactor.util.Logger reactorLogger = Loggers.getLogger(LOGGER.getName());
                // flux = flux.log("history-flux-" + CONTROLLER_ID, Level.FINEST);
                flux = flux.log("history-flux-" + CONTROLLER_ID, Level.FINEST, true);
                // flux = flux.log("MYFLUX",java.util.logging.Level.INFO);// flux.log("MYFLUX",Level.FINEST);

            }
            // Step 1 - Process all events in parallel, but use flatMapSequential to ensure that the original events order is maintained
            flux = flux.flatMapSequential(e -> Mono.fromRunnable(() -> {
                // process all events in parallel before filtering
                JocClusterServiceLogger.setLogger(serviceIdentifier);
                if (!closed.get()) {
                    try {
                        if (!"OrderStdoutWritten".equals(e.stampedEvent().value().event().getClass().getSimpleName())) {
                            // LOGGER.info("[FluxEventHandler.processEvent]" + SOSString.toString(e.stampedEvent().value().event()));
                        }
                        // LOGGER.info("[FluxEventHandler.processEvent]" + e.stampedEvent().value().event().getClass().getSimpleName());
                        // FluxEventHandler.processEvent(e, controllerId);
                    } catch (Throwable e1) {
                        LOGGER.info("[FluxEventHandler.processEvent]" + e.toString(), e);
                    }
                }
            }).thenReturn(e).filter(event -> {
                // Step 2 - filter events (in parallel) for history processing
                try {
                    return HistoryEventType.fromValue(event.stampedEvent().value().event().getClass().getSimpleName()) != null;
                } catch (Throwable ex) {
                    JocClusterServiceLogger.setLogger(serviceIdentifier);
                    LOGGER.info("[process][error filtering event]" + ex.toString(), ex);
                    return false;
                }
            }).subscribeOn(Schedulers.fromExecutor(ForkJoinPool.commonPool())));

            // Step 3 - Publish events to another scheduler? currently problem by stop flux - thread IllegalMonitorStateException(NPE)
            // flux = flux.publishOn(Schedulers.single());
            // flux = flux.publishOn(Schedulers.fromExecutor(ForkJoinPool.commonPool()));

            // Error handling and completion
            flux = flux.doOnError(this::fluxDoOnError);
            flux = flux.doOnComplete(this::fluxDoOnComplete);
            flux = flux.doOnCancel(this::fluxDoOnCancel);
            flux = flux.doFinally(this::fluxDoFinally);

            flux.takeUntilOther(stopper.stopped()).map(this::map2fat).filter(e -> e.getEventId() != null).bufferTimeout(BUFFER_TIMEOUT_MAXSIZE,
                    Duration.ofSeconds(BUFFER_TIMEOUT_MAXTIME)).toIterable().forEach(list -> {
                        LOGGER.info("[HANDLE BLOCK][START][" + closed.get() + "]" + list.size());

                        // while (!closed.get()) {
                        if (!closed.get()) {
                            try {
                                handleBlock(list);
                            } catch (Throwable e) {
                                LOGGER.error(e.toString(), e);
                            }
                        }

                        LOGGER.info("[HANDLE BLOCK][END]");
                    });

            JocCluster.shutdownThreadPool("history-metrics", metricsConf.scheduler, 3);

            LOGGER.info("[flux][END]");
            return eventId;
        }
    }

    @SuppressWarnings("unused")
    private void handleBlock(List<AFatEvent> list) throws Exception {
        for (AFatEvent event : list) {
            if (event instanceof FatEventOrderStepStdWritten) {
                // FatEventOrderStepProcessed p = (FatEventOrderStepProcessed) event;
                // LOGGER.info(SOSString.toString(event));
                // LOGGER.info("----" + SOSString.toString(p.getOutcome()));
                // try {
                // LOGGER.info("-----------" + p.getOutcome().getNamedValuesAsJsonString());
                // } catch (JsonProcessingException e1) {
                // e1.printStackTrace();
                // }

            } else {
                LOGGER.info(SOSString.toString(event, true));
            }
            if (SIMULATE_LONG_EXECUTION_INTERVAL > 0) {
                try {
                    TimeUnit.SECONDS.sleep(SIMULATE_LONG_EXECUTION_INTERVAL);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    private AFatEvent map2fat(JEventAndControllerState<Event> eventAndState) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("[history][map2fat][controllerId=" + CONTROLLER_ID + "]" + eventAndState);
        }
        AFatEvent event = null;
        HistoryEventEntry entry = null;
        HistoryOrder order = null;
        try {
            entry = new HistoryEventEntry(CONTROLLER_ID, eventAndState);
            List<FatForkedChild> childs;
            List<OrderLock> ol;
            switch (entry.getEventType()) {
            case ClusterCoupled:
                HistoryClusterCoupled cc = entry.getClusterCoupled();

                event = new FatEventClusterCoupled(entry.getEventId(), entry.getEventDate());
                event.set(CONTROLLER_ID, cc.getActiveId(), cc.isPrimary());
                break;

            case ControllerReady:
                HistoryControllerReady cr = entry.getControllerReady();

                event = new FatEventControllerReady(entry.getEventId(), entry.getEventDate());
                event.set(CONTROLLER_ID, cr.getTimezone(), cr.getTotalRunningTimeAsMillis());
                break;

            case ControllerShutDown:
                event = new FatEventControllerShutDown(entry.getEventId(), entry.getEventDate());
                event.set(CONTROLLER_ID);
                break;

            case AgentReady:
                HistoryAgentReady ar = entry.getAgentReady();

                event = new FatEventAgentReady(entry.getEventId(), entry.getEventDate());
                event.set(ar.getId(), ar.getUri(), ar.getTimezone());
                break;

            case AgentSubagentDedicated:
                // HistoryAgentSubagentDedicated ad = entry.getAgentSubagentDedicated();
                // ad.postEvent();

                event = new FatEventAgentSubagentDedicated(entry.getEventId(), entry.getEventDate());
                break;

            case AgentCoupled:
                HistoryAgentCoupled ac = entry.getAgentCoupled();

                event = new FatEventAgentCoupled(entry.getEventId(), entry.getEventDate());
                event.set(ac.getId());
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
                        LOGGER.warn(String.format("[OrderMoved][%s]%s", order.getOrderId(), e.toString()), e);
                    } catch (Throwable ee) {
                        LOGGER.warn(String.format("[OrderMoved]%s", e.toString()), e);
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
                        .getPosition(), order.getArguments(), order.getPriority());
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

            case OrderOrderAdded:
                order = entry.getCheckedOrder();
                event = new FatEventOrderOrderAdded(entry.getEventId(), entry.getEventDate(), order.getOrderId(), order.getWorkflowInfo()
                        .getPosition(), order.getOrderAddedInfo());
                break;

            default:
                event = new FatEventWithProblem(entry, null, new Exception("unknown type=" + entry.getEventType()));
                break;
            }

        } catch (Throwable e) {
            e.printStackTrace();
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

    private synchronized void setStopper() {

        Thread thread = new Thread() {

            public void run() {
                closed.set(false);

                String name = Thread.currentThread().getName();
                LOGGER.info(String.format("[%s][start][setStopper][%ss]...", name, MAX_EXECUTION_TIME));

                try {
                    TimeUnit.SECONDS.sleep(MAX_EXECUTION_TIME);
                } catch (InterruptedException e) {

                } finally {
                    closed.set(true);
                    stopper.stop();
                }
                LOGGER.info(String.format("[%s][end][setStopper][%ss]", name, MAX_EXECUTION_TIME));
            }
        };
        thread.start();
    }

    private void fluxDoOnCancel() {
        LOGGER.info("[fluxDoOnCancel]");
    }

    private Throwable fluxDoOnError(Throwable t) {
        LOGGER.warn("[fluxDoOnError]" + t.toString());
        return t;
    }

    private void fluxDoOnComplete() {
        LOGGER.info("[fluxDoOnComplete]");
    }

    private void fluxDoFinally(SignalType type) {
        LOGGER.info("[fluxDoFinally] - " + type);
    }

    private MetricsConf getMetricsConf(Flux<JEventAndControllerState<Event>> flux) {
        if (!USE_METRICS || !LOGGER.isTraceEnabled()) {
            return new MetricsConf(flux, null);
        }
        // MeterRegistry meterRegistry = new LoggingMeterRegistry();// new SimpleMeterRegistry();

        // Name für die Metriken + Metriken aktivieren
        // MeterRegistry meterRegistry = new LoggingMeterRegistry();
        // flux = flux.name("history-flux").tap(Micrometer.metrics(meterRegistry));

        // ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        // scheduler.scheduleAtFixedRate(() -> {
        // LOGGER.trace(meterRegistry.getMeters().stream().map(meter -> meter.getId() + " -> " + meter.measure()).reduce((a, b) -> a + "; " + b)
        // .orElse("no meters"));
        // }, 0, 1, TimeUnit.SECONDS);
        // return new MetricsConf(flux, scheduler);

        return new MetricsConf(flux, null);
    }

    private class MetricsConf {

        private final Flux<JEventAndControllerState<Event>> flux;
        private final ScheduledExecutorService scheduler;

        private MetricsConf(Flux<JEventAndControllerState<Event>> flux, ScheduledExecutorService scheduler) {
            this.flux = flux;
            this.scheduler = scheduler;
        }
    }

}
