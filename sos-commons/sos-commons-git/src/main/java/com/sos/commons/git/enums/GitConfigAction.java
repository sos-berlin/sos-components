package com.sos.commons.git.enums;

import java.util.HashMap;
import java.util.Map;

public enum GitConfigAction {
    ADD("ADD"),
    GET("GET"),
    UNSET("UNSET");

    private final String value;
    private final static Map<String, GitConfigAction> CONSTANTS = new HashMap<String, GitConfigAction>();
    
    static {
        for (GitConfigAction c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }
    
    GitConfigAction(String value) {
        this.value = value;
    }
    
    @Override
    public String toString() {
        return this.value;
    }
    
    public String value() {
        return this.value;
    }
    
    public static GitConfigAction fromValue(String value) {
        GitConfigAction constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
