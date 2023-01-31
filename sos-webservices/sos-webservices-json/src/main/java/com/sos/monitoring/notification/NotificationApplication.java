package com.sos.monitoring.notification;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum NotificationApplication {

    ORDER_NOTIFICATION(0), SYSTEM_NOTIFICATION(1);

    private final Integer intValue;
    private final static Map<String, NotificationApplication> CONSTANTS = new HashMap<String, NotificationApplication>();
    private final static Map<Integer, NotificationApplication> INTCONSTANTS = new HashMap<Integer, NotificationApplication>();

    static {
        for (NotificationApplication c : values()) {
            CONSTANTS.put(c.name(), c);
        }
    }

    static {
        for (NotificationApplication c : values()) {
            INTCONSTANTS.put(c.intValue, c);
        }
    }

    private NotificationApplication(Integer intValue) {
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
    public static NotificationApplication fromValue(String value) {
        NotificationApplication constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

    public static NotificationApplication fromValue(Integer intValue) {
        NotificationApplication constant = INTCONSTANTS.get(intValue);
        if (constant == null) {
            throw new IllegalArgumentException(intValue + "");
        } else {
            return constant;
        }
    }
}
