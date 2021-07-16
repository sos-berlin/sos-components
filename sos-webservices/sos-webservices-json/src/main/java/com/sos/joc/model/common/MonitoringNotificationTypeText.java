
package com.sos.joc.model.common;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MonitoringNotificationTypeText {

    SUCCESS("SUCCESS"),
    ERROR("ERROR"),
    WARNING("WARNING"),
    RECOVERED("RECOVERED");
    private final String value;
    private final static Map<String, MonitoringNotificationTypeText> CONSTANTS = new HashMap<String, MonitoringNotificationTypeText>();

    static {
        for (MonitoringNotificationTypeText c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private MonitoringNotificationTypeText(String value) {
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
    public static MonitoringNotificationTypeText fromValue(String value) {
        MonitoringNotificationTypeText constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
