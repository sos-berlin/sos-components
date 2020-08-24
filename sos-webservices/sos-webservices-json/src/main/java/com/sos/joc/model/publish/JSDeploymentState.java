
package com.sos.joc.model.publish;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum JSDeploymentState {

    DEPLOYED(0),
    NOT_DEPLOYED(1);
    private final Integer value;
    private final static Map<Integer, JSDeploymentState> CONSTANTS = new HashMap<Integer, JSDeploymentState>();

    static {
        for (JSDeploymentState c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private JSDeploymentState(Integer value) {
        this.value = value;
    }

    @JsonValue
    public Integer value() {
        return this.value;
    }

    @JsonCreator
    public static JSDeploymentState fromValue(Integer value) {
        JSDeploymentState constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException((value +""));
        } else {
            return constant;
        }
    }

}
