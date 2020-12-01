package com.sos.joc.event.bean.proxy;

public class ProxyStarted extends ProxyEvent {
    /**
     * No args constructor for use in serialization
     * 
     */
    public ProxyStarted() {
    }

    /**
     * @param key
     * @param controllerId
     * @param variables
     */
    public ProxyStarted(String key, String controllerId) {
        super(key, controllerId);
    }
}
