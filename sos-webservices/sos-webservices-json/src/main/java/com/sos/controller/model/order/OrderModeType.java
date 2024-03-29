
package com.sos.controller.model.order;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OrderModeType {

    FRESH_ONLY("FreshOnly"),
    FRESH_OR_STARTED("FreshOrStarted");
    private final String value;
    private final static Map<String, OrderModeType> CONSTANTS = new HashMap<String, OrderModeType>();

    static {
        for (OrderModeType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private OrderModeType(String value) {
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
    public static OrderModeType fromValue(String value) {
        OrderModeType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
