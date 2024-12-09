
package com.sos.inventory.model.board;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BoardType {

    GLOBAL("GLOBAL"),
    PLANNABLE("PLANNABLE");
    private final String value;
    private final static Map<String, BoardType> CONSTANTS = new HashMap<String, BoardType>();

    static {
        for (BoardType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private BoardType(String value) {
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
    public static BoardType fromValue(String value) {
        BoardType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
