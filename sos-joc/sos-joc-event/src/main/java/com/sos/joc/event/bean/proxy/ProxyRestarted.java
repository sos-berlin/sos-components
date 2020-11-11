package com.sos.joc.event.bean.proxy;

import java.util.Map;

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
    public ProxyRestarted(String key, String controllerId, Map<String, String> variables) {
        super(key, controllerId, variables);
    }
}
