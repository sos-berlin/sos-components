
package com.sos.joc.model.common;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum LogMime {

    PLAIN("PLAIN"),
    HTML("HTML");
    private final String value;
    private final static Map<String, LogMime> CONSTANTS = new HashMap<String, LogMime>();

    static {
        for (LogMime c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private LogMime(String value) {
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
    public static LogMime fromValue(String value) {
        LogMime constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
