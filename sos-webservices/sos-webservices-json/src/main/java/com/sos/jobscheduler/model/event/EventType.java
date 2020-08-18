
package com.sos.jobscheduler.model.event;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EventType {

    VERSION_ADDED("VersionAdded"),
    FILE_BASED_CHANGED("FileBasedChanged"),
    CONTROLLER_READY("ControllerReady"),
    AGENT_READY("AgentReady"),
    ORDER_ADDED("OrderAdded"),
    ORDER_ATTACHABLE("OrderAttachable"),
    ORDER_TRANSFERRED_TO_AGENT("OrderTransferredToAgent"),
    ORDER_STARTED("OrderStarted"),
    ORDER_TRANSFERRED_TO_AGENT_("OrderTransferredToAgent"),
    ORDER_PROCESSING_STARTED("OrderProcessingStarted"),
    ORDER_STDOUT_WRITTEN("OrderStdoutWritten"),
    ORDER_STDERR_WRITTEN("OrderStderrWritten"),
    ORDER_PROCESSED("OrderProcessed"),
    ORDER_RESUMED("OrderResumed"),
    ORDER_FORKED("OrderForked"),
    ORDER_JOINED("OrderJoined"),
    ORDER_OFFERED("OrderOffered"),
    ORDER_RETRYING("OrderRetrying"),
    ORDER_AWAITING("OrderAwaiting"),
    ORDER_MOVED("OrderMoved"),
    ORDER_DETACHABLE("OrderDetachable"),
    ORDER_DETACHED("OrderDetached"),
    ORDER_FAILED("OrderFailed"),
    ORDER_CATCHED("OrderCatched"),
    ORDER_AWOKE("OrderAwoke"),
    ORDER_FAILEDIN_FORK("OrderFailedinFork"),
    ORDER_SUSPENDED("OrderSuspended"),
    ORDER_BROKEN("OrderBroken"),
    ORDER_CANCELLED("OrderCancelled"),
    ORDER_FINISHED("OrderFinished");
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
