package com.sos.joc.event.bean.event;

import java.util.Map;

public class EventServiceStart extends EventServiceEvent {
    
    /**
     * No args constructor for use in serialization
     * 
     */
    public EventServiceStart() {
    }

    /**
     * @param key
     * @param controllerId
     * @param variables
     */
    public EventServiceStart(String key, String controllerId, Map<String, String> variables) {
        super(key, controllerId, variables);
    }
    
    public EventServiceStart(String key, String controllerId, String accessToken) {
        super(key, controllerId, accessToken);
    }
}
