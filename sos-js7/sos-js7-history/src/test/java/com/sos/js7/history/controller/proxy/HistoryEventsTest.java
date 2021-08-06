package com.sos.js7.history.controller.proxy;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.util.SOSString;
import com.sos.joc.classes.proxy.ProxyUser;
import com.sos.js7.history.controller.proxy.HistoryEventEntry.HistoryAgentCouplingFailed;
import com.sos.js7.history.controller.proxy.HistoryEventEntry.HistoryAgentReady;
import com.sos.js7.history.controller.proxy.HistoryEventEntry.HistoryClusterCoupled;
import com.sos.js7.history.controller.proxy.HistoryEventEntry.HistoryControllerReady;
import com.sos.js7.history.controller.proxy.HistoryEventEntry.HistoryOrder;
import com.sos.js7.history.controller.proxy.HistoryEventEntry.HistoryOrder.OrderLock;
import com.sos.js7.history.controller.proxy.HistoryEventEntry.HistoryOrder.OutcomeInfo;
import com.sos.js7.history.controller.proxy.HistoryEventEntry.HistoryOrder.WorkflowInfo;
import com.sos.js7.history.controller.proxy.HistoryEventEntry.HistoryOrder.WorkflowInfo.Position;
import com.sos.js7.history.controller.proxy.HistoryEventEntry.OutcomeType;
import com.sos.js7.history.controller.proxy.common.JProxyTestClass;
import com.sos.js7.history.controller.proxy.fatevent.AFatEvent;
import com.sos.js7.history.controller.proxy.fatevent.FatEventAgentCouplingFailed;
import com.sos.js7.history.controller.proxy.fatevent.FatEventAgentReady;
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
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderSuspendMarked;
import com.sos.js7.history.controller.proxy.fatevent.FatEventOrderSuspended;
import com.sos.js7.history.controller.proxy.fatevent.FatEventWithProblem;
import com.sos.js7.history.controller.proxy.fatevent.FatForkedChild;
import com.sos.js7.history.controller.proxy.fatevent.FatOutcome;

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
import js7.proxy.data.event.ProxyEvent;
import js7.proxy.javaapi.JControllerApi;
import js7.proxy.javaapi.data.controller.JEventAndControllerState;
import js7.proxy.javaapi.eventbus.JStandardEventBus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;

