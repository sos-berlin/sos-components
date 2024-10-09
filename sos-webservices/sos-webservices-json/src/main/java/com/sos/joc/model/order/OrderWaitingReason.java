
package com.sos.joc.model.order;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OrderWaitingReason {

    DELAYED_AFTER_ERROR("DELAYED_AFTER_ERROR"),
    DELAYING_RETRY("DELAYING_RETRY"),
    FORKED("FORKED"),
    EXPECTING_NOTICES("EXPECTING_NOTICES"),
    WAITING_FOR_LOCK("WAITING_FOR_LOCK"),
    WAITING_FOR_ADMISSION("WAITING_FOR_ADMISSION"),
    JOB_PROCESS_LIMIT_REACHED("JOB_PROCESS_LIMIT_REACHED"),
    AGENT_PROCESS_LIMIT_REACHED("AGENT_PROCESS_LIMIT_REACHED"),
    WORKFLOW_IS_SUSPENDED("WORKFLOW_IS_SUSPENDED"),
    BETWEEN_CYCLES("BETWEEN_CYCLES");
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
