package com.sos.monitoring.notification;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum NotificationStatus {

    OK(0), WARNING(1), CRITICAL(2), UNKNOWN(3);

    private final Integer intValue;
    private final static Map<String, NotificationStatus> CONSTANTS = new HashMap<String, NotificationStatus>();
    private final static Map<Integer, NotificationStatus> INTCONSTANTS = new HashMap<Integer, NotificationStatus>();

    static {
        for (NotificationStatus c : values()) {
            CONSTANTS.put(c.name(), c);
        }
    }

    static {
        for (NotificationStatus c : values()) {
            INTCONSTANTS.put(c.intValue, c);
        }
    }

    private NotificationStatus(Integer intValue) {
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
    public static NotificationStatus fromValue(String value) {
        NotificationStatus constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

    public static NotificationStatus fromValue(Integer intValue) {
        NotificationStatus constant = INTCONSTANTS.get(intValue);
        if (constant == null) {
            throw new IllegalArgumentException(intValue + "");
        } else {
            return constant;
        }
    }
}
