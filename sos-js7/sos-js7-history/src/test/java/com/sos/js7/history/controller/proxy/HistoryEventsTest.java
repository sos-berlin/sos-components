package com.sos.js7.history.controller.proxy;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.joc.classes.proxy.ProxyUser;
import com.sos.js7.history.controller.proxy.HistoryEventEntry.HistoryAgentCouplingFailed;
import com.sos.js7.history.controller.proxy.HistoryEventEntry.HistoryAgentReady;
import com.sos.js7.history.controller.proxy.HistoryEventEntry.HistoryControllerReady;
import com.sos.js7.history.controller.proxy.HistoryEventEntry.HistoryOrder;
import com.sos.js7.history.controller.proxy.HistoryEventEntry.HistoryOrder.OrderLock;
import com.sos.js7.history.controller.proxy.HistoryEventEntry.HistoryOrder.OutcomeInfo;
import com.sos.js7.history.controller.proxy.HistoryEventEntry.OutcomeType;
import com.sos.js7.history.controller.proxy.common.JProxyTestClass;
import com.sos.js7.history.controller.proxy.fatevent.AFatEvent;
import com.sos.js7.history.controller.proxy.fatevent.FatEventAgentCouplingFailed;
import com.sos.js7.history.controller.proxy.fatevent.FatEventAgentReady;
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
import js7.proxy.data.ProxyEvent;
import js7.proxy.javaapi.JControllerApi;
import js7.proxy.javaapi.data.controller.JEventAndControllerState;
import js7.proxy.javaapi.data.order.JOrder.Forked;
import js7.proxy.javaapi.data.order.JOrderEvent.JOrderFailed;
import js7.proxy.javaapi.data.order.JOrderEvent.JOrderForked;
import js7.proxy.javaapi.data.order.JOrderEvent.JOrderJoined;
import js7.proxy.javaapi.data.order.JOrderEvent.JOrderProcessed;
import js7.proxy.javaapi.eventbus.JStandardEventBus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;

