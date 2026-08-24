package com.sos.jitl.jobs.mail;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MailPriority {
    VERY_HIGH(1),
    HIGH(2),
    NORMAL(3),
    LOW(4),
    VERY_LOW(5);

    private final Integer intValue;
    private final static Map<String, MailPriority> CONSTANTS = new HashMap<String, MailPriority>();
    private final static Map<Integer, MailPriority> INTCONSTANTS = new HashMap<Integer, MailPriority>();

    static {
        for (MailPriority c: values()) {
            CONSTANTS.put(c.name(), c);
        }
    }

    static {
        for (MailPriority c: values()) {
            INTCONSTANTS.put(c.intValue, c);
        }
    }

    private MailPriority(Integer intValue) {
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
    public static MailPriority fromValue(String value) {
        MailPriority constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

    public static MailPriority fromValue(Integer intValue) {
        MailPriority constant = INTCONSTANTS.get(intValue);
        if (constant == null) {
            throw new IllegalArgumentException(intValue + "");
        } else {
            return constant;
        }
    }

}
