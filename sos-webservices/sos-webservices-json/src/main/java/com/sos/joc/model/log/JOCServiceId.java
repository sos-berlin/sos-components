
package com.sos.joc.model.log;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum JOCServiceId {

    JOC("joc"),
    CLUSTER("service-cluster"),
    HISTORY("service-history"),
    DAILYPLAN("service-dailyplan"),
    CLEANUP("service-cleanup"),
    MONITOR("service-monitor"),
    LOGNOTIFICATION("service-lognotification"),
    REPORTS("service-reports"),
    AUTHENTICATION("authentication"),
    AUDIT("audit");
    private final String value;
    private final static Map<String, JOCServiceId> CONSTANTS = new HashMap<String, JOCServiceId>();
    private final static Map<String, JOCServiceId> LOGPREFIX = new HashMap<String, JOCServiceId>();

    static {
        for (JOCServiceId c: values()) {
            CONSTANTS.put(c.name(), c);
        }
    }
    
    static {
        for (JOCServiceId c: values()) {
            LOGPREFIX.put(c.value, c);
        }
    }

    private JOCServiceId(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.name();
    }

    @JsonValue
    public String value() {
        return this.name();
    }
    
    public String logPrefix() {
        return this.value;
    }

    @JsonCreator
    public static JOCServiceId fromValue(String value) {
        JOCServiceId constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }
    
    public static JOCServiceId fromLogPrefix(String value) {
        JOCServiceId constant = LOGPREFIX.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
