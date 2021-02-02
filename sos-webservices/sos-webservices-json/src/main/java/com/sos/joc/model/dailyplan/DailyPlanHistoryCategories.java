
package com.sos.joc.model.dailyplan;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DailyPlanHistoryCategories {

    SUBMITTED("SUBMITTED"),
    NOT_SUBMITTED("NOT-SUBMITTED"),
    CANCELED("CANCELED"),
    ERROR("ERROR"),
    WARN("WARN");
    private final String value;
    private final static Map<String, DailyPlanHistoryCategories> CONSTANTS = new HashMap<String, DailyPlanHistoryCategories>();

    static {
        for (DailyPlanHistoryCategories c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private DailyPlanHistoryCategories(String value) {
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
    public static DailyPlanHistoryCategories fromValue(String value) {
        DailyPlanHistoryCategories constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
