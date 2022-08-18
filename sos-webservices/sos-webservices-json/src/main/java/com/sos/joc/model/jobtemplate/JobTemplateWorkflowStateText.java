
package com.sos.joc.model.jobtemplate;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum JobTemplateWorkflowStateText {

    IN_SYNC("IN_SYNC"),
    NOT_IN_SYNC("NOT_IN_SYNC");
    private final String value;
    private final static Map<String, JobTemplateWorkflowStateText> CONSTANTS = new HashMap<String, JobTemplateWorkflowStateText>();

    static {
        for (JobTemplateWorkflowStateText c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private JobTemplateWorkflowStateText(String value) {
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
    public static JobTemplateWorkflowStateText fromValue(String value) {
        JobTemplateWorkflowStateText constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
