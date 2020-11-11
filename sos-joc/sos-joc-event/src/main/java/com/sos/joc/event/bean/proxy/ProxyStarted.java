package com.sos.joc.event.bean.proxy;

import java.util.Map;

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
    public ProxyStarted(String key, String controllerId, Map<String, String> variables) {
        super(key, controllerId, variables);
    }
}
