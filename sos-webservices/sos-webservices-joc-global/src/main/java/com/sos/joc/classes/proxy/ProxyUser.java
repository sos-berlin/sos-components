package com.sos.joc.classes.proxy;

import java.util.HashMap;
import java.util.Map;

import js7.proxy.javaapi.JCredentials;

public enum ProxyUser {
    
    JOC("JOC"),
    HISTORY("history");
    
    private final String val;
    private final static Map<String, ProxyUser> CONSTANTS = new HashMap<String, ProxyUser>();

    static {
        for (ProxyUser c: values()) {
            CONSTANTS.put(c.val, c);
        }
    }

    private ProxyUser(String value) {
        this.val = value;
    }

    @Override
    public String toString() {
        return this.val.toString();
    }

    public JCredentials value() {
        if (this.val.equals("history")) {
            return JCredentials.of(this.val, this.val);
        } else {
            return JCredentials.noCredentials();
        }
        //return JCredentials.of(this.val, "");
    }

    public static ProxyUser fromValue(String value) {
        ProxyUser constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value.toString());
        } else {
            return constant;
        }
    }
}