public class HistoryEventsTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryEventsTest.class);

    private static final String CONTROLLER_URI_PRIMARY = "http://localhost:5444";
    private static final String CONTROLLER_ID = "js7.x";
    private static final int MAX_EXECUTION_TIME = 30; // seconds
    private static final int CHECKER_REFRESH_INTERVAL = 10; // seconds
    private static final Long START_EVENT_ID = 1626690343147000L;

    private EventFluxStopper stopper = new EventFluxStopper();
    private AtomicBoolean stopped = new AtomicBoolean();
    private AtomicLong lastExecution = new AtomicLong();

    @Ignore
    @Test
    public void testGetEvents() throws Exception {
        JProxyTestClass proxy = new JProxyTestClass();
        JControllerApi api = null;
        try {
            api = proxy.getControllerApi(ProxyUser.JOC, CONTROLLER_URI_PRIMARY);

            setStopper(stopper);
            setChecker();
            process(api, START_EVENT_ID);

        } catch (Throwable e) {
            try {
                Long id = api.journalInfo().thenApply(o -> o.get().tornEventId()).get();
                LOGGER.info(String.format("[tornEventId]%s", id));
            } catch (Throwable ee) {
            }
            throw e;
        } finally {
            proxy.close();
        }
    }

    public Long process(JControllerApi api, Long eventId) throws Exception {
        try (JStandardEventBus<ProxyEvent> eventBus = new JStandardEventBus<>(ProxyEvent.class)) {
            Flux<JEventAndControllerState<Event>> flux = api.eventFlux(eventBus, OptionalLong.of(eventId));

            // flux = flux.doOnNext(e -> LOGGER.info(e.stampedEvent().value().event().getClass().getSimpleName()));
            // flux = flux.doOnNext(e -> LOGGER.info(SOSString.toString(e)));

            flux = flux.filter(e -> HistoryEventType.fromValue(e.stampedEvent().value().event().getClass().getSimpleName()) != null);

            // flux = flux.doOnNext(this::fluxDoOnNext);
            flux = flux.doOnError(this::fluxDoOnError);
            flux = flux.doOnComplete(this::fluxDoOnComplete);
            flux = flux.doOnCancel(this::fluxDoOnCancel);
            flux = flux.doFinally(this::fluxDoFinally);

            flux.takeUntilOther(stopper.stopped()).map(this::map2fat).bufferTimeout(1_000, Duration.ofSeconds(2)).toIterable().forEach(list -> {
                LOGGER.info("[HANDLE BLOCK][START]" + list.size());
                for (AFatEvent event : list) {
                    if (event instanceof FatEventOrderStepStdWritten) {
                        // continue;
                    }
                    if (event instanceof FatEventOrderStepProcessed) {
                        FatEventOrderStepProcessed p = (FatEventOrderStepProcessed) event;
                        LOGGER.info(SOSString.toString(event));
                        LOGGER.info("----" + SOSString.toString(p.getOutcome()));
                        try {
                            LOGGER.info("-----------" + p.getOutcome().getNamedValuesAsJsonString());
                        } catch (JsonProcessingException e1) {
                            e1.printStackTrace();
                        }

                    }
                }
                LOGGER.info("[HANDLE BLOCK][END]");
                lastExecution.set(new Date().getTime());
            });
            return eventId;
        }
    }

    public AtomicLong getLastExecution() {
        return lastExecution;
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

            case AgentCouplingFailed:
                HistoryAgentCouplingFailed acf = entry.getAgentCouplingFailed();

                event = new FatEventAgentCouplingFailed(entry.getEventId(), entry.getEventDate());
                event.set(acf.getId(), acf.getMessage());
                break;

            case AgentReady:
                HistoryAgentReady ar = entry.getAgentReady();

                event = new FatEventAgentReady(entry.getEventId(), entry.getEventDate());
                event.set(ar.getId(), ar.getUri(), ar.getTimezone());
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
                    if (c.branchId().isPresent()) {
                        String branchId = c.branchId().get().string();
                        // copy
                        List<Object> childPositions = positions.stream().collect(Collectors.toList());
                        childPositions.add(branchId);
                        childPositions.add(0);
                        childs.add(new FatForkedChild(c.orderId().string(), branchId, wi.createNewPosition(childPositions)));
                    }
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
            e.printStackTrace();
            // Flux.error(e);
            if (entry == null) {
                event = new FatEventWithProblem(entry, e);
            } else {
                event = new FatEventWithProblem(entry, e, entry.getEventId(), entry.getEventDate());
            }
        }
        return event;
    }

    private void setStopper(EventFluxStopper stopper) {
        stopped.set(false);
        Thread thread = new Thread() {

            public void run() {
                String name = Thread.currentThread().getName();
                LOGGER.info(String.format("[%s][start][setStopper][%ss]...", name, MAX_EXECUTION_TIME));

                try {
                    TimeUnit.SECONDS.sleep(MAX_EXECUTION_TIME);
                } catch (InterruptedException e) {

                } finally {
                    stopper.stop();
                }
                stopped.set(true);
                LOGGER.info(String.format("[%s][end][setStopper][%ss]", name, MAX_EXECUTION_TIME));
            }
        };
        thread.start();
    }

    private void setChecker() {
        Thread thread = new Thread() {

            public void run() {
                String name = Thread.currentThread().getName();
                LOGGER.info(String.format("[%s][start][setChecker][%ss]...", name, CHECKER_REFRESH_INTERVAL));

                while (!stopped.get()) {
                    try {
                        TimeUnit.SECONDS.sleep(CHECKER_REFRESH_INTERVAL);

                        LOGGER.info("LAST EXECUTION=" + lastExecution);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                LOGGER.info(String.format("[%s][end][setChecker][%ss]", name, CHECKER_REFRESH_INTERVAL));
            }
        };
        thread.start();
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

}
