
package com.sos.joc.model.jobscheduler;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum JobSchedulerStateText {

    RUNNING("RUNNING"),
    WAITING_FOR_ACTIVATION("WAITING_FOR_ACTIVATION"),
    TERMINATING("TERMINATING"),
    UNKNOWN_AGENT("UNKNOWN_AGENT"),
    UNREACHABLE("UNREACHABLE");
    private final String value;
    private final static Map<String, JobSchedulerStateText> CONSTANTS = new HashMap<String, JobSchedulerStateText>();

    static {
        for (JobSchedulerStateText c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private JobSchedulerStateText(String value) {
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
    public static JobSchedulerStateText fromValue(String value) {
        JobSchedulerStateText constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
