package com.sos.commons.git.enums;

import java.util.HashMap;
import java.util.Map;


public enum RepositoryUpdateType {
    BRANCH("[new branch]"),
    TAG("[new tag]");
    
    private final String value;
    private final static Map<String, RepositoryUpdateType> CONSTANTS = new HashMap<String, RepositoryUpdateType>();
    
    static {
        for (RepositoryUpdateType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }
    
    RepositoryUpdateType(String value) {
        this.value = value;
    }
    
    @Override
    public String toString() {
        return this.value;
    }
    
    public String value() {
        return this.value;
    }

    public static RepositoryUpdateType fromValue(String value) {
        RepositoryUpdateType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
