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
     * @param jobschedulerId
     * @param variables
     */
    public ProxyStarted(String key, String jobschedulerId, Map<String, String> variables) {
        super(key, jobschedulerId, variables);
    }
}
