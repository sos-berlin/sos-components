
package com.sos.joc.model.order;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ObstacleType {

    WaitingForAdmission("WaitingForAdmission"),
    JobParallelismLimitReached("JobParallelismLimitReached");
    private final String value;
    private final static Map<String, ObstacleType> CONSTANTS = new HashMap<String, ObstacleType>();

    static {
        for (ObstacleType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private ObstacleType(String value) {
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
    public static ObstacleType fromValue(String value) {
        ObstacleType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
