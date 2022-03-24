package com.sos.commons.git.enums;

import java.util.HashMap;
import java.util.Map;

public enum RepositoryLinkType {
    FETCH("(fetch)"),
    PUSH("(push)");

    private final String value;
    private final static Map<String, RepositoryLinkType> CONSTANTS = new HashMap<String, RepositoryLinkType>();
    
    static {
        for (RepositoryLinkType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }
    
    RepositoryLinkType(String value) {
        this.value = value;
    }
    
    @Override
    public String toString() {
        return this.value;
    }
    
    public String value() {
        return this.value;
    }
    
    public static RepositoryLinkType fromValue(String value) {
        RepositoryLinkType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
