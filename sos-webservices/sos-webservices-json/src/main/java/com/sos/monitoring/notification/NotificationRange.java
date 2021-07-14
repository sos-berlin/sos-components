package com.sos.monitoring.notification;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum NotificationRange {

    WORKFLOW(0), WORKFLOW_JOB(1);

    private final Integer intValue;
    private final static Map<String, NotificationRange> CONSTANTS = new HashMap<String, NotificationRange>();
    private final static Map<Integer, NotificationRange> INTCONSTANTS = new HashMap<Integer, NotificationRange>();

    static {
        for (NotificationRange c : values()) {
            CONSTANTS.put(c.name(), c);
        }
    }

    static {
        for (NotificationRange c : values()) {
            INTCONSTANTS.put(c.intValue, c);
        }
    }

    private NotificationRange(Integer intValue) {
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
    public static NotificationRange fromValue(String value) {
        NotificationRange constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

    public static NotificationRange fromValue(Integer intValue) {
        NotificationRange constant = INTCONSTANTS.get(intValue);
        if (constant == null) {
            throw new IllegalArgumentException(intValue + "");
        } else {
            return constant;
        }
    }
}
