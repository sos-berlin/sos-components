
package com.sos.joc.publish.repository.util;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum StoreItemsCategory {

    BOTH("BOTH"),
    ROLLOUT("ROLLOUT"),
    LOCAL("LOCAL");
    private final String value;
    private final static Map<String, StoreItemsCategory> CONSTANTS = new HashMap<String, StoreItemsCategory>();

    static {
        for (StoreItemsCategory c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private StoreItemsCategory(String value) {
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
    public static StoreItemsCategory fromValue(String value) {
        StoreItemsCategory constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
