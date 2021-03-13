
package com.sos.joc.model.common;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum VariableType {

    STRING(0),
    BOOLEAN(1),
    INTEGER(2),
    BIGDECIMAL(3),
    DOUBLE(4);
    private final Integer value;
    private final static Map<Integer, VariableType> CONSTANTS = new HashMap<Integer, VariableType>();

    static {
        for (VariableType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private VariableType(Integer value) {
        this.value = value;
    }

    @JsonValue
    public Integer value() {
        return this.value;
    }

    @JsonCreator
    public static VariableType fromValue(Integer value) {
        VariableType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException((value +""));
        } else {
            return constant;
        }
    }

}
