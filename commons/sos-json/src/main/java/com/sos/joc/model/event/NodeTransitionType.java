
package com.sos.joc.model.event;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum NodeTransitionType {

    SUCCESS("SUCCESS"),
    KEEP("KEEP"),
    PROCEEDING("PROCEEDING"),
    ERROR("ERROR");
    private final String value;
    private final static Map<String, NodeTransitionType> CONSTANTS = new HashMap<String, NodeTransitionType>();

    static {
        for (NodeTransitionType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private NodeTransitionType(String value) {
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
    public static NodeTransitionType fromValue(String value) {
        NodeTransitionType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
