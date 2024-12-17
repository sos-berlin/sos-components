
package com.sos.joc.model.monitoring.enums;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EntryItemSource {

    history("history"),
    historyNotInInventory("historyNotInInventory"),
    inventory("inventory"),
    webservice("webservice");
    private final String value;
    private final static Map<String, EntryItemSource> CONSTANTS = new HashMap<String, EntryItemSource>();

    static {
        for (EntryItemSource c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private EntryItemSource(String value) {
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
    public static EntryItemSource fromValue(String value) {
        EntryItemSource constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
