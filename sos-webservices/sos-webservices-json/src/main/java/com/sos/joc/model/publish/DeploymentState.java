
package com.sos.joc.model.publish;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DeploymentState {

    DEPLOYED(0),
    NOT_DEPLOYED(1);
    private final Integer value;
    private final static Map<Integer, DeploymentState> CONSTANTS = new HashMap<Integer, DeploymentState>();

    static {
        for (DeploymentState c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private DeploymentState(Integer value) {
        this.value = value;
    }

    @JsonValue
    public Integer value() {
        return this.value;
    }

    @JsonCreator
    public static DeploymentState fromValue(Integer value) {
        DeploymentState constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException((value +""));
        } else {
            return constant;
        }
    }

}
