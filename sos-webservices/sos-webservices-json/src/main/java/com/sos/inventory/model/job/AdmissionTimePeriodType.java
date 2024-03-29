
package com.sos.inventory.model.job;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AdmissionTimePeriodType {

    MONTHLY_DATE_PERIOD("MonthlyDatePeriod"),
    MONTHLY_LAST_DATE_PERIOD("MonthlyLastDatePeriod"),
    MONTHLY_WEEKDAY_PERIOD("MonthlyWeekdayPeriod"),
    MONTHLY_LAST_WEEKDAY_PERIOD("MonthlyLastWeekdayPeriod"),
    WEEKDAY_PERIOD("WeekdayPeriod"),
    DAILY_PERIOD("DailyPeriod"),
    SPECIFIC_DATE_PERIOD("SpecificDatePeriod");
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
