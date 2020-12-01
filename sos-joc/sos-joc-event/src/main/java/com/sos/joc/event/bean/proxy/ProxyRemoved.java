package com.sos.joc.event.bean.proxy;

public class ProxyRemoved extends ProxyEvent {
    /**
     * No args constructor for use in serialization
     * 
     */
    public ProxyRemoved() {
    }

    /**
     * @param key
     * @param controllerId
     * @param variables
     */
    public ProxyRemoved(String key, String controllerId) {
        super(key, controllerId);
    }
}
