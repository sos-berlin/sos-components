
package com.sos.inventory.model.instruction;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum WhenNotAnnouced {

    WAIT("Wait"),
    DONT_WAIT("DontWait"),
    SKIP_WHEN_NO_NOTICE("SkipWhenNoNotice");
    private final String value;
    private final static Map<String, WhenNotAnnouced> CONSTANTS = new HashMap<String, WhenNotAnnouced>();

    static {
        for (WhenNotAnnouced c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private WhenNotAnnouced(String value) {
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
    public static WhenNotAnnouced fromValue(String value) {
        WhenNotAnnouced constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
