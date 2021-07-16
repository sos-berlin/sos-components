
package com.sos.joc.model.common;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MonitoringMonitorTypeText {

    COMMAND("COMMAND"),
    MAIL("MAIL"),
    NSCA("NSCA"),
    JMS("JMS");
    private final String value;
    private final static Map<String, MonitoringMonitorTypeText> CONSTANTS = new HashMap<String, MonitoringMonitorTypeText>();

    static {
        for (MonitoringMonitorTypeText c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private MonitoringMonitorTypeText(String value) {
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
    public static MonitoringMonitorTypeText fromValue(String value) {
        MonitoringMonitorTypeText constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
