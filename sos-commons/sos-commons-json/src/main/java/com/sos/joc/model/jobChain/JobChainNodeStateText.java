
package com.sos.joc.model.jobChain;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum JobChainNodeStateText {

    ACTIVE("ACTIVE"),
    SKIPPED("SKIPPED"),
    STOPPED("STOPPED");
    private final String value;
    private final static Map<String, JobChainNodeStateText> CONSTANTS = new HashMap<String, JobChainNodeStateText>();

    static {
        for (JobChainNodeStateText c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private JobChainNodeStateText(String value) {
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
    public static JobChainNodeStateText fromValue(String value) {
        JobChainNodeStateText constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
