package com.sos.joc.event.bean.proxy;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ProxyCoupled extends ProxyEvent {
    /**
     * No args constructor for use in serialization
     * 
     */
    public ProxyCoupled() {
    }

    /**
     * @param key
     * @param controllerId
     * @param variables
     */
    public ProxyCoupled(String controllerId, Boolean isCoupled) {
        super("ProxyCoupled", controllerId, null);
        putVariable("isCoupled", isCoupled);
    }
    
    public ProxyCoupled(String controllerId, Boolean isCoupled, String url, String backupUrl, String user, String pwd) {
        super("ProxyCoupled", controllerId, null);
        putVariable("isCoupled", isCoupled);
        putVariable("url", url);
        putVariable("backupUrl", backupUrl);
        putVariable("user", user);
        putVariable("pwd", pwd);
    }
    
    @JsonIgnore
    public Boolean isCoupled() {
        return (Boolean) getVariables().get("isCoupled");
    }
    
    @JsonIgnore
    public String getUrl() {
        return (String) getVariables().get("url");
    }
    
    @JsonIgnore
    public String getBackupUrl() {
        return (String) getVariables().get("backupUrl");
    }
    
    @JsonIgnore
    public String getUser() {
        return (String) getVariables().get("user");
    }
    
    @JsonIgnore
    public String getPwd() {
        return (String) getVariables().get("pwd");
    }
}
