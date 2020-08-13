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
     * @param jobschedulerId
     * @param variables
     */
    public ProxyRemoved(String key, String jobschedulerId, Map<String, String> variables) {
        super(key, jobschedulerId, variables);
    }
}
