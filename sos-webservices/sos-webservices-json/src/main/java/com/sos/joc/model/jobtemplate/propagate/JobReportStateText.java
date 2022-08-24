
package com.sos.joc.model.jobtemplate.propagate;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum JobReportStateText {

    SKIPPED("SKIPPED"),
    UPTODATE("UPTODATE"),
    CONFLICT("CONFLICT"),
    CHANGED("CHANGED"),
    TEMPLATE_REFERENCE_DELETED("TEMPLATE_REFERENCE_DELETED"),
    PERMISSION_DENIED("PERMISSION_DENIED");
    private final String value;
    private final static Map<String, JobReportStateText> CONSTANTS = new HashMap<String, JobReportStateText>();

    static {
        for (JobReportStateText c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private JobReportStateText(String value) {
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
    public static JobReportStateText fromValue(String value) {
        JobReportStateText constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
