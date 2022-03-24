package com.sos.commons.git.enums;

import java.util.HashMap;
import java.util.Map;

public enum GitConfigType {
    LOCAL("--local"),
    GLOBAL("--global"),
    SYSTEM("--system"),
    WORKTREE("--worktree"),
    FILE("--file"),
    BLOB("--blob");

    private final String value;
    private final static Map<String, GitConfigType> CONSTANTS = new HashMap<String, GitConfigType>();
    
    static {
        for (GitConfigType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }
    
    GitConfigType(String value) {
        this.value = value;
    }
    
    @Override
    public String toString() {
        return this.value;
    }
    
    public String value() {
        return this.value;
    }
    
    public static GitConfigType fromValue(String value) {
        GitConfigType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
