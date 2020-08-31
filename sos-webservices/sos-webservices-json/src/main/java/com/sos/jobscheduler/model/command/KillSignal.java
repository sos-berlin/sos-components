
package com.sos.jobscheduler.model.command;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum KillSignal {

    SIGTERM("SIGTERM"),
    SIGKILL("SIGKILL");
    private final String value;
    private final static Map<String, KillSignal> CONSTANTS = new HashMap<String, KillSignal>();

    static {
        for (KillSignal c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private KillSignal(String value) {
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
    public static KillSignal fromValue(String value) {
        KillSignal constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
