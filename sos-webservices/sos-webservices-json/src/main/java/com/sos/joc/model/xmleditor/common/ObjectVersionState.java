
package com.sos.joc.model.xmleditor.common;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ObjectVersionState {

    LIVE_IS_NEWER("LIVE_IS_NEWER"),
    DRAFT_IS_NEWER("DRAFT_IS_NEWER"),
    LIVE_NOT_EXIST("LIVE_NOT_EXIST"),
    DRAFT_NOT_EXIST("DRAFT_NOT_EXIST"),
    NO_CONFIGURATION_EXIST("NO_CONFIGURATION_EXIST");
    private final String value;
    private final static Map<String, ObjectVersionState> CONSTANTS = new HashMap<String, ObjectVersionState>();

    static {
        for (ObjectVersionState c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private ObjectVersionState(String value) {
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
    public static ObjectVersionState fromValue(String value) {
        ObjectVersionState constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
