
package com.sos.controller.model.common;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SyncStateText {

    IN_SYNC("IN_SYNC"),
    NOT_IN_SYNC("NOT_IN_SYNC"),
    NOT_DEPLOYED("NOT_DEPLOYED"),
    SUSPENDED("SUSPENDED"),
    OUTSTANDING("OUTSTANDING"),
    UNKNOWN("UNKNOWN");
    private final String value;
    private final static Map<String, SyncStateText> CONSTANTS = new HashMap<String, SyncStateText>();

    static {
        for (SyncStateText c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private SyncStateText(String value) {
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
    public static SyncStateText fromValue(String value) {
        SyncStateText constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
