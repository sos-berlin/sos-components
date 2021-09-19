
package com.sos.controller.model.board;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum NoticeStateText {

    POSTED("POSTED"),
    EXPECTED("EXPECTED");
    private final String value;
    private final static Map<String, NoticeStateText> CONSTANTS = new HashMap<String, NoticeStateText>();

    static {
        for (NoticeStateText c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private NoticeStateText(String value) {
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
    public static NoticeStateText fromValue(String value) {
        NoticeStateText constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
