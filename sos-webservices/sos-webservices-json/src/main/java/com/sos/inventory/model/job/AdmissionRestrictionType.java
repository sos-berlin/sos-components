
package com.sos.inventory.model.job;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AdmissionRestrictionType {

    MONTH_RESTRICTION("MonthRestriction");
    private final String value;
    private final static Map<String, AdmissionRestrictionType> CONSTANTS = new HashMap<String, AdmissionRestrictionType>();

    static {
        for (AdmissionRestrictionType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private AdmissionRestrictionType(String value) {
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
    public static AdmissionRestrictionType fromValue(String value) {
        AdmissionRestrictionType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
