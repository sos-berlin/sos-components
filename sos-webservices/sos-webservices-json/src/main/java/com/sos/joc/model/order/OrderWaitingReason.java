
package com.sos.joc.model.order;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OrderWaitingReason {

    DELAYED_AFTER_ERROR("DelayedAfterError"),
    FORKED("Forked"),
    EXPECTING_NOTICE("ExpectingNotice"),
    WAITING_FOR_LOCK("WaitingForLock");
    private final String value;
    private final static Map<String, OrderWaitingReason> CONSTANTS = new HashMap<String, OrderWaitingReason>();

    static {
        for (OrderWaitingReason c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private OrderWaitingReason(String value) {
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
    public static OrderWaitingReason fromValue(String value) {
        OrderWaitingReason constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
