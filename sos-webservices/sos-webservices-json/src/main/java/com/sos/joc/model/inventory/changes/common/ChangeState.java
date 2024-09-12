
package com.sos.joc.model.inventory.changes.common;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ChangeState {

    OPEN(0),
    CLOSED(1),
    PUBLISHED(2);
    private final Integer value;
    private final static Map<Integer, ChangeState> CONSTANTS = new HashMap<Integer, ChangeState>();

    static {
        for (ChangeState c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private ChangeState(Integer value) {
        this.value = value;
    }

    @JsonValue
    public Integer value() {
        return this.value;
    }
    
    @JsonCreator
    public static ChangeState fromValue(Integer value) {
        ChangeState constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException((value +""));
        } else {
            return constant;
        }
    }

}
