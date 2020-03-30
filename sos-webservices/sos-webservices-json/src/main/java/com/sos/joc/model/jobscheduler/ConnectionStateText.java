
package com.sos.joc.model.jobscheduler;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ConnectionStateText {

    established("established"),
    unstable("unstable"),
    unreachable("unreachable"),
    unknown("unknown");
    private final String value;
    private final static Map<String, ConnectionStateText> CONSTANTS = new HashMap<String, ConnectionStateText>();

    static {
        for (ConnectionStateText c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private ConnectionStateText(String value) {
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
    public static ConnectionStateText fromValue(String value) {
        ConnectionStateText constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
