
package com.sos.joc.model.jobChain;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum JobChainStateText {

    NOT_INITIALIZED("NOT_INITIALIZED"),
    INITIALIZED("INITIALIZED"),
    ACTIVE("ACTIVE"),
    UNDER_CONSTRUCTION("UNDER_CONSTRUCTION"),
    STOPPED("STOPPED");
    private final String value;
    private final static Map<String, JobChainStateText> CONSTANTS = new HashMap<String, JobChainStateText>();

    static {
        for (JobChainStateText c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private JobChainStateText(String value) {
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
    public static JobChainStateText fromValue(String value) {
        JobChainStateText constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
