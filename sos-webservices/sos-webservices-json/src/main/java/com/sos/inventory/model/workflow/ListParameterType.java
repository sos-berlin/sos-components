
package com.sos.inventory.model.workflow;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ListParameterType {

    String("String"),
    Number("Number"),
    Boolean("Boolean");
    private final java.lang.String value;
    private final static Map<java.lang.String, ListParameterType> CONSTANTS = new HashMap<java.lang.String, ListParameterType>();

    static {
        for (ListParameterType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private ListParameterType(java.lang.String value) {
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
    public static ListParameterType fromValue(java.lang.String value) {
        ListParameterType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
