
package com.sos.inventory.model.job.notification;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum JobNotificationType {

    ERROR("ERROR"),
    SUCCESS("SUCCESS"),
    WARNING("WARNING");
    private final String value;
    private final static Map<String, JobNotificationType> CONSTANTS = new HashMap<String, JobNotificationType>();

    static {
        for (JobNotificationType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private JobNotificationType(String value) {
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
    public static JobNotificationType fromValue(String value) {
        JobNotificationType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
