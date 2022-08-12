
package com.sos.joc.model.inventory.search;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RequestSearchReturnType {

    WORKFLOW("WORKFLOW"),
    FILEORDERSOURCE("FILEORDERSOURCE"),
    JOBRESOURCE("JOBRESOURCE"),
    JOBTEMPLATE("JOBTEMPLATE"),
    NOTICEBOARD("NOTICEBOARD"),
    LOCK("LOCK"),
    SCHEDULE("SCHEDULE"),
    INCLUDESCRIPT("INCLUDESCRIPT"),
    CALENDAR("CALENDAR");
    private final String value;
    private final static Map<String, RequestSearchReturnType> CONSTANTS = new HashMap<String, RequestSearchReturnType>();

    static {
        for (RequestSearchReturnType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private RequestSearchReturnType(String value) {
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
    public static RequestSearchReturnType fromValue(String value) {
        RequestSearchReturnType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
