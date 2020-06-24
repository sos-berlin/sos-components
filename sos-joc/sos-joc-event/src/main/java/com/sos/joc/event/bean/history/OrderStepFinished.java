package com.sos.joc.event.bean.history;

import java.util.Map;

public class OrderStepFinished extends HistoryEvent {
    
    /**
     * No args constructor for use in serialization
     * 
     */
    public OrderStepFinished() {
    }

    /**
     * @param key
     * @param jobschedulerId
     * @param variables
     */
    public OrderStepFinished(String key, String jobschedulerId, Map<String, String> variables) {
        super(key, jobschedulerId, variables);
    }
}
