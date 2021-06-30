
package com.sos.joc.model.order;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OrderStateText2 {

    Fresh("Fresh"),
    Awaiting("Awaiting"),
    DelayedAfterError("DelayedAfterError"),
    Forked("Forked"),
    Offering("Offering"),
    Broken("Broken"),
    Failed("Failed"),
    FailedInFork("FailedInFork"),
    FailedWhileFresh("FailedWhileFresh"),
    Ready("Ready"),
    Prompting("Prompting"),
    Processed("Processed"),
    Processing("Processing"),
    Finished("Finished"),
    Cancelled("Cancelled"),
    ProcessingKilled("ProcessingKilled"),
    Suspended("Suspended"),
    WaitingForLock("WaitingForLock");
    private final String value;
    private final static Map<String, OrderStateText2> CONSTANTS = new HashMap<String, OrderStateText2>();

    static {
        for (OrderStateText2 c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private OrderStateText2(String value) {
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
    public static OrderStateText2 fromValue(String value) {
        OrderStateText2 constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
