package com.sos.joc.classes.proxy;

import java.util.Base64;

import js7.proxy.javaapi.data.auth.JCredentials;

public enum ProxyUser {
    
    JOC("JOC", "JS7-JOC"),
    HISTORY("History", "JS7-History");
    
    private final String user;
    private final String pwd;

    private ProxyUser(String user, String pwd) {
        this.user = user;
        this.pwd = pwd;
    }

    @Override
    public String toString() {
        return this.user;
    }
    
    public String getUser() {
        return user;
    }
    
    public String getPwd() {
        return pwd;
    }

    protected JCredentials value() {
        return JCredentials.of(this.user, this.pwd);
    }
    
    public String getBasicAuthorization() {
        return new String(Base64.getEncoder().encode((this.user + ":" + this.pwd).getBytes()));
    }
}
