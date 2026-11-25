
package com.sos.controller.model.event;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EventType {

    VersionAdded("VersionAdded", false),
    FileBasedChanged("FileBasedChanged", false),
    ControllerReady("ControllerReady", false),
    AgentReady("AgentReady", false),
    OrderAdded("OrderAdded", false),
    OrderAttachable("OrderAttachable", false),
    OrderAttached("OrderAttached", false),
    OrderStarted("OrderStarted", false),
    OrderTransferredToAgent("OrderTransferredToAgent", false),
    OrderProcessingStarted("OrderProcessingStarted", false),
    OrderStdoutWritten("OrderStdoutWritten", false),
    OrderStderrWritten("OrderStderrWritten", false),
    OrderProcessed("OrderProcessed", false),
    OrderResumed("OrderResumed", false),
    OrderResumeMarked("OrderResumeMarked", false),
    OrderResumptionMarked("OrderResumptionMarked", false),
    OrderForked("OrderForked", false),
    OrderJoined("OrderJoined", false),
    OrderOffered("OrderOffered", false),
    OrderRetrying("OrderRetrying", false),
    OrderAwaiting("OrderAwaiting", false),
    OrderMoved("OrderMoved", false),
    OrderDetachable("OrderDetachable", false),
    OrderDetached("OrderDetached", false),
    OrderOutcomeAdded("OrderOutcomeAdded", false),
    OrderFailedinFork("OrderFailedinFork", true),
    OrderFailed("OrderFailed", true),
    OrderStopped("OrderStopped", true),
    OrderCatched("OrderCatched", false),
    OrderCaught("OrderCaught", false),
    OrderAwoke("OrderAwoke", false),
    OrderSuspended("OrderSuspended", true),
    OrderSuspendMarked("OrderSuspendMarked", true),
    OrderSuspensionMarked("OrderSuspensionMarked", true),
    OrderBroken("OrderBroken", true),
    OrderCancelled("OrderCancelled", true),
    OrderFinished("OrderFinished", false),
    OrderLocksAcquired("OrderLocksAcquired", false),
    OrderLocksQueued("OrderLocksQueued", false), //maybe true
    OrderLocksReleased("OrderLocksReleased", false),
    OrderNoticesConsumed("OrderNoticesConsumed", false),
    OrderNoticesConsumptionStarted("OrderNoticesConsumptionStarted", false),
    OrderNoticesRead("OrderNoticesRead", false),
    OrderNoticesExpected("OrderNoticesExpected", false),
    OrderNoticePosted("OrderNoticePosted", false),
    OrderPrompted("OrderPrompted", false), //maybe true
    OrderPromptAnswered("OrderPromptAnswered", false),
    OrderCyclingPrepared("OrderCyclingPrepared", false),
    OrderOrderAdded("OrderOrderAdded", false),
    OrderSleeping("OrderSleeping", false),
    OrderPriorityChanged("OrderPriorityChanged", false);
    private final String value;
    private final boolean disturbed;
    private final static Map<String, EventType> CONSTANTS = new HashMap<String, EventType>();

    static {
        for (EventType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private EventType(String value, boolean disturbed) {
        this.value = value;
        this.disturbed = disturbed;
    }

    @Override
    public String toString() {
        return this.value;
    }

    @JsonValue
    public String value() {
        return this.value;
    }
    
    public Boolean isDisturbed() {
        return this.disturbed;
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
