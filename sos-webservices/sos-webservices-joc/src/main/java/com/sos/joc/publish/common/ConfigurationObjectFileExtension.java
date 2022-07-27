package com.sos.joc.publish.common;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ConfigurationObjectFileExtension {

    SCHEDULE_FILE_EXTENSION(".schedule.json"),
    CALENDAR_FILE_EXTENSION(".calendar.json"),
    SCRIPT_FILE_EXTENSION(".script.json"),
    JOB_FILE_EXTENSION(".job.json");


    private final String value;
    private static final Map<String, ConfigurationObjectFileExtension> CONSTANTS = new HashMap<String, ConfigurationObjectFileExtension>();

    static {
        for (ConfigurationObjectFileExtension c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private ConfigurationObjectFileExtension(String value) {
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
    public static ConfigurationObjectFileExtension fromValue(String value) {
        ConfigurationObjectFileExtension constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
