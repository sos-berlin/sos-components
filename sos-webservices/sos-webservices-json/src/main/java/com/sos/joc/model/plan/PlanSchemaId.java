
package com.sos.joc.model.plan;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PlanSchemaId {

    Global("Global"),
    DailyPlan("DailyPlan");
    private final String value;
    private final static Map<String, PlanSchemaId> CONSTANTS = new HashMap<String, PlanSchemaId>();

    static {
        for (PlanSchemaId c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private PlanSchemaId(String value) {
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
    public static PlanSchemaId fromValue(String value) {
        PlanSchemaId constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
