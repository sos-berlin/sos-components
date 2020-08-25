package com.sos.joc.classes.proxy;

import js7.proxy.javaapi.data.auth.JCredentials;

public enum ProxyUser {
    
    JOC("JOC"),
    HISTORY("history");
    
    private final String val;

    private ProxyUser(String value) {
        this.val = value;
    }

    @Override
    public String toString() {
        return this.val.toString();
    }

    protected JCredentials value() {
        if (this.val.equals("history")) {
            return JCredentials.of(this.val, this.val);
        } else {
            return JCredentials.noCredentials();
        }
        //return JCredentials.of(this.val, "");
    }
}
