package com.sos.inventory.model.report;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ReportOrder {

    HIGHEST("highest", 1),
    LOWEST("lowest", 2);
    
    private final String strValue;
    private final Integer intValue;
    private final static Map<String, ReportOrder> CONSTANTS = new HashMap<String, ReportOrder>();
    private final static Map<String, ReportOrder> STRCONSTANTS = new HashMap<String, ReportOrder>();
    private final static Map<Integer, ReportOrder> INTCONSTANTS = new HashMap<Integer, ReportOrder>();

    static {
        for (ReportOrder c: values()) {
            CONSTANTS.put(c.name(), c);
        }
    }

    static {
        for (ReportOrder c: values()) {
            STRCONSTANTS.put(c.strValue, c);
        }
    }
    
    static {
        for (ReportOrder c: values()) {
            INTCONSTANTS.put(c.intValue, c);
        }
    }

    private ReportOrder(String strValue, Integer intValue) {
        this.strValue = strValue;
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

    public String strValue() {
        return this.strValue;
    }
    
    public Integer intValue() {
        return this.intValue;
    }

    @JsonCreator
    public static ReportOrder fromValue(String value) {
        ReportOrder constant = CONSTANTS.get(value);
        if (constant == null) {
            return fromStrValue(value);
        } else {
            return constant;
        }
    }
    
    public static ReportOrder fromValue(Integer value) {
        ReportOrder constant = INTCONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value + "");
        } else {
            return constant;
        }
    }
    
    private static ReportOrder fromStrValue(String value) {
        ReportOrder constant = STRCONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }
}
