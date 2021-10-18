
package com.sos.inventory.model.job;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AdmissionTimePeriodType {

    WEEKDAY_PERIOD("WeekdayPeriod"),
    DAILY_PERIOD("DailyPeriod");
    private final String value;
    private final static Map<String, AdmissionTimePeriodType> CONSTANTS = new HashMap<String, AdmissionTimePeriodType>();

    static {
        for (AdmissionTimePeriodType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private AdmissionTimePeriodType(String value) {
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
    public static AdmissionTimePeriodType fromValue(String value) {
        AdmissionTimePeriodType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
