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
    
    @JsonIgnore
    public Boolean isCoupled() {
        return (Boolean) getVariables().get("isCoupled");
    }
}
