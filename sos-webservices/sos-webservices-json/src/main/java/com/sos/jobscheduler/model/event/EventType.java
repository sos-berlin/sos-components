
package com.sos.jobscheduler.model.event;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EventType {

    ORDER_ADDED("OrderAdded"),
    ORDER_ATTACHED("OrderAttached"),
    ORDER_TRANSFERRED_TO_AGENT("OrderTransferredToAgent"),
    ORDER_PROCESSING_STARTED("OrderProcessingStarted"),
    ORDER_STDOUT_WRITTEN("OrderStdoutWritten"),
    ORDER_STDERR_WRITTEN("OrderStderrWritten"),
    ORDER_PROCESSED("OrderProcessed"),
    ORDER_FORKED("OrderForked"),
    ORDER_JOINED("OrderJoined"),
    ORDER_OFFERED("OrderOffered"),
    ORDER_AWAITING("OrderAwaiting"),
    ORDER_MOVED("OrderMoved"),
    ORDER_DETACHABLE("OrderDetachable"),
    ORDER_DETACHED("OrderDetached"),
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
