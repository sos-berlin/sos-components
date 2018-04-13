
package com.sos.jobscheduler.model.event;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CalendarObjectType {

    WORKINGDAYSCALENDAR("WORKINGDAYSCALENDAR"),
    NONWORKINGDAYSCALENDAR("NONWORKINGDAYSCALENDAR"),
    JOB("JOB"),
    ORDER("ORDER"),
    SCHEDULE("SCHEDULE");
    private final String value;
    private final static Map<String, CalendarObjectType> CONSTANTS = new HashMap<String, CalendarObjectType>();

    static {
        for (CalendarObjectType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private CalendarObjectType(String value) {
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
    public static CalendarObjectType fromValue(String value) {
        CalendarObjectType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
