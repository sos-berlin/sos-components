
package com.sos.joc.model.job;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum JobCriticalities {

    NORMAL("NORMAL"),
    MINOR("MINOR"),
    MAJOR("MAJOR");
    private final String value;
    private final static Map<String, JobCriticalities> CONSTANTS = new HashMap<String, JobCriticalities>();

    static {
        for (JobCriticalities c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private JobCriticalities(String value) {
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
    public static JobCriticalities fromValue(String value) {
        JobCriticalities constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
