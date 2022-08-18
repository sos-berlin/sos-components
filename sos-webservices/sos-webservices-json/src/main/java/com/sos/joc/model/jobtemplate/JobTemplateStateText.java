
package com.sos.joc.model.jobtemplate;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum JobTemplateStateText {

    IN_SYNC("IN_SYNC"),
    NOT_IN_SYNC("NOT_IN_SYNC"),
    DELETED("DELETED"),
    UNKNOWN("UNKNOWN");
    private final String value;
    private final static Map<String, JobTemplateStateText> CONSTANTS = new HashMap<String, JobTemplateStateText>();

    static {
        for (JobTemplateStateText c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private JobTemplateStateText(String value) {
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
    public static JobTemplateStateText fromValue(String value) {
        JobTemplateStateText constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
