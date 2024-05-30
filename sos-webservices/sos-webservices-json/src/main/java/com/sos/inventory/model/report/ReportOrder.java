
package com.sos.inventory.model.report;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ReportOrder {

    HIGHEST("HIGHEST"),
    LOWEST("LOWEST");
    private final String value;
    private final static Map<String, ReportOrder> CONSTANTS = new HashMap<String, ReportOrder>();

    static {
        for (ReportOrder c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private ReportOrder(String value) {
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
    public static ReportOrder fromValue(String value) {
        ReportOrder constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
