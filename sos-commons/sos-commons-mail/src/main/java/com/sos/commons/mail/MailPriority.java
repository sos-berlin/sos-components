package com.sos.commons.mail;

import java.util.HashMap;
import java.util.Map;

public enum MailPriority {
    HIGHEST(1),
    HIGH(2),
    LOW(4),
    LOWEST(5);

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

    public String value() {
        return this.name();
    }

    public Integer intValue() {
        return this.intValue;
    }

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
