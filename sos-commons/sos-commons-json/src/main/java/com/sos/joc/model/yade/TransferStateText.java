
package com.sos.joc.model.yade;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TransferStateText {

    SUCCESSFUL("SUCCESSFUL"),
    FAILED("FAILED"),
    INCOMPLETE("INCOMPLETE");
    private final String value;
    private final static Map<String, TransferStateText> CONSTANTS = new HashMap<String, TransferStateText>();

    static {
        for (TransferStateText c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private TransferStateText(String value) {
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
    public static TransferStateText fromValue(String value) {
        TransferStateText constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
