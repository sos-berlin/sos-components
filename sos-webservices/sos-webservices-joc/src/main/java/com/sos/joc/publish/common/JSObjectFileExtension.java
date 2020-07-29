package com.sos.joc.publish.common;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum JSObjectFileExtension {
    WORKFLOW_FILE_EXTENSION(".workflow.json"),
    WORKFLOW_SIGNATURE_FILE_EXTENSION(".workflow.json.asc"),
    AGENT_REF_FILE_EXTENSION(".agentref.json"),
    AGENT_REF_SIGNATURE_FILE_EXTENSION(".agentref.json.asc"),
    LOCK_FILE_EXTENSION(".lock.json"),
    LOCK_SIGNATURE_FILE_EXTENSION(".lock.json.asc"),
    JUNCTION_FILE_EXTENSION(".junction.json"),
    JUNCTION_SIGNATURE_FILE_EXTENSION(".junction.json.asc");

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
