package com.sos.joc.event.bean.proxy;

public class ProxyClosed extends ProxyEvent {
    /**
     * No args constructor for use in serialization
     * 
     */
    public ProxyClosed() {
    }

    /**
     * @param key
     * @param controllerId
     * @param variables
     */
    public ProxyClosed(String key, String controllerId) {
        super(key, controllerId);
    }
}
