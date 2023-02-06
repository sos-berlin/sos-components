package com.sos.monitoring.notification;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OrderNotificationRange {

    WORKFLOW(0), WORKFLOW_JOB(1);

    private final Integer intValue;
    private final static Map<String, OrderNotificationRange> CONSTANTS = new HashMap<String, OrderNotificationRange>();
    private final static Map<Integer, OrderNotificationRange> INTCONSTANTS = new HashMap<Integer, OrderNotificationRange>();

    static {
        for (OrderNotificationRange c : values()) {
            CONSTANTS.put(c.name(), c);
        }
    }

    static {
        for (OrderNotificationRange c : values()) {
            INTCONSTANTS.put(c.intValue, c);
        }
    }

    private OrderNotificationRange(Integer intValue) {
        this.intValue = intValue;
    }

    @Override
    public String toString() {
        return this.name();
    }

    @JsonValue
    public String value() {
        return this.name();
    }

    public Integer intValue() {
        return this.intValue;
    }

    @JsonCreator
    public static OrderNotificationRange fromValue(String value) {
        OrderNotificationRange constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

    public static OrderNotificationRange fromValue(Integer intValue) {
        OrderNotificationRange constant = INTCONSTANTS.get(intValue);
        if (constant == null) {
            throw new IllegalArgumentException(intValue + "");
        } else {
            return constant;
        }
    }
}
