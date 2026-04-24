
package com.sos.joc.model.log;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RequestLevel {

    ERROR("ERROR"),
    INFO("INFO"),
    DEBUG("DEBUG");
    private final String value;
    private final static Map<String, RequestLevel> CONSTANTS = new HashMap<String, RequestLevel>();

    static {
        for (RequestLevel c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private RequestLevel(String value) {
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
    public static RequestLevel fromValue(String value) {
        RequestLevel constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
