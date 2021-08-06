
package com.sos.sign.model.workflow;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ParameterType {

    String("String"),
    Number("Number"),
    Boolean("Boolean");
    private final java.lang.String value;
    private final static Map<java.lang.String, ParameterType> CONSTANTS = new HashMap<java.lang.String, ParameterType>();

    static {
        for (ParameterType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private ParameterType(java.lang.String value) {
        this.value = value;
    }

    @Override
    public java.lang.String toString() {
        return this.value;
    }

    @JsonValue
    public java.lang.String value() {
        return this.value;
    }

    @JsonCreator
    public static ParameterType fromValue(java.lang.String value) {
        ParameterType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
