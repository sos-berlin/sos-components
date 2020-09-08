
package com.sos.joc.db.inventory.meta;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ConfigurationType {

    FOLDER(0),
    WORKFLOW(1),
    JOBCLASS(2),
    AGENTCLUSTER(3),
    LOCK(4),
    JUNCTION(5),
    CALENDAR(6),
    ORDER(7);
    private final Integer intValue;
    private final static Map<String, ConfigurationType> CONSTANTS = new HashMap<String, ConfigurationType>();
    private final static Map<Integer, ConfigurationType> INTCONSTANTS = new HashMap<Integer, ConfigurationType>();

    static {
        for (ConfigurationType c: values()) {
            CONSTANTS.put(c.name(), c);
        }
    }

    static {
        for (ConfigurationType c: values()) {
            INTCONSTANTS.put(c.intValue, c);
        }
    }

    private ConfigurationType(Integer intValue) {
        this.intValue = intValue;
    }

    @Override
    public String toString() {
        return this.name();
    }

    @JsonValue
    public String value() {
        return this.name();
    }

    public Integer intValue() {
        return this.intValue;
    }

    @JsonCreator
    public static ConfigurationType fromValue(String value) {
        ConfigurationType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

    public static ConfigurationType fromValue(Integer intValue) {
        ConfigurationType constant = INTCONSTANTS.get(intValue);
        if (constant == null) {
            throw new IllegalArgumentException(intValue + "");
        } else {
            return constant;
        }
    }

}
