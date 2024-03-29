
package com.sos.js7.converter.js1.common.json.calendars;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RepetitionText {

    DAILY("DAILY"), WEEKLY("WEEKLY"), MONTHLY("MONTHLY"), YEARLY("YEARLY");

    private final String value;
    private final static Map<String, RepetitionText> CONSTANTS = new HashMap<String, RepetitionText>();

    static {
        for (RepetitionText c : values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private RepetitionText(String value) {
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
    public static RepetitionText fromValue(String value) {
        RepetitionText constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
