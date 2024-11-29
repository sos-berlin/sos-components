
package com.sos.joc.model.monitoring.enums;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TotalRunningTimeSource {

    estimated("estimated"),
    dateFrom("dateFrom"),
    dateTo("dateTo"),
    nextReadyTime("nextReadyTime"),
    now("now"),
    shutdown("shutdown");
    private final String value;
    private final static Map<String, TotalRunningTimeSource> CONSTANTS = new HashMap<String, TotalRunningTimeSource>();

    static {
        for (TotalRunningTimeSource c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private TotalRunningTimeSource(String value) {
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
    public static TotalRunningTimeSource fromValue(String value) {
        TotalRunningTimeSource constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
