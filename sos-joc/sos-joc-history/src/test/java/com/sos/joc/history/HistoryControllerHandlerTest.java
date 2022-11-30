package com.sos.joc.history;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSString;
import com.sos.joc.classes.proxy.ProxyUser;
import com.sos.joc.history.controller.configuration.HistoryConfiguration;
import com.sos.joc.history.controller.proxy.EventFluxStopper;
import com.sos.joc.history.controller.proxy.HistoryEventEntry;
import com.sos.joc.history.controller.proxy.HistoryEventEntry.AgentInfo;
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
import com.sos.joc.history.controller.proxy.fatevent.FatEventAgentCouplingFailed;
import com.sos.joc.history.controller.proxy.fatevent.FatEventAgentReady;
import com.sos.joc.history.controller.proxy.fatevent.FatEventAgentShutDown;
import com.sos.joc.history.controller.proxy.fatevent.FatEventAgentSubagentDedicated;
import com.sos.joc.history.controller.proxy.fatevent.FatEventClusterCoupled;
import com.sos.joc.history.controller.proxy.fatevent.FatEventControllerReady;
import com.sos.joc.history.controller.proxy.fatevent.FatEventControllerShutDown;
import com.sos.joc.history.controller.proxy.fatevent.FatEventEmpty;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderBroken;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderCancelled;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderCaught;
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
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderResumed;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderResumptionMarked;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderRetrying;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderStarted;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderStepProcessed;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderStepStarted;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderStepStdWritten;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderStepStdWritten.StdType;
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
import reactor.core.publisher.SignalType;

public class HistoryControllerHandlerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryControllerHandlerTest.class);

    private static final String CONTROLLER_URI_PRIMARY = "http://localhost:5444";
    private static final String CONTROLLER_ID = "js7.x";
    private static final int MAX_EXECUTION_TIME = 20; // seconds
    private static final int SIMULATE_LONG_EXECUTION_INTERVAL = 0; // seconds
    private static final Long START_EVENT_ID = 0L;

    private HistoryConfiguration config = new HistoryConfiguration();
    private EventFluxStopper stopper;
    private AtomicBoolean closed;

    @Ignore
    @Test
    public void testGetEvents() throws Exception {
        JProxyTestClass proxy = new JProxyTestClass();
        JControllerApi api = null;

        stopper = new EventFluxStopper();
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
            }
            throw e;
        } finally {
            proxy.close();
        }
    }

    private synchronized Long process(JControllerApi api, Long eventId) throws Exception {
        try (JStandardEventBus<ProxyEvent> eventBus = new JStandardEventBus<>(ProxyEvent.class)) {
            LOGGER.info("[flux][START]");
            Flux<JEventAndControllerState<Event>> flux = api.eventFlux(eventBus, OptionalLong.of(eventId));

            // flux = flux.doOnNext(e -> LOGGER.info(e.stampedEvent().value().event().getClass().getSimpleName()));
            // flux = flux.doOnNext(e -> LOGGER.info(SOSString.toString(e)));

            flux = flux.filter(e -> HistoryEventType.fromValue(e.stampedEvent().value().event().getClass().getSimpleName()) != null);

            // flux = flux.doOnNext(this::fluxDoOnNext);
            flux = flux.doOnError(this::fluxDoOnError);
            flux = flux.doOnComplete(this::fluxDoOnComplete);
            flux = flux.doOnCancel(this::fluxDoOnCancel);
            flux = flux.doFinally(this::fluxDoFinally);
            flux = flux.onErrorStop();
            flux.takeUntilOther(stopper.stopped()).map(this::map2fat).filter(e -> e.getEventId() != null).bufferTimeout(1000, Duration.ofSeconds(1))
                    .toIterable().forEach(list -> {
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
                LOGGER.info(SOSString.toString(event));
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
        AFatEvent event = null;
        HistoryEventEntry entry = null;
        try {
            entry = new HistoryEventEntry(eventAndState);
            HistoryOrder order;
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
                        event = new FatEventEmpty();
                    } else {
                        order = entry.getCheckedOrderFromPreviousState();
                        event = new FatEventOrderMoved(entry.getEventId(), entry.getEventDate(), order.getOrderId(), order.getWorkflowInfo()
                                .getPosition(), om, order.getCurrentPositionInstruction(), order.isStarted());
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
                Position position = wi.getPosition();
                List<?> positions = position.getUnderlying().toList();
                childs = new ArrayList<FatForkedChild>();
                jof.children().forEach(c -> {
                    String branchIdOrName = null;
                    if (c.branchId().isPresent()) {
                        branchIdOrName = c.branchId().get().string();
                    } else {
                        branchIdOrName = HistoryUtil.getForkChildNameFromOrderId(c.orderId().string());
                    }
                    // copy
                    List<Object> childPositions = positions.stream().collect(Collectors.toList());
                    childPositions.add("fork+" + branchIdOrName);
                    childPositions.add(0);
                    childs.add(new FatForkedChild(c.orderId().string(), branchIdOrName, wi.createNewPosition(childPositions)));
                });
                event = new FatEventOrderForked(entry.getEventId(), entry.getEventDate());
                event.set(order.getOrderId(), wi.getPath(), wi.getVersionId(), position, null, childs);
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

    private void deleteNotStartedOrderLog(HistoryOrder order) {
        if (order == null || !order.isSuspended()) {
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

}
