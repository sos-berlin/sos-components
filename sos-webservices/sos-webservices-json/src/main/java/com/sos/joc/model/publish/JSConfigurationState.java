
package com.sos.joc.model.publish;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum JSConfigurationState {

    READY_TO_DEPLOY("READY_TO_DEPLOY"),
    DEPLOYED_SUCCESSFULLY("DEPLOYED_SUCCESSFULLY"),
    DEPLOYED_WITH_ERRORS("DEPLOYED_WITH_ERRORS"),
    INCOMPLETE("INCOMPLETE");
    private final String value;
    private final static Map<String, JSConfigurationState> CONSTANTS = new HashMap<String, JSConfigurationState>();

    static {
        for (JSConfigurationState c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private JSConfigurationState(String value) {
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
    public static JSConfigurationState fromValue(String value) {
        JSConfigurationState constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
