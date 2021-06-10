package com.sos.js7.history.controller.proxy;

import java.util.HashMap;
import java.util.Map;

import js7.data.agent.AgentRefStateEvent.AgentCouplingFailed;
import js7.data.agent.AgentRefStateEvent.AgentReady;
import js7.data.controller.ControllerEvent.ControllerReady;
import js7.data.controller.ControllerEvent.ControllerShutDown;
import js7.data.order.OrderEvent.OrderBroken;
import js7.data.order.OrderEvent.OrderCancelled$;
import js7.data.order.OrderEvent.OrderFailed;
import js7.data.order.OrderEvent.OrderFinished$;
import js7.data.order.OrderEvent.OrderForked;
import js7.data.order.OrderEvent.OrderJoined;
import js7.data.order.OrderEvent.OrderLockAcquired;
import js7.data.order.OrderEvent.OrderLockQueued;
import js7.data.order.OrderEvent.OrderLockReleased;
import js7.data.order.OrderEvent.OrderProcessed;
import js7.data.order.OrderEvent.OrderProcessingStarted$;
import js7.data.order.OrderEvent.OrderResumed;
import js7.data.order.OrderEvent.OrderResumptionMarked;
import js7.data.order.OrderEvent.OrderStarted$;
import js7.data.order.OrderEvent.OrderStderrWritten;
import js7.data.order.OrderEvent.OrderStdoutWritten;
import js7.data.order.OrderEvent.OrderSuspended$;
import js7.data.order.OrderEvent.OrderSuspensionMarked;

public enum HistoryEventType {
    EventWithProblem("EventWithProblem"),// special case for events with problems

    ControllerReady(ControllerReady.class.getSimpleName()),

    ControllerShutDown(ControllerShutDown.class.getSimpleName()),

    AgentCouplingFailed(AgentCouplingFailed.class.getSimpleName()),

    AgentReady(AgentReady.class.getSimpleName()),

    OrderStarted(OrderStarted$.class.getSimpleName()),

    OrderForked(OrderForked.class.getSimpleName()),

    OrderJoined(OrderJoined.class.getSimpleName()),

    OrderCancelled(OrderCancelled$.class.getSimpleName()),

    OrderFailed(OrderFailed.class.getSimpleName()),

    OrderBroken(OrderBroken.class.getSimpleName()),

    OrderSuspended(OrderSuspended$.class.getSimpleName()),

    OrderSuspendMarked(OrderSuspensionMarked.class.getSimpleName()),

    OrderResumed(OrderResumed.class.getSimpleName()),

    OrderResumeMarked(OrderResumptionMarked.class.getSimpleName()),

    OrderLockAcquired(OrderLockAcquired.class.getSimpleName()),

    OrderLockQueued(OrderLockQueued.class.getSimpleName()),

    OrderLockReleased(OrderLockReleased.class.getSimpleName()),

    OrderFinished(OrderFinished$.class.getSimpleName()),

    OrderStepStarted(OrderProcessingStarted$.class.getSimpleName()),

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
