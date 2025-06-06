
package com.sos.controller.model.event;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EventType {

    VersionAdded("VersionAdded"),
    FileBasedChanged("FileBasedChanged"),
    ControllerReady("ControllerReady"),
    AgentReady("AgentReady"),
    OrderAdded("OrderAdded"),
    OrderAttachable("OrderAttachable"),
    OrderAttached("OrderAttached"),
    OrderStarted("OrderStarted"),
    OrderTransferredToAgent("OrderTransferredToAgent"),
    OrderProcessingStarted("OrderProcessingStarted"),
    OrderStdoutWritten("OrderStdoutWritten"),
    OrderStderrWritten("OrderStderrWritten"),
    OrderProcessed("OrderProcessed"),
    OrderResumed("OrderResumed"),
    OrderResumeMarked("OrderResumeMarked"),
    OrderResumptionMarked("OrderResumptionMarked"),
    OrderForked("OrderForked"),
    OrderJoined("OrderJoined"),
    OrderOffered("OrderOffered"),
    OrderRetrying("OrderRetrying"),
    OrderAwaiting("OrderAwaiting"),
    OrderMoved("OrderMoved"),
    OrderDetachable("OrderDetachable"),
    OrderDetached("OrderDetached"),
    OrderOutcomeAdded("OrderOutcomeAdded"),
    OrderFailedinFork("OrderFailedinFork"),
    OrderFailed("OrderFailed"),
    OrderStopped("OrderStopped"),
    OrderCatched("OrderCatched"),
    OrderCaught("OrderCaught"),
    OrderAwoke("OrderAwoke"),
    OrderSuspended("OrderSuspended"),
    OrderSuspendMarked("OrderSuspendMarked"),
    OrderSuspensionMarked("OrderSuspensionMarked"),
    OrderBroken("OrderBroken"),
    OrderCancelled("OrderCancelled"),
    OrderFinished("OrderFinished"),
    OrderLocksAcquired("OrderLocksAcquired"),
    OrderLocksQueued("OrderLocksQueued"),
    OrderLocksReleased("OrderLocksReleased"),
    OrderNoticesConsumed("OrderNoticesConsumed"),
    OrderNoticesConsumptionStarted("OrderNoticesConsumptionStarted"),
    OrderNoticesRead("OrderNoticesRead"),
    OrderNoticesExpected("OrderNoticesExpected"),
    OrderNoticePosted("OrderNoticePosted"),
    OrderPrompted("OrderPrompted"),
    OrderPromptAnswered("OrderPromptAnswered"),
    OrderCyclingPrepared("OrderCyclingPrepared"),
    OrderOrderAdded("OrderOrderAdded"),
    OrderSleeping("OrderSleeping"),
    OrderPriorityChanged("OrderPriorityChanged");
    private final String value;
    private final static Map<String, EventType> CONSTANTS = new HashMap<String, EventType>();

    static {
        for (EventType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private EventType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    @JsonValue
    public String value() {
        return this.value;
    }

    @JsonCreator
    public static EventType fromValue(String value) {
        EventType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
