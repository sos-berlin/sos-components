package com.sos.history;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum JobWarning {
    NONE(0), LONGER_THAN(1), SHORTER_THAN(2);

    private final Integer intValue;
    private final static Map<String, JobWarning> CONSTANTS = new HashMap<String, JobWarning>();
    private final static Map<Integer, JobWarning> INTCONSTANTS = new HashMap<Integer, JobWarning>();

    static {
        for (JobWarning c : values()) {
            CONSTANTS.put(c.name(), c);
        }
    }

    static {
        for (JobWarning c : values()) {
            INTCONSTANTS.put(c.intValue, c);
        }
    }

    private JobWarning(Integer intValue) {
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
    public static JobWarning fromValue(String value) {
        JobWarning constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

    public static JobWarning fromValue(Integer intValue) {
        JobWarning constant = INTCONSTANTS.get(intValue);
        if (constant == null) {
            throw new IllegalArgumentException(intValue + "");
        } else {
            return constant;
        }
    }

}
