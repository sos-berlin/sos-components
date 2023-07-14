
package com.sos.inventory.model.job;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum InternalExecutableType {

    JITL("JITL"),
    Java("Java"),
    JavaScript_Graal("JavaScript_Graal"),
    JavaScript_Node("JavaScript_Node");
    private final String value;
    private final static Map<String, InternalExecutableType> CONSTANTS = new HashMap<String, InternalExecutableType>();

    static {
        for (InternalExecutableType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private InternalExecutableType(String value) {
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
    public static InternalExecutableType fromValue(String value) {
        InternalExecutableType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
