package com.sos.monitoring;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MonitorType {

    COMMAND(0), MAIL(1), NSCA(2), JMS(3);

    private final Integer intValue;
    private final static Map<String, MonitorType> CONSTANTS = new HashMap<String, MonitorType>();
    private final static Map<Integer, MonitorType> INTCONSTANTS = new HashMap<Integer, MonitorType>();

    static {
        for (MonitorType c : values()) {
            CONSTANTS.put(c.name(), c);
        }
    }

    static {
        for (MonitorType c : values()) {
            INTCONSTANTS.put(c.intValue, c);
        }
    }

    private MonitorType(Integer intValue) {
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
    public static MonitorType fromValue(String value) {
        MonitorType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

    public static MonitorType fromValue(Integer intValue) {
        MonitorType constant = INTCONSTANTS.get(intValue);
        if (constant == null) {
            throw new IllegalArgumentException(intValue + "");
        } else {
            return constant;
        }
    }
}
