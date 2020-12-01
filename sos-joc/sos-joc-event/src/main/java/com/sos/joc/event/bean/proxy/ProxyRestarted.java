package com.sos.joc.event.bean.proxy;

public class ProxyRestarted extends ProxyEvent {
    /**
     * No args constructor for use in serialization
     * 
     */
    public ProxyRestarted() {
    }

    /**
     * @param key
     * @param controllerId
     * @param variables
     */
    public ProxyRestarted(String key, String controllerId) {
        super(key, controllerId);
    }
}
