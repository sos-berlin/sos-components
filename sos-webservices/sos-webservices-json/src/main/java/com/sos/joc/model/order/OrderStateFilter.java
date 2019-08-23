
package com.sos.joc.model.order;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OrderStateFilter {

    PENDING("PENDING"),
    RUNNING("RUNNING"),
    SUSPENDED("SUSPENDED"),
    SETBACK("SETBACK"),
    BLACKLIST("BLACKLIST"),
    WAITINGFORRESOURCE("WAITINGFORRESOURCE");
    private final String value;
    private final static Map<String, OrderStateFilter> CONSTANTS = new HashMap<String, OrderStateFilter>();

    static {
        for (OrderStateFilter c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private OrderStateFilter(String value) {
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
    public static OrderStateFilter fromValue(String value) {
        OrderStateFilter constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
