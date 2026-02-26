
package com.sos.joc.model.joc;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum LogLevel {

    FATAL("FATAL"),
    ERROR("ERROR"),
    WARN("WARN"),
    INFO("INFO"),
    DEBUG("DEBUG"),
    TRACE("TRACE");
    private final String value;
    private final static Map<String, LogLevel> CONSTANTS = new HashMap<String, LogLevel>();

    static {
        for (LogLevel c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private LogLevel(String value) {
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
    public static LogLevel fromValue(String value) {
        LogLevel constant = CONSTANTS.get(value);
        if (constant == null) {
            return INFO;
        } else {
            return constant;
        }
    }

}
