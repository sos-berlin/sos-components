
package com.sos.joc.model.publish;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum JSConfigurationState {

    READY_TO_DEPLOY(0),
    DEPLOYED_SUCCESSFULLY(1),
    DEPLOYED_WITH_ERRORS(2),
    NOT_DEPLOYED(3),
    INCOMPLETE(4);
    private final Integer value;
    private final static Map<Integer, JSConfigurationState> CONSTANTS = new HashMap<Integer, JSConfigurationState>();

    static {
        for (JSConfigurationState c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private JSConfigurationState(Integer value) {
        this.value = value;
    }

    @JsonValue
    public Integer value() {
        return this.value;
    }

    @JsonCreator
    public static JSConfigurationState fromValue(Integer value) {
        JSConfigurationState constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException((value +""));
        } else {
            return constant;
        }
    }

}
