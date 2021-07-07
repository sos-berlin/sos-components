
package com.sos.joc.model.order;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PositionChangeCode {

    NOT_ONE_WORKFLOW("NOT_ONE_WORKFLOW"),
    NO_COMMON_POSITIONS("NO_COMMON_POSITIONS");
    private final String value;
    private final static Map<String, PositionChangeCode> CONSTANTS = new HashMap<String, PositionChangeCode>();

    static {
        for (PositionChangeCode c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private PositionChangeCode(String value) {
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
    public static PositionChangeCode fromValue(String value) {
        PositionChangeCode constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
