package com.sos.joc.event.bean.history;

import java.util.Map;

public class OrderStepStarted extends HistoryEvent {
    
    /**
     * No args constructor for use in serialization
     * 
     */
    public OrderStepStarted() {
    }

    /**
     * @param key
     * @param jobschedulerId
     * @param variables
     */
    public OrderStepStarted(String key, String controllerId, Map<String, String> variables) {
        super(key, controllerId, variables);
    }
}
