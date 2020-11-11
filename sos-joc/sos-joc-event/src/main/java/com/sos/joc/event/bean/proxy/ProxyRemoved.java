package com.sos.joc.event.bean.proxy;

import java.util.Map;

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
    public ProxyRemoved(String key, String controllerId, Map<String, String> variables) {
        super(key, controllerId, variables);
    }
}
