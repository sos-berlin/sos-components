
package com.sos.inventory.model.deploy;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DeployType {

    WORKFLOW("Workflow", 1),
    JOBCLASS("JobClass", 2),
    LOCK("Lock", 4),
    JUNCTION("Junction", 5),
    FILEORDERSOURCE("FileWatch", 9);
    private final String value;
    private final Integer intValue;
    private final static Map<String, DeployType> CONSTANTS = new HashMap<String, DeployType>();
    private final static Map<Integer, DeployType> INTCONSTANTS = new HashMap<Integer, DeployType>();

    static {
        for (DeployType c: values()) {
            INTCONSTANTS.put(c.intValue, c);
        }
    }

    static {
        for (DeployType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private DeployType(String value, Integer intValue) {
        this.value = value;
        this.intValue = intValue;
    }

    @Override
    public String toString() {
        return this.value;
    }

    @JsonValue
    public String value() {
        return this.value;
    }

    public Integer intValue() {
        return this.intValue;
    }

    @JsonCreator
    public static DeployType fromValue(String value) {
        DeployType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

    public static DeployType fromValue(Integer intValue) {
        DeployType constant = INTCONSTANTS.get(intValue);
        if (constant == null) {
            throw new IllegalArgumentException(intValue + "");
        } else {
            return constant;
        }
    }

}
