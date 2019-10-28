package com.sos.joc.deploy.common;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum JSObjectFileExtension {
    WORKFLOW_FILE_EXTENSION(".workflow.json"),
    AGENT_REF_FILE_EXTENSION(".agentRef.json"),
    LOCK_FILE_EXTENSION(".lock.json");

    private final String value;
    private static final Map<String, JSObjectFileExtension> CONSTANTS = new HashMap<String, JSObjectFileExtension>();

    static {
        for (JSObjectFileExtension c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private JSObjectFileExtension(String value) {
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
    public static JSObjectFileExtension fromValue(String value) {
    	JSObjectFileExtension constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
