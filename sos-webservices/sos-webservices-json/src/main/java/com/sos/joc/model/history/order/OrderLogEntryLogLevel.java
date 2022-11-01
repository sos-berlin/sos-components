
package com.sos.joc.model.history.order;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OrderLogEntryLogLevel {

    MAIN("MAIN"),
    DETAIL("DETAIL"),
    INFO("INFO"),
    ERROR("ERROR"),
    SUCCESS("SUCCESS");
    private final String value;
    private final static Map<String, OrderLogEntryLogLevel> CONSTANTS = new HashMap<String, OrderLogEntryLogLevel>();

    static {
        for (OrderLogEntryLogLevel c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private OrderLogEntryLogLevel(String value) {
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
    public static OrderLogEntryLogLevel fromValue(String value) {
        OrderLogEntryLogLevel constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
