
package com.sos.joc.model.publish;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
    public String strValue() {
        return this.name();
    }
    
    public Integer value() {
        return this.value;
    }

    @JsonCreator
    public static DeploymentState fromValue(String value) {
        Optional<DeploymentState> opt = Arrays.asList(DeploymentState.values()).stream().filter(c -> c.name().equals(value)).findAny();
        if (opt.isEmpty()) {
            throw new IllegalArgumentException(value);
        } else {
            return opt.get();
        }
    }
    
    public static DeploymentState fromValue(Integer value) {
        DeploymentState constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException((value +""));
        } else {
            return constant;
        }
    }

}
