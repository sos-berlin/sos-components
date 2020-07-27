
package com.sos.joc.model.inventory.common;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ItemStateEnum {

    DEPLOYMENT_IS_NEWER("DEPLOYMENT_IS_NEWER"),
    DRAFT_IS_NEWER("DRAFT_IS_NEWER"),
    DEPLOYMENT_NOT_EXIST("DEPLOYMENT_NOT_EXIST"),
    DRAFT_NOT_EXIST("DRAFT_NOT_EXIST"),
    NO_CONFIGURATION_EXIST("NO_CONFIGURATION_EXIST");
    private final String value;
    private final static Map<String, ItemStateEnum> CONSTANTS = new HashMap<String, ItemStateEnum>();

    static {
        for (ItemStateEnum c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private ItemStateEnum(String value) {
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
    public static ItemStateEnum fromValue(String value) {
        ItemStateEnum constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
