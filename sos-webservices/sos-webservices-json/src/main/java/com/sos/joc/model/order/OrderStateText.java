
package com.sos.joc.model.order;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OrderStateText {

    PENDING("PENDING"),
    RUNNING("RUNNING"),
    SUSPENDED("SUSPENDED"),
    FAILED("FAILED"),
    WAITING("WAITING"),
    BLOCKED("BLOCKED"),
    FINISHED("FINISHED"),
    UNKNOWN("UNKNOWN");
    private final String value;
    private final static Map<String, OrderStateText> CONSTANTS = new HashMap<String, OrderStateText>();

    static {
        for (OrderStateText c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private OrderStateText(String value) {
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
    public static OrderStateText fromValue(String value) {
        OrderStateText constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
