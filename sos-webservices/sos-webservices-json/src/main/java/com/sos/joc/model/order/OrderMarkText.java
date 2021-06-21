
package com.sos.joc.model.order;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OrderMarkText {

    CANCELLING("CANCELLING"),
    RESUMING("RESUMING"),
    SUSPENDING("SUSPENDING");
    private final String value;
    private final static Map<String, OrderMarkText> CONSTANTS = new HashMap<String, OrderMarkText>();

    static {
        for (OrderMarkText c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private OrderMarkText(String value) {
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
    public static OrderMarkText fromValue(String value) {
        OrderMarkText constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
