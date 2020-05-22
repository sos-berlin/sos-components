
package com.sos.joc.model.common;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DeployOperationStatus {

    ADD("ADD"),
    UPDATE("UPDATE"),
    DELETE("DELETE"),
    NONE("NONE");
    private final String value;
    private final static Map<String, DeployOperationStatus> CONSTANTS = new HashMap<String, DeployOperationStatus>();

    static {
        for (DeployOperationStatus c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private DeployOperationStatus(String value) {
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
    public static DeployOperationStatus fromValue(String value) {
        DeployOperationStatus constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
