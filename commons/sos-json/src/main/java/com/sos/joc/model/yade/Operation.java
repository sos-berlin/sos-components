
package com.sos.joc.model.yade;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Operation {

    COPY("COPY"),
    MOVE("MOVE"),
    GETLIST("GETLIST"),
    RENAME("RENAME"),
    COPYTOINTERNET("COPYTOINTERNET"),
    COPYFROMINTERNET("COPYFROMINTERNET");
    private final String value;
    private final static Map<String, Operation> CONSTANTS = new HashMap<String, Operation>();

    static {
        for (Operation c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private Operation(String value) {
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
    public static Operation fromValue(String value) {
        Operation constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
