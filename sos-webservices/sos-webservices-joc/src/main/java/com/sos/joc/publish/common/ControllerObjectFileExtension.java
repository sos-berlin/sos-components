package com.sos.joc.publish.common;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ControllerObjectFileExtension {
    WORKFLOW_FILE_EXTENSION(".workflow.json"),
    WORKFLOW_PGP_SIGNATURE_FILE_EXTENSION(".workflow.json.asc"),
    WORKFLOW_X509_SIGNATURE_FILE_EXTENSION(".workflow.json.sig"),
    JOBRESOURCE_FILE_EXTENSION(".jobresource.json"),
    JOBRESOURCE_PGP_SIGNATURE_FILE_EXTENSION(".jobresource.json.asc"),
    JOBRESOURCE_X509_SIGNATURE_FILE_EXTENSION(".jobresource.json.sig"),
    LOCK_FILE_EXTENSION(".lock.json"),
    NOTICEBOARD_FILE_EXTENSION(".noticeboard.json"),
    JOBCLASS_FILE_EXTENSION(".jobclass.json"),
    FILEORDERSOURCE_FILE_EXTENSION(".fileordersource.json");

    private final String value;
    private static final Map<String, ControllerObjectFileExtension> CONSTANTS = new HashMap<String, ControllerObjectFileExtension>();

    static {
        for (ControllerObjectFileExtension c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private ControllerObjectFileExtension(String value) {
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
    public static ControllerObjectFileExtension fromValue(String value) {
    	ControllerObjectFileExtension constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
