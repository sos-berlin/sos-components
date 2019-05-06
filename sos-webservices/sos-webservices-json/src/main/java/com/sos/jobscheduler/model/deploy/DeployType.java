
package com.sos.jobscheduler.model.deploy;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DeployType {

    WORKFLOW("Workflow"),
    AGENT_REF("AgentRef"),
    LOCK("Lock");
    private final String value;
    private final static Map<String, DeployType> CONSTANTS = new HashMap<String, DeployType>();

    static {
        for (DeployType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private DeployType(String value) {
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
    public static DeployType fromValue(String value) {
        DeployType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
