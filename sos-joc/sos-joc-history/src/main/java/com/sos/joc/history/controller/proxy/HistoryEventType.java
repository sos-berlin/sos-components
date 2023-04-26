package com.sos.joc.history.controller.proxy;

import java.util.HashMap;
import java.util.Map;

import js7.data.agent.AgentRefStateEvent.AgentCouplingFailed;
import js7.data.agent.AgentRefStateEvent.AgentReady;
import js7.data.agent.AgentRefStateEvent.AgentShutDown$;
import js7.data.cluster.ClusterEvent.ClusterCoupled;
import js7.data.controller.ControllerEvent.ControllerReady;
import js7.data.controller.ControllerEvent.ControllerShutDown$;
import js7.data.order.OrderEvent.OrderBroken;
import js7.data.order.OrderEvent.OrderCancelled$;
import js7.data.order.OrderEvent.OrderCaught;
import js7.data.order.OrderEvent.OrderFailed;
import js7.data.order.OrderEvent.OrderFinished;
import js7.data.order.OrderEvent.OrderForked;
import js7.data.order.OrderEvent.OrderJoined;
import js7.data.order.OrderEvent.OrderLocksAcquired;
import js7.data.order.OrderEvent.OrderLocksQueued;
import js7.data.order.OrderEvent.OrderLocksReleased;
import js7.data.order.OrderEvent.OrderMoved;
import js7.data.order.OrderEvent.OrderNoticePosted;
import js7.data.order.OrderEvent.OrderNoticesConsumed;
import js7.data.order.OrderEvent.OrderNoticesConsumptionStarted;
import js7.data.order.OrderEvent.OrderNoticesExpected;
import js7.data.order.OrderEvent.OrderNoticesRead$;
import js7.data.order.OrderEvent.OrderOutcomeAdded;
import js7.data.order.OrderEvent.OrderProcessed;
import js7.data.order.OrderEvent.OrderProcessingStarted;
import js7.data.order.OrderEvent.OrderPrompted;
import js7.data.order.OrderEvent.OrderPromptAnswered;
import js7.data.order.OrderEvent.OrderResumed;
import js7.data.order.OrderEvent.OrderResumptionMarked;
import js7.data.order.OrderEvent.OrderRetrying;
import js7.data.order.OrderEvent.OrderStarted$;
import js7.data.order.OrderEvent.OrderStderrWritten;
import js7.data.order.OrderEvent.OrderStdoutWritten;
import js7.data.order.OrderEvent.OrderStopped$;
import js7.data.order.OrderEvent.OrderSuspended$;
import js7.data.order.OrderEvent.OrderSuspensionMarked;
import js7.data.subagent.SubagentItemStateEvent.SubagentDedicated;

public enum HistoryEventType {

    Empty("Empty"),// special case for skipped events

    EventWithProblem("EventWithProblem"),// special case for events with problems

    ControllerReady(ControllerReady.class.getSimpleName()),

    ControllerShutDown(ControllerShutDown$.class.getSimpleName()),

    ClusterCoupled(ClusterCoupled.class.getSimpleName()),

    AgentReady(AgentReady.class.getSimpleName()),

    AgentSubagentDedicated(SubagentDedicated.class.getSimpleName()),

    AgentCouplingFailed(AgentCouplingFailed.class.getSimpleName()),

    AgentShutDown(AgentShutDown$.class.getSimpleName()),

    OrderStarted(OrderStarted$.class.getSimpleName()),

    OrderForked(OrderForked.class.getSimpleName()),

    OrderJoined(OrderJoined.class.getSimpleName()),

    OrderCancelled(OrderCancelled$.class.getSimpleName()),

    OrderOutcomeAdded(OrderOutcomeAdded.class.getSimpleName()),

    OrderFailed(OrderFailed.class.getSimpleName()),

    OrderStopped(OrderStopped$.class.getSimpleName()),

    OrderBroken(OrderBroken.class.getSimpleName()),

    OrderSuspended(OrderSuspended$.class.getSimpleName()),

    OrderSuspensionMarked(OrderSuspensionMarked.class.getSimpleName()),

    OrderResumed(OrderResumed.class.getSimpleName()),

    OrderResumptionMarked(OrderResumptionMarked.class.getSimpleName()),

    OrderLocksAcquired(OrderLocksAcquired.class.getSimpleName()),

    OrderLocksQueued(OrderLocksQueued.class.getSimpleName()),

    OrderLocksReleased(OrderLocksReleased.class.getSimpleName()),

    OrderNoticesConsumed(OrderNoticesConsumed.class.getSimpleName()),

    OrderNoticesConsumptionStarted(OrderNoticesConsumptionStarted.class.getSimpleName()),

    // expected notices - if exists
    OrderNoticesRead(OrderNoticesRead$.class.getSimpleName()),
    // expected notices - if waiting for
    OrderNoticesExpected(OrderNoticesExpected.class.getSimpleName()),

    OrderNoticePosted(OrderNoticePosted.class.getSimpleName()),

    // catch
    OrderCaught(OrderCaught.class.getSimpleName()),

    OrderRetrying(OrderRetrying.class.getSimpleName()),

    OrderFinished(OrderFinished.class.getSimpleName()),

    OrderMoved(OrderMoved.class.getSimpleName()),

    OrderPrompted(OrderPrompted.class.getSimpleName()),

    OrderPromptAnswered(OrderPromptAnswered.class.getSimpleName()),

    OrderStepStarted(OrderProcessingStarted.class.getSimpleName()),

    OrderStepStdoutWritten(OrderStdoutWritten.class.getSimpleName()),

    OrderStepStderrWritten(OrderStderrWritten.class.getSimpleName()),

    OrderStepProcessed(OrderProcessed.class.getSimpleName());

    private final static Map<String, HistoryEventType> VALUES = new HashMap<String, HistoryEventType>();

    private final String value;

    static {
        for (HistoryEventType v : values()) {
            VALUES.put(v.value, v);
        }
    }

    private HistoryEventType(String val) {
        value = val;
    }

    public String value() {
        return value;
    }

    public static HistoryEventType fromValue(String value) {
        return VALUES.get(value);
    }
}
