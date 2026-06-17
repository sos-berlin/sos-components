
package com.sos.joc.model.publish;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OperationType {

    UPDATE(0),
    DELETE(1);
    private final Integer value;
    private final static Map<Integer, OperationType> CONSTANTS = new HashMap<Integer, OperationType>();

    static {
        for (OperationType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private OperationType(Integer value) {
        this.value = value;
    }

    @JsonValue
    public String strValue() {
        return this.name();
    }
    
    public Integer value() {
        return this.value;
    }

    @JsonCreator
    public static OperationType fromValue(String value) {
        Optional<OperationType> opt = Arrays.asList(OperationType.values()).stream().filter(c -> c.name().equals(value)).findAny();
        if (opt.isEmpty()) {
            throw new IllegalArgumentException(value);
        } else {
            return opt.get();
        }
    }
    
    public static OperationType fromValue(Integer value) {
        OperationType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException((value +""));
        } else {
            return constant;
        }
    }

}
