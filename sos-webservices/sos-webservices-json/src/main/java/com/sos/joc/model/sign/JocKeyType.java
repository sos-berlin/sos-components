
package com.sos.joc.model.sign;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum JocKeyType {

    PRIVATE(0),
    PUBLIC(1),
    X509(2);
    private final Integer value;
    private final static Map<Integer, JocKeyType> CONSTANTS = new HashMap<Integer, JocKeyType>();

    static {
        for (JocKeyType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private JocKeyType(Integer value) {
        this.value = value;
    }

    @JsonValue
    public Integer value() {
        return this.value;
    }

    @JsonCreator
    public static JocKeyType fromValue(Integer value) {
        JocKeyType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException((value +""));
        } else {
            return constant;
        }
    }

}
