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
     * @param controllerId
     * @param variables
     */
    public OrderStepFinished(String key, String controllerId, Map<String, String> variables) {
        super(key, controllerId, variables);
    }
}