public class HistoryEventsTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryEventsTest.class);

    private static final String CONTROLLER_URI_PRIMARY = "http://localhost:5444";
    private static final String CONTROLLER_ID = "js7.x";
    private static final int MAX_EXECUTION_TIME = 20; // seconds
    private static final Long START_EVENT_ID = 1611933032992003L;

    private EventFluxStopper stopper = new EventFluxStopper();

    @Ignore
    @Test
    public void testGetEvents() throws Exception {
        JProxyTestClass proxy = new JProxyTestClass();

        try {
            JControllerApi api = proxy.getControllerApi(ProxyUser.JOC, CONTROLLER_URI_PRIMARY);

            setStopper(stopper);
            process(api, new Long(START_EVENT_ID));

        } catch (Throwable e) {
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
                    LOGGER.info(SOSString.toString(event));
                }
                LOGGER.info("[HANDLE BLOCK][END]");
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
            case ControllerReady:
                HistoryControllerReady cr = entry.getControllerReady();

                event = new FatEventControllerReady(entry.getEventId(), entry.getEventDate());
                event.set(CONTROLLER_ID, cr.getTimezone());
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
                Date planned = null;
                try {
                    planned = entry.getCheckedOrderFromPreviousState().getScheduledFor();
                } catch (Throwable e) {
                    LOGGER.warn(String.format("[%s][%s][PreviousState]%s", entry.getEventType().name(), order.getOrderId(), e.toString()), e);
                }
                event = new FatEventOrderStarted(entry.getEventId(), entry.getEventDate());
                event.set(order.getOrderId(), order.getWorkflowInfo().getPath(), order.getWorkflowInfo().getVersionId(), order.getWorkflowInfo()
                        .getPosition().asList(), order.getArguments(), planned);
                break;
            case OrderForked:
                order = entry.getCheckedOrder();
                JOrderForked jof = (JOrderForked) entry.getJOrderEvent();

                List<Object> position = order.getWorkflowInfo().getPosition().asList();
                childs = new ArrayList<FatForkedChild>();
                jof.children().forEach(c -> {
                    String branchId = c.branchId().string();
                    List<Object> childPosition = position.stream().collect(Collectors.toList());
                    childPosition.add("fork+" + branchId);
                    childs.add(new FatForkedChild(c.orderId().string(), branchId, childPosition));
                });
                event = new FatEventOrderForked(entry.getEventId(), entry.getEventDate());
                event.set(order.getOrderId(), order.getWorkflowInfo().getPath(), order.getWorkflowInfo().getVersionId(), position, order
                        .getArguments(), childs);
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

                LOGGER.info("AA:" + SOSString.toString(joj.outcome()));

                outcome = null;
                if (oi != null) {
                    outcome = new FatOutcome(oi.getType(), oi.getReturnCode(), oi.isSucceeded(), oi.isFailed(), oi.getNamedValues(), oi
                            .getErrorCode(), oi.getErrorMessage());
                }

                event = new FatEventOrderJoined(entry.getEventId(), entry.getEventDate());
                event.set(order.getOrderId(), order.getWorkflowInfo().getPath(), order.getWorkflowInfo().getVersionId(), order.getWorkflowInfo()
                        .getPosition().asList(), order.getArguments(), childs, outcome);
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
                        .getPosition().asList(), order.getArguments(), order.getStepInfo().getAgentId(), order.getStepInfo().getJobName());
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
                event.set(order.getOrderId(), outcome, order.getWorkflowInfo().getPosition().asList());
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
                event.set(order.getOrderId(), outcome, order.getWorkflowInfo().getPosition().asList());
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
                event.set(order.getOrderId(), outcome, order.getWorkflowInfo().getPosition().asList());
                break;

            case OrderSuspended:
                order = entry.getOrder();

                event = new FatEventOrderSuspended(entry.getEventId(), entry.getEventDate());
                event.set(order.getOrderId(), null, order.getWorkflowInfo().getPosition().asList());
                break;

            case OrderSuspendMarked:
                order = entry.getCheckedOrder();

                event = new FatEventOrderSuspendMarked(entry.getEventId(), entry.getEventDate());
                event.set(order.getOrderId(), null, order.getWorkflowInfo().getPosition().asList());
                break;

            case OrderResumed:
                order = entry.getOrder();

                event = new FatEventOrderResumed(entry.getEventId(), entry.getEventDate());
                event.set(order.getOrderId());
                break;

            case OrderResumeMarked:
                order = entry.getCheckedOrder();

                event = new FatEventOrderResumeMarked(entry.getEventId(), entry.getEventDate());
                event.set(order.getOrderId(), null, order.getWorkflowInfo().getPosition().asList());
                break;

            case OrderFinished:
                order = entry.getCheckedOrder();

                event = new FatEventOrderFinished(entry.getEventId(), entry.getEventDate());
                event.set(order.getOrderId(), null, order.getWorkflowInfo().getPosition().asList());
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
                event.set(order.getOrderId(), null, order.getWorkflowInfo().getPosition().asList());
                break;

            case OrderLockAcquired:
                order = entry.getCheckedOrder();

                ol = order.getOrderLock((OrderLockAcquired) entry.getEvent());
                event = new FatEventOrderLockAcquired(entry.getEventId(), entry.getEventDate(), order.getOrderId(), ol, order.getWorkflowInfo()
                        .getPosition().asList());

                break;

            case OrderLockQueued:
                order = entry.getCheckedOrder();

                ol = order.getOrderLock((OrderLockQueued) entry.getEvent());
                event = new FatEventOrderLockQueued(entry.getEventId(), entry.getEventDate(), order.getOrderId(), ol, order.getWorkflowInfo()
                        .getPosition().asList());

                break;

            case OrderLockReleased:
                order = entry.getCheckedOrder();

                ol = order.getOrderLock((OrderLockReleased) entry.getEvent());
                event = new FatEventOrderLockReleased(entry.getEventId(), entry.getEventDate(), order.getOrderId(), ol, order.getWorkflowInfo()
                        .getPosition().asList());

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

                LOGGER.info(String.format("[%s][end][setStopper][%ss]", name, MAX_EXECUTION_TIME));
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
