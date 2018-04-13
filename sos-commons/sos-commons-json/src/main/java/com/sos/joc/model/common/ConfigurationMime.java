
package com.sos.joc.model.common;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ConfigurationMime {

    HTML("HTML"),
    XML("XML");
    private final String value;
    private final static Map<String, ConfigurationMime> CONSTANTS = new HashMap<String, ConfigurationMime>();

    static {
        for (ConfigurationMime c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private ConfigurationMime(String value) {
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
    public static ConfigurationMime fromValue(String value) {
        ConfigurationMime constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
