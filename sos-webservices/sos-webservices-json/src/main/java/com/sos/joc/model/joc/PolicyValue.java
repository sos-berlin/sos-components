
package com.sos.joc.model.joc;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PolicyValue {

    enabled("enabled"),
    disabled("disabled");
    private final String value;
    private final static Map<String, PolicyValue> CONSTANTS = new HashMap<String, PolicyValue>();

    static {
        for (PolicyValue c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private PolicyValue(String value) {
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
    public static PolicyValue fromValue(String value) {
        PolicyValue constant = CONSTANTS.get(value);
        if (constant == null) {
            return enabled;
        } else {
            return constant;
        }
    }

}
