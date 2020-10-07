
package com.sos.joc.model.calendar;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum WhenHolidayType {

    SUPPRESS("SUPPRESS"),
    IGNORE("IGNORE"),
    PREVIOUSNONWORKINGDAY("PREVIOUSNONWORKINGDAY"),
    NEXTNONWORKINGDAY("NEXTNONWORKINGDAY");
    private final String value;
    private final static Map<String, WhenHolidayType> CONSTANTS = new HashMap<String, WhenHolidayType>();

    static {
        for (WhenHolidayType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private WhenHolidayType(String value) {
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
    public static WhenHolidayType fromValue(String value) {
        WhenHolidayType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
