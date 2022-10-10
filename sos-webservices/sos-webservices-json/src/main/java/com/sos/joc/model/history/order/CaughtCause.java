
package com.sos.joc.model.history.order;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CaughtCause {

    Unknown("Unknown"),
    TryInstruction("TryInstruction"),
    Retry("Retry");
    private final String value;
    private final static Map<String, CaughtCause> CONSTANTS = new HashMap<String, CaughtCause>();

    static {
        for (CaughtCause c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private CaughtCause(String value) {
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
    public static CaughtCause fromValue(String value) {
        CaughtCause constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
